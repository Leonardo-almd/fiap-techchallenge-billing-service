package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.consumer;

import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.CreateBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.ServiceOrderCreatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * SQS consumer for Service Order events.
 * Listens to service-order-events queue and processes ORDER_CREATED events.
 * Supports both the Billing-native format (ServiceOrderCreatedEvent with items)
 * and the OS-Service format (ServiceOrderEventDto without items).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderEventConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final CreateBudgetUseCase createBudgetUseCase;

    @Value("${aws.sqs.queues.service-order-events}")
    private String queueName;

    /**
     * Poll for messages from service-order-events queue.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedDelay = 5000)
    public void pollMessages() {
        try {
            String queueUrl = getQueueUrl();

            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(request);

            if (!response.messages().isEmpty()) {
                log.info("Received {} messages from queue: {}", response.messages().size(), queueName);

                for (Message message : response.messages()) {
                    processMessage(message, queueUrl);
                }
            }

        } catch (Exception e) {
            log.error("Error polling messages from SQS", e);
        }
    }

    /**
     * Process a single message using flexible JSON parsing.
     * Accepts both OS-Service event format and Billing-native format.
     */
    private void processMessage(Message message, String queueUrl) {
        try {
            log.info("Processing message: {}", message.messageId());

            JsonNode json = objectMapper.readTree(message.body());

            // Check eventType — only process ORDER_CREATED events
            String eventType = json.has("eventType") ? json.get("eventType").asText() : "";
            if (!eventType.isEmpty() && !"ORDER_CREATED".equals(eventType)) {
                log.debug("Ignoring event type: {}", eventType);
                deleteMessage(queueUrl, message.receiptHandle());
                return;
            }

            // Extract serviceOrderId (supports both 'serviceOrderId' and 'orderId')
            String serviceOrderId = extractStringField(json, "serviceOrderId", "orderId");
            String customerId = extractStringField(json, "customerId");
            String vehicleId = extractStringField(json, "vehicleId");
            String description = json.has("description") ? json.get("description").asText() : "";

            if (serviceOrderId == null || serviceOrderId.isBlank()) {
                log.error("Missing serviceOrderId/orderId in event, skipping message: {}", message.messageId());
                deleteMessage(queueUrl, message.receiptHandle());
                return;
            }

            // Extract items — may be present (Billing-native format) or absent (OS-Service format)
            List<CreateBudgetUseCase.BudgetItemRequest> items = extractItems(json, serviceOrderId, description);

            log.info("Creating budget for service order: {} with {} item(s)", serviceOrderId, items.size());

            createBudgetUseCase.execute(serviceOrderId, customerId, vehicleId, items);

            deleteMessage(queueUrl, message.receiptHandle());
            log.info("Message processed successfully: {}", message.messageId());

        } catch (Exception e) {
            log.error("Error processing message: {}", message.messageId(), e);
        }
    }

    /**
     * Extract items from event JSON. The OS Service sends services and resources as an items[] array
     * with {type, itemCode, description, quantity, unitPrice}. If items are absent (unexpected),
     * creates a fallback SERVICE item so the Budget can still be created.
     */
    private List<CreateBudgetUseCase.BudgetItemRequest> extractItems(
            JsonNode json, String serviceOrderId, String description) {

        List<CreateBudgetUseCase.BudgetItemRequest> items = new ArrayList<>();

        if (json.has("items") && json.get("items").isArray() && !json.get("items").isEmpty()) {
            for (JsonNode itemNode : json.get("items")) {
                String type = itemNode.has("type") ? itemNode.get("type").asText() : "SERVICE";
                String itemCode = itemNode.has("itemCode") ? itemNode.get("itemCode").asText() : "";
                String desc = itemNode.has("description") ? itemNode.get("description").asText() : "";
                int quantity = itemNode.has("quantity") ? itemNode.get("quantity").asInt() : 1;
                BigDecimal unitPrice = itemNode.has("unitPrice")
                    ? new BigDecimal(itemNode.get("unitPrice").asText())
                    : BigDecimal.ZERO;

                items.add(new CreateBudgetUseCase.BudgetItemRequest(
                    BudgetItemType.valueOf(type), itemCode, desc, quantity, unitPrice));
            }
            log.info("Parsed {} item(s) from ORDER_CREATED event for OS: {}", items.size(), serviceOrderId);
        } else {
            // Fallback: items should normally come from OS Service, but create a generic one if absent
            log.warn("No items array in ORDER_CREATED event for OS: {}. Creating fallback item.", serviceOrderId);
            items.add(new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.SERVICE,
                "OS-" + serviceOrderId,
                description != null && !description.isBlank() ? description : "Servico OS #" + serviceOrderId,
                1,
                BigDecimal.ZERO
            ));
        }

        return items;
    }

    /**
     * Extract a string field from JSON, trying multiple field names (alias support).
     */
    private String extractStringField(JsonNode json, String... fieldNames) {
        for (String name : fieldNames) {
            if (json.has(name) && !json.get(name).isNull()) {
                return json.get(name).asText();
            }
        }
        return null;
    }

    private void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build();
        sqsClient.deleteMessage(deleteRequest);
    }

    private String getQueueUrl() {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
            .queueName(queueName)
            .build();
        return sqsClient.getQueueUrl(request).queueUrl();
    }
}

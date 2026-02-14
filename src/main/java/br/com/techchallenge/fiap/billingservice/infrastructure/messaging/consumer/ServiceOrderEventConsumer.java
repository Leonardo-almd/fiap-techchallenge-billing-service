package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.consumer;

import br.com.techchallenge.fiap.billingservice.application.entity.BudgetItemType;
import br.com.techchallenge.fiap.billingservice.application.usecase.budget.CreateBudgetUseCase;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.ServiceOrderCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQS consumer for Service Order events.
 * Listens to service-order-events queue and processes ServiceOrderCreatedEvent.
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
                log.info("📥 Received {} messages from queue: {}", response.messages().size(), queueName);

                for (Message message : response.messages()) {
                    processMessage(message, queueUrl);
                }
            }

        } catch (Exception e) {
            log.error("❌ Error polling messages from SQS", e);
        }
    }

    /**
     * Process a single message.
     */
    private void processMessage(Message message, String queueUrl) {
        try {
            log.info("🔄 Processing message: {}", message.messageId());

            ServiceOrderCreatedEvent event = objectMapper.readValue(
                message.body(),
                ServiceOrderCreatedEvent.class
            );

            handleServiceOrderCreated(event);

            // Delete message after successful processing
            deleteMessage(queueUrl, message.receiptHandle());

            log.info("✅ Message processed successfully: {}", message.messageId());

        } catch (Exception e) {
            log.error("❌ Error processing message: {}", message.messageId(), e);
            // Message will be retried based on SQS visibility timeout
        }
    }

    /**
     * Handle ServiceOrderCreatedEvent by creating a budget.
     */
    private void handleServiceOrderCreated(ServiceOrderCreatedEvent event) {
        log.info("🎯 Handling ServiceOrderCreatedEvent for order: {}", event.serviceOrderId());

        List<CreateBudgetUseCase.BudgetItemRequest> items = event.items().stream()
            .map(item -> new CreateBudgetUseCase.BudgetItemRequest(
                BudgetItemType.valueOf(item.type()),
                item.itemCode(),
                item.description(),
                item.quantity(),
                item.unitPrice()
            ))
            .collect(Collectors.toList());

        createBudgetUseCase.execute(
            event.serviceOrderId(),
            event.customerId(),
            event.vehicleId(),
            items
        );

        log.info("✅ Budget created for service order: {}", event.serviceOrderId());
    }

    /**
     * Delete message from queue.
     */
    private void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(receiptHandle)
            .build();

        sqsClient.deleteMessage(deleteRequest);
    }

    /**
     * Get SQS queue URL.
     */
    private String getQueueUrl() {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
            .queueName(queueName)
            .build();
        
        return sqsClient.getQueueUrl(request).queueUrl();
    }
}

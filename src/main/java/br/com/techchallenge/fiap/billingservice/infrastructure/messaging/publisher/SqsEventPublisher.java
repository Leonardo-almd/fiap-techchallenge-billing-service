package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher;

import br.com.techchallenge.fiap.billingservice.application.exception.MessagingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.Map;

/**
 * SQS event publisher for publishing domain events.
 * Uses SqsOperations (port) so tests can mock without AWS SDK types.
 * Supports both FIFO queues (billing-events) and standard queues (OS-service
 * compensation).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqsEventPublisher {

    private final SqsOperations sqsOperations;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queues.billing-events-url}")
    private String billingEventsQueueUrl;

    /**
     * Publish an event to the billing events FIFO queue.
     */
    public void publishEvent(Object event) {
        try {
            String queueUrl = billingEventsQueueUrl;
            String messageBody = objectMapper.writeValueAsString(event);

            log.info("Publishing event: {} to queue: {}",
                    event.getClass().getSimpleName(), queueUrl);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .messageGroupId(event.getClass().getSimpleName())
                    .messageDeduplicationId(java.util.UUID.randomUUID().toString())
                    .build();

            SendMessageResponse response = sqsOperations.sendMessage(request);

            log.info("Event published successfully - MessageId: {}", response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing event", e);
            throw new MessagingException("Error serializing event: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing event to SQS", e);
            throw new MessagingException("Error publishing event to SQS: " + e.getMessage(), e);
        }
    }

    /**
     * Publish a payload to a standard (non-FIFO) queue.
     * Used for Saga compensation/notification queues (e.g. quote-approved-queue,
     * payment-failed-queue).
     *
     * @param queueName the SQS standard queue name
     * @param payload   a simple Map that will be serialized to JSON
     */
    public void publishToStandardQueue(String queueUrl, Map<String, Object> payload) {
        try {
            String messageBody = objectMapper.writeValueAsString(payload);

            log.info("Publishing to standard queue: {}", queueUrl);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            SendMessageResponse response = sqsOperations.sendMessage(request);

            log.info("Published to standard queue {} - MessageId: {}", queueUrl, response.messageId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing payload for queue: {}", queueUrl, e);
            throw new MessagingException("Error serializing payload: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error publishing to standard queue: {}", queueUrl, e);
            throw new MessagingException("Error publishing to standard queue: " + e.getMessage(), e);
        }
    }
}

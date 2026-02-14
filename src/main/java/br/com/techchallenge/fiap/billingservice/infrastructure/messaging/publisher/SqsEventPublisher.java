package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher;

import br.com.techchallenge.fiap.billingservice.application.exception.MessagingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * SQS event publisher for publishing domain events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqsEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queues.billing-events}")
    private String billingEventsQueueName;

    /**
     * Publish an event to the billing events queue.
     */
    public void publishEvent(Object event) {
        try {
            String queueUrl = getQueueUrl(billingEventsQueueName);
            String messageBody = objectMapper.writeValueAsString(event);

            log.info("📤 Publishing event: {} to queue: {}", 
                     event.getClass().getSimpleName(), billingEventsQueueName);

            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageGroupId(event.getClass().getSimpleName())
                .messageDeduplicationId(java.util.UUID.randomUUID().toString())
                .build();

            SendMessageResponse response = sqsClient.sendMessage(request);

            log.info("✅ Event published successfully - MessageId: {}", response.messageId());

        } catch (JsonProcessingException e) {
            log.error("❌ Error serializing event", e);
            throw new MessagingException("Error serializing event: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error publishing event to SQS", e);
            throw new MessagingException("Error publishing event to SQS: " + e.getMessage(), e);
        }
    }

    /**
     * Get SQS queue URL by queue name.
     */
    private String getQueueUrl(String queueName) {
        GetQueueUrlRequest request = GetQueueUrlRequest.builder()
            .queueName(queueName)
            .build();
        
        return sqsClient.getQueueUrl(request).queueUrl();
    }
}

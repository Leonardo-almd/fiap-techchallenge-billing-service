package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher;

import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * Port for SQS operations so that tests can mock without depending on AWS SDK interfaces
 * (which Mockito cannot mock on some JDK versions).
 */
public interface SqsOperations {

    GetQueueUrlResponse getQueueUrl(GetQueueUrlRequest request);

    SendMessageResponse sendMessage(SendMessageRequest request);
}

package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * Adapter from AWS SqsClient to SqsOperations so production uses AWS and tests can mock SqsOperations.
 */
@Component
@RequiredArgsConstructor
public class SqsClientSqsOperationsAdapter implements SqsOperations {

    private final SqsClient sqsClient;

    @Override
    public GetQueueUrlResponse getQueueUrl(GetQueueUrlRequest request) {
        return sqsClient.getQueueUrl(request);
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        return sqsClient.sendMessage(request);
    }
}

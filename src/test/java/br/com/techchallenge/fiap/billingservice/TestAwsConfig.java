package br.com.techchallenge.fiap.billingservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * AWS clients for test profile: point to LocalStack-style endpoint so context loads
 * without real AWS credentials. Actual calls may fail if no LocalStack is running.
 */
@Configuration
@Profile("test")
public class TestAwsConfig {

    private static final String TEST_ENDPOINT = "http://localhost:4566";
    private static final StaticCredentialsProvider TEST_CREDENTIALS =
        StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(TEST_CREDENTIALS)
            .endpointOverride(URI.create(TEST_ENDPOINT))
            .build();
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(TEST_CREDENTIALS)
            .endpointOverride(URI.create(TEST_ENDPOINT))
            .build();
    }
}

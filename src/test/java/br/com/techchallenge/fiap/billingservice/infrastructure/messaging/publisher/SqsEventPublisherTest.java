package br.com.techchallenge.fiap.billingservice.infrastructure.messaging.publisher;

import br.com.techchallenge.fiap.billingservice.application.exception.MessagingException;
import br.com.techchallenge.fiap.billingservice.infrastructure.messaging.event.BudgetApprovedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SqsEventPublisher - Unit Tests")
class SqsEventPublisherTest {

    @Mock
    private SqsClient sqsClient;

    private ObjectMapper objectMapper;
    private SqsEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        publisher = new SqsEventPublisher(sqsClient, objectMapper);
        ReflectionTestUtils.setField(publisher, "billingEventsQueueName", "billing-events");
    }

    @Test
    @DisplayName("Should publish event successfully")
    void shouldPublishEventSuccessfully() {
        // Arrange
        BudgetApprovedEvent event = createBudgetApprovedEvent();
        
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenReturn(GetQueueUrlResponse.builder()
                .queueUrl("http://localhost:4566/000000000000/billing-events")
                .build());
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder()
                .messageId("MSG-001")
                .build());

        // Act
        publisher.publishEvent(event);

        // Assert
        verify(sqsClient).getQueueUrl(any(GetQueueUrlRequest.class));
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    @DisplayName("Should serialize event to JSON")
    void shouldSerializeEventToJson() {
        // Arrange
        BudgetApprovedEvent event = createBudgetApprovedEvent();
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenReturn(GetQueueUrlResponse.builder()
                .queueUrl("http://localhost:4566/queue")
                .build());
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().messageId("MSG-001").build());

        // Act
        publisher.publishEvent(event);

        // Assert
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();
        
        assertThat(request.messageBody()).contains("budgetId");
        assertThat(request.messageBody()).contains("BUDGET-001");
    }

    @Test
    @DisplayName("Should set message group ID to event class name")
    void shouldSetMessageGroupIdToEventClassName() {
        // Arrange
        BudgetApprovedEvent event = createBudgetApprovedEvent();
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenReturn(GetQueueUrlResponse.builder()
                .queueUrl("http://localhost:4566/queue")
                .build());
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().messageId("MSG-001").build());

        // Act
        publisher.publishEvent(event);

        // Assert
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();
        
        assertThat(request.messageGroupId()).isEqualTo("BudgetApprovedEvent");
    }

    @Test
    @DisplayName("Should generate unique deduplication ID")
    void shouldGenerateUniqueDeduplicationId() {
        // Arrange
        BudgetApprovedEvent event = createBudgetApprovedEvent();
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenReturn(GetQueueUrlResponse.builder()
                .queueUrl("http://localhost:4566/queue")
                .build());
        
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(SendMessageResponse.builder().messageId("MSG-001").build());

        // Act
        publisher.publishEvent(event);

        // Assert
        verify(sqsClient).sendMessage(captor.capture());
        SendMessageRequest request = captor.getValue();
        
        assertThat(request.messageDeduplicationId()).isNotNull();
        assertThat(request.messageDeduplicationId()).isNotBlank();
    }

    @Test
    @DisplayName("Should throw MessagingException when SQS fails")
    void shouldThrowMessagingExceptionWhenSqsFails() {
        // Arrange
        BudgetApprovedEvent event = createBudgetApprovedEvent();
        
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
            .thenThrow(new RuntimeException("SQS error"));

        // Act & Assert
        assertThatThrownBy(() -> publisher.publishEvent(event))
            .isInstanceOf(MessagingException.class)
            .hasMessageContaining("Error publishing event to SQS");
    }

    private BudgetApprovedEvent createBudgetApprovedEvent() {
        return BudgetApprovedEvent.builder()
            .eventId("EVT-001")
            .budgetId("BUDGET-001")
            .serviceOrderId("ORDER-001")
            .customerId("CUST-001")
            .vehicleId("VEH-001")
            .totalAmount(new BigDecimal("100.00"))
            .approvedAt(LocalDateTime.now())
            .timestamp(LocalDateTime.now())
            .build();
    }
}

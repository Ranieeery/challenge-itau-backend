package challenge.dev.raniery.itaubackend.controller;

import challenge.dev.raniery.itaubackend.dto.TransactionRequest;
import challenge.dev.raniery.itaubackend.model.Transaction;
import challenge.dev.raniery.itaubackend.service.TransactionalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController Tests")
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionalService transactionalService;

    @Nested
    @DisplayName("POST /transacao Tests")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should return 201 when creating valid transaction")
        void shouldReturn201WhenCreatingValidTransaction() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusMinutes(5)
            );

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            verify(transactionalService, times(1)).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 201 when creating transaction with zero value")
        void shouldReturn201WhenCreatingTransactionWithZeroValue() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.ZERO,
                OffsetDateTime.now().minusMinutes(1)
            );
            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            verify(transactionalService, times(1)).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when transaction is in the future")
        void shouldReturn422WhenTransactionIsInTheFuture() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().plusMinutes(5)
            );

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when valor is negative")
        void shouldReturn422WhenValueIsNegative() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(-10.50),
                OffsetDateTime.now().minusMinutes(5)
            );

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when valor is null")
        void shouldReturn422WhenValueIsNull() throws Exception {

            String jsonWithNullValue = """
                {
                    "valor": null,
                    "dataHora": "%s"
                }
                """.formatted(OffsetDateTime.now().minusMinutes(5));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithNullValue))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when dataHora is null")
        void shouldReturn422WhenDataHoraIsNull() throws Exception {

            String jsonWithNullDataHora = """
                {
                    "valor": 100.50,
                    "dataHora": null
                }
                """;

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithNullDataHora))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when request body is missing required fields")
        void shouldReturn422WhenRequestBodyIsMissingRequiredFields() throws Exception {

            String incompleteJson = """
                {
                    "valor": 100.50
                }
                """;

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(incompleteJson))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when JSON is malformed")
        void shouldReturn422WhenJsonIsMalformed() throws Exception {

            String malformedJson = """
                {
                    "valor": 100.50,
                    "dataHora": "invalid-date-format"
                }
                """;

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when JSON is completely invalid")
        void shouldReturn422WhenJsonIsCompletelyInvalid() throws Exception {

            String invalidJson = "{ invalid json }";

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andExpect(status().isUnprocessableEntity());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should return 422 when Content-Type is not application/json")
        void shouldReturn422WhenContentTypeIsNotJson() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusMinutes(5)
            );
            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

            verify(transactionalService, never()).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle transaction with very large valid value")
        void shouldHandleTransactionWithVeryLargeValidValue() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(999999999.99),
                OffsetDateTime.now().minusMinutes(1)
            );

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            verify(transactionalService, times(1)).addTransaction(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle transaction with very small valid value")
        void shouldHandleTransactionWithVerySmallValidValue() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(0.01),
                OffsetDateTime.now().minusMinutes(1)
            );

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            verify(transactionalService, times(1)).addTransaction(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("DELETE /transacao Tests")
    class DeleteTransactionsTests {

        @Test
        @DisplayName("Should return 200 when deleting all transactions")
        void shouldReturn200WhenDeletingAllTransactions() throws Exception {
            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            verify(transactionalService, times(1)).clearTransactions();
        }

        @Test
        @DisplayName("Should return 200 when deleting transactions even when no transactions exist")
        void shouldReturn200WhenDeletingTransactionsEvenWhenNoTransactionsExist() throws Exception {
            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            verify(transactionalService, times(1)).clearTransactions();
        }

        @Test
        @DisplayName("Should handle multiple delete requests successfully")
        void shouldHandleMultipleDeleteRequestsSuccessfully() throws Exception {
            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            verify(transactionalService, times(2)).clearTransactions();
        }
    }
}

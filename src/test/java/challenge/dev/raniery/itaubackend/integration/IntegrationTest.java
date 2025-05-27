package challenge.dev.raniery.itaubackend.integration;

import challenge.dev.raniery.itaubackend.dto.TransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Integration Tests")
public class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        try {
            mockMvc.perform(delete("/transacao"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Nested
    @DisplayName("Complete Transaction Flow Tests")
    class CompleteTransactionFlowTests {

        @Test
        @DisplayName("Should handle complete transaction flow successfully")
        void shouldHandleCompleteTransactionFlowSuccessfully() throws Exception {

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.sum").value(0.0))
                .andExpect(jsonPath("$.avg").value(0.0))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(0.0));

            TransactionRequest request1 = new TransactionRequest(
                BigDecimal.valueOf(100.50),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(100.50))
                .andExpect(jsonPath("$.avg").value(100.50))
                .andExpect(jsonPath("$.min").value(100.50))
                .andExpect(jsonPath("$.max").value(100.50));

            TransactionRequest request2 = new TransactionRequest(
                BigDecimal.valueOf(200.75),
                OffsetDateTime.now().minusSeconds(45));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.sum").value(301.25))
                .andExpect(jsonPath("$.avg").value(150.625))
                .andExpect(jsonPath("$.min").value(100.50))
                .andExpect(jsonPath("$.max").value(200.75));

            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.sum").value(0.0))
                .andExpect(jsonPath("$.avg").value(0.0))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(0.0));
        }

        @Test
        @DisplayName("Should only include transactions from last 60 seconds in statistics")
        void shouldOnlyIncludeTransactionsFromLast60SecondsInStatistics() throws Exception {

            TransactionRequest oldRequest = new TransactionRequest(
                BigDecimal.valueOf(1000.00),
                OffsetDateTime.now().minusSeconds(70));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(oldRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.sum").value(0.0));

            TransactionRequest recentRequest = new TransactionRequest(
                BigDecimal.valueOf(500.00),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(recentRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(500.00))
                .andExpect(jsonPath("$.avg").value(500.00))
                .andExpect(jsonPath("$.min").value(500.00))
                .andExpect(jsonPath("$.max").value(500.00));
        }

        @Test
        @DisplayName("Should handle zero value transactions correctly")
        void shouldHandleZeroValueTransactionsCorrectly() throws Exception {

            TransactionRequest zeroRequest = new TransactionRequest(
                BigDecimal.ZERO,
                OffsetDateTime.now().minusSeconds(30));

            TransactionRequest normalRequest = new TransactionRequest(
                BigDecimal.valueOf(100.00),
                OffsetDateTime.now().minusSeconds(20));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(zeroRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(normalRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.sum").value(100.00))
                .andExpect(jsonPath("$.avg").value(50.00))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(100.00));
        }
    }

    @Nested
    @DisplayName("Error Handling Integration Tests")
    class ErrorHandlingIntegrationTests {

        @Test
        @DisplayName("Should reject future transactions with 422")
        void shouldRejectFutureTransactionsWith422() throws Exception {

            TransactionRequest futureRequest = new TransactionRequest(
                BigDecimal.valueOf(100.00),
                OffsetDateTime.now().plusMinutes(5));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(futureRequest)))
                .andExpect(status().isUnprocessableEntity());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("Should reject negative value transactions with 422")
        void shouldRejectNegativeValueTransactionsWith422() throws Exception {

            TransactionRequest negativeRequest = new TransactionRequest(
                BigDecimal.valueOf(-10.50),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(negativeRequest)))
                .andExpect(status().isUnprocessableEntity());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("Should reject malformed JSON with 422")
        void shouldRejectMalformedJsonWith422() throws Exception {

            String malformedJson = "{ invalid json }";

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                .andExpect(status().isUnprocessableEntity());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        }

        @Test
        @DisplayName("Should reject requests with missing fields with 422")
        void shouldRejectRequestsWithMissingFieldsWith422() throws Exception {

            String incompleteJson = """
                {
                    "valor": 100.50
                }
                """;

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(incompleteJson))
                .andExpect(status().isUnprocessableEntity());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        }
    }

    @Nested
    @DisplayName("Concurrent Access Tests")
    class ConcurrentAccessTests {

        @Test
        @DisplayName("Should handle concurrent transaction creation and statistics retrieval")
        void shouldHandleConcurrentTransactionCreationAndStatisticsRetrieval() throws Exception {

            for (int i = 0; i < 10; i++) {
                TransactionRequest request = new TransactionRequest(
                    BigDecimal.valueOf(i * 10.0),
                    OffsetDateTime.now().minusSeconds(30));

                mockMvc.perform(post("/transacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
            }

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(10))

                .andExpect(jsonPath("$.avg").value(45.0))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(90.0));
        }

        @Test
        @DisplayName("Should handle multiple delete operations gracefully")
        void shouldHandleMultipleDeleteOperationsGracefully() throws Exception {

            TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100.00),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            mockMvc.perform(delete("/transacao"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large transaction values")
        void shouldHandleVeryLargeTransactionValues() throws Exception {

            TransactionRequest largeRequest = new TransactionRequest(
                BigDecimal.valueOf(999999999.99),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(largeRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(999999999.99))
                .andExpect(jsonPath("$.avg").value(999999999.99));
        }

        @Test
        @DisplayName("Should handle very small transaction values")
        void shouldHandleVerySmallTransactionValues() throws Exception {

            TransactionRequest smallRequest = new TransactionRequest(
                BigDecimal.valueOf(0.01),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(smallRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(0.01))
                .andExpect(jsonPath("$.avg").value(0.01));
        }

        @Test
        @DisplayName("Should handle transactions with high precision decimal values")
        void shouldHandleTransactionsWithHighPrecisionDecimalValues() throws Exception {

            TransactionRequest preciseRequest = new TransactionRequest(
                new BigDecimal("123.456789"),
                OffsetDateTime.now().minusSeconds(30));

            mockMvc.perform(post("/transacao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(preciseRequest)))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(123.456789))
                .andExpect(jsonPath("$.avg").value(123.456789));
        }
    }
}

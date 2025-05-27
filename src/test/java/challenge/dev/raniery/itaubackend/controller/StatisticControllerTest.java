package challenge.dev.raniery.itaubackend.controller;

import challenge.dev.raniery.itaubackend.service.TransactionalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.DoubleSummaryStatistics;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatisticController.class)
@DisplayName("StatisticController Tests")
public class StatisticControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionalService transactionalService;

    @Nested
    @DisplayName("GET /estatistica Tests")
    class GetStatisticsTests {

        @Test
        @DisplayName("Should return 200 with statistics when transactions exist in last 60 seconds")
        void shouldReturn200WithStatisticsWhenTransactionsExistInLast60Seconds() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(100.0);
            stats.accept(200.0);
            stats.accept(50.0);

            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.sum").value(350.0))
                .andExpect(jsonPath("$.avg").value(116.66666666666667))
                .andExpect(jsonPath("$.min").value(50.0))
                .andExpect(jsonPath("$.max").value(200.0));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with zero statistics when no transactions exist in last 60 seconds")
        void shouldReturn200WithZeroStatisticsWhenNoTransactionsExistInLast60Seconds() throws Exception {

            DoubleSummaryStatistics emptyStats = new DoubleSummaryStatistics();
            when(transactionalService.getStatistics()).thenReturn(emptyStats);
            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.sum").value(0.0))
                .andExpect(jsonPath("$.avg").value(0.0))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(0.0));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with statistics for single transaction")
        void shouldReturn200WithStatisticsForSingleTransaction() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(123.45);

            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.sum").value(123.45))
                .andExpect(jsonPath("$.avg").value(123.45))
                .andExpect(jsonPath("$.min").value(123.45))
                .andExpect(jsonPath("$.max").value(123.45));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with statistics for multiple same value transactions")
        void shouldReturn200WithStatisticsForMultipleSameValueTransactions() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(100.0);
            stats.accept(100.0);
            stats.accept(100.0);

            when(transactionalService.getStatistics()).thenReturn(stats);
            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.sum").value(300.0))
                .andExpect(jsonPath("$.avg").value(100.0))
                .andExpect(jsonPath("$.min").value(100.0))
                .andExpect(jsonPath("$.max").value(100.0));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with statistics including zero value transactions")
        void shouldReturn200WithStatisticsIncludingZeroValueTransactions() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(0.0);
            stats.accept(50.0);
            stats.accept(100.0);

            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.sum").value(150.0))
                .andExpect(jsonPath("$.avg").value(50.0))
                .andExpect(jsonPath("$.min").value(0.0))
                .andExpect(jsonPath("$.max").value(100.0));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with statistics for very large numbers")
        void shouldReturn200WithStatisticsForVeryLargeNumbers() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(999999999.99);
            stats.accept(888888888.88);

            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.sum").value(1888888888.87))
                .andExpect(jsonPath("$.avg").value(944444444.435))
                .andExpect(jsonPath("$.min").value(888888888.88))
                .andExpect(jsonPath("$.max").value(999999999.99));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should return 200 with statistics for very small numbers")
        void shouldReturn200WithStatisticsForVerySmallNumbers() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(0.01);
            stats.accept(0.02);
            stats.accept(0.03);

            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.sum").value(0.06))
                .andExpect(jsonPath("$.avg").value(0.02))
                .andExpect(jsonPath("$.min").value(0.01))
                .andExpect(jsonPath("$.max").value(0.03));

            verify(transactionalService, times(1)).getStatistics();
        }

        @Test
        @DisplayName("Should handle multiple consecutive requests successfully")
        void shouldHandleMultipleConsecutiveRequestsSuccessfully() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            stats.accept(100.0);
            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));

            verify(transactionalService, times(2)).getStatistics();
        }

        @Test
        @DisplayName("Should return correct content type header")
        void shouldReturnCorrectContentTypeHeader() throws Exception {

            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            when(transactionalService.getStatistics()).thenReturn(stats);

            mockMvc.perform(get("/estatistica"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));

            verify(transactionalService, times(1)).getStatistics();
        }
    }
}

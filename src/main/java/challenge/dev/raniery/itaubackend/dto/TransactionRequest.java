package challenge.dev.raniery.itaubackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionRequest(

    @NotNull
    @Min(0)
    BigDecimal valor,

    @NotNull
    @PastOrPresent
    OffsetDateTime dataHora) {

}

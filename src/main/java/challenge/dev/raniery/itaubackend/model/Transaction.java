package challenge.dev.raniery.itaubackend.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Transaction {

    private final BigDecimal valor;
    private final OffsetDateTime dataHora;

    public Transaction(BigDecimal valor, OffsetDateTime dataHora) {
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Double getValor() {
        return valor.doubleValue();
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }
}

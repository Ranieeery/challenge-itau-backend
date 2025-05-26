package challenge.dev.raniery.itaubackend.controller;

import challenge.dev.raniery.itaubackend.dto.TransactionRequest;
import challenge.dev.raniery.itaubackend.model.Transaction;
import challenge.dev.raniery.itaubackend.service.TransactionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

//TODO: exception handler HttpMessageNotReadableException
//  422 Unprocessable Entity sem nenhum corpo
//  A transação não foi aceita por qualquer motivo (1 ou mais dos critérios de aceite não foram atendidos - por exemplo: uma transação com valor menor que 0)
@RestController
@RequestMapping("/transacao")
public class TransacaoController {

    private final TransactionalService transacaoService;

    public TransacaoController(TransactionalService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid TransactionRequest request) {

        if (request.dataHora().isAfter(OffsetDateTime.now())) {
            return ResponseEntity.unprocessableEntity().build();
        }

        transacaoService.addTransaction(new Transaction(request.valor(), request.dataHora()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTransaction(@RequestBody @Valid TransactionRequest request) {

        transacaoService.clearTransactions();
        return ResponseEntity.ok().build();
    }
}

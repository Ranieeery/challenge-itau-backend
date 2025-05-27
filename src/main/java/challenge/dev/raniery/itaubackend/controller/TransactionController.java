package challenge.dev.raniery.itaubackend.controller;

import challenge.dev.raniery.itaubackend.docs.TransactionControllerInterface;
import challenge.dev.raniery.itaubackend.dto.TransactionRequest;
import challenge.dev.raniery.itaubackend.model.Transaction;
import challenge.dev.raniery.itaubackend.service.TransactionalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/transacao")
public class TransactionController implements TransactionControllerInterface {

    private final TransactionalService transacaoService;

    public TransactionController(TransactionalService transacaoService) {
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
    public ResponseEntity<Void> deleteTransactions() {
        transacaoService.clearTransactions();
        return ResponseEntity.ok().build();
    }
}

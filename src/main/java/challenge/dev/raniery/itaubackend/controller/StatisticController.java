package challenge.dev.raniery.itaubackend.controller;

import challenge.dev.raniery.itaubackend.docs.StatisticControllerInterface;
import challenge.dev.raniery.itaubackend.dto.StatisticResponse;
import challenge.dev.raniery.itaubackend.service.TransactionalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estatistica")
public class StatisticController implements StatisticControllerInterface {

    private final TransactionalService transactionalService;

    public StatisticController(TransactionalService transactionalService) {
        this.transactionalService = transactionalService;
    }

    @GetMapping
    public ResponseEntity<StatisticResponse> getStatistic() {
        return ResponseEntity.ok(new StatisticResponse(transactionalService.getStatistics()));
    }
}

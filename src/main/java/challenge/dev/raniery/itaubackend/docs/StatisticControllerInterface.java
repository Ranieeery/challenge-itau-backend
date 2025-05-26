package challenge.dev.raniery.itaubackend.docs;

import challenge.dev.raniery.itaubackend.dto.StatisticResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Statistic", description = "Controller for statistics")
public interface StatisticControllerInterface {

    @Operation(
        summary = "Get Application Statistics",
        description = "Fetches the statistics of the application in the last 60 seconds"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns the statistics of the application in the last 60 seconds",
        content = @Content(schema = @Schema(implementation = StatisticResponse.class))
    )
    public ResponseEntity<StatisticResponse> getStatistic();
}

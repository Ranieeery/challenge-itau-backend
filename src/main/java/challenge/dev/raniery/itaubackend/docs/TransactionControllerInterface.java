package challenge.dev.raniery.itaubackend.docs;

import challenge.dev.raniery.itaubackend.dto.TransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Transaction", description = "Controller for transactions")
public interface TransactionControllerInterface {

    @Operation(
        summary = "Create Transaction",
        description = "Creates a new transaction in the system. The transaction must be valid and adhere to the defined schema."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = TransactionRequest.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid transaction data provided",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - The transaction could not be processed due to validation errors",
            content = @Content
        )
    })
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid TransactionRequest request);

    @Operation(
        summary = "Delete Transactions",
        description = "Deletes transactions from the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Transactions deleted successfully",
        content = @Content(schema = @Schema(implementation = TransactionRequest.class))
    )
    public ResponseEntity<Void> deleteTransaction(@RequestBody @Valid TransactionRequest request);
}

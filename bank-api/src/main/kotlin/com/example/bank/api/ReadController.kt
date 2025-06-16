package com.example.bank.api

import com.example.bank.common.ApiResponse
import com.example.bank.domain.dto.AccountBalanceView
import com.example.bank.domain.dto.AccountView
import com.example.bank.domain.dto.TransactionView
import com.example.bank.service.AccountReadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/accounts/read")
@Tag(name = "Account Read API", description = "Account read operations")
class ReadController(
    private val accountReadService: AccountReadService
) {
    private val logger = LoggerFactory.getLogger(ReadController::class.java)

    @Operation(
        summary = "Get account details",
        description = "Retrieves detailed information about a specific account",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Account found successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountView::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Account not found"
            )
        ]
    )
    @GetMapping("/{accountNumber}")
    fun getAccount(
        @Parameter(description = "Account number to retrieve", required = true)
        @PathVariable accountNumber: String
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Getting account: $accountNumber")
        return accountReadService.getAccount(accountNumber)
    }

    @Operation(
        summary = "Get transaction history",
        description = "Retrieves the transaction history for a specific account",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Transaction history retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = TransactionView::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Account not found"
            )
        ]
    )
    @GetMapping("/{accountNumber}/transactions")
    fun getTransactionHistory(
        @Parameter(description = "Account number to retrieve transactions for", required = true)
        @PathVariable accountNumber: String,
        @Parameter(description = "Maximum number of transactions to return")
        @RequestParam(required = false) limit: Int?
    ): ResponseEntity<ApiResponse<List<TransactionView>>> {
        logger.info("Getting transaction history for account: $accountNumber")
        return accountReadService.transactionHistory(accountNumber, limit)
    }

    @Operation(
        summary = "Get account balance",
        description = "Retrieves the current balance for a specific account",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Account balance retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountBalanceView::class))]
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "Account not found"
            )
        ]
    )
    @GetMapping("/{accountNumber}/balance")
    fun getAccountBalance(
        @Parameter(description = "Account number to retrieve balance for", required = true)
        @PathVariable accountNumber: String
    ): ResponseEntity<ApiResponse<AccountBalanceView>> {
        logger.info("Getting balance for account: $accountNumber")
        return accountReadService.accountBalance(accountNumber)
    }

    @Operation(
        summary = "Get all accounts",
        description = "Retrieves a list of all accounts in the system",
        responses = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "Accounts retrieved successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountView::class))]
            )
        ]
    )
    @GetMapping
    fun getAllAccounts(): ResponseEntity<ApiResponse<List<AccountView>>> {
        logger.info("Getting all accounts")
        return accountReadService.allAccount()
    }
}

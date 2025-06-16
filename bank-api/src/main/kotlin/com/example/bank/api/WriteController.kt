package com.example.bank.api

import com.example.bank.common.ApiResponse
import com.example.bank.domain.dto.AccountView
import com.example.bank.service.AccountWriteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/accounts/write")
@Tag(name = "Account Write API", description = "Account write operations")
class WriteController(
    private val accountService: AccountWriteService
) {
    private val logger = LoggerFactory.getLogger(WriteController::class.java)

    @Operation(
        summary = "Create new account",
        description = "Creates a new bank account with the specified holder name and initial balance",
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Account created successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountView::class))]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid input parameters"
            )
        ]
    )
    @PostMapping
    fun createAccount(
        @Parameter(description = "Name of the account holder", required = true)
        @RequestParam accountHolderName: String,
        @Parameter(description = "Initial balance for the account", required = true)
        @RequestParam initialBalance: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Creating account for: $accountHolderName with initial balance: $initialBalance")
        return accountService.createAccount(accountHolderName, initialBalance)
    }

    @Operation(
        summary = "Deposit money",
        description = "Deposits the specified amount into the account",
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Deposit completed successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountView::class))]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Account not found"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid amount"
            )
        ]
    )
    @PostMapping("/{accountNumber}/deposit")
    fun deposit(
        @Parameter(description = "Account number to deposit to", required = true)
        @PathVariable accountNumber: String,
        @Parameter(description = "Amount to deposit", required = true)
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Depositing $amount to account: $accountNumber")
        return accountService.deposit(accountNumber, amount)
    }

    @Operation(
        summary = "Withdraw money",
        description = "Withdraws the specified amount from the account",
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Withdrawal completed successfully",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = AccountView::class))]
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Account not found"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid amount or insufficient funds"
            )
        ]
    )
    @PostMapping("/{accountNumber}/withdraw")
    fun withdraw(
        @Parameter(description = "Account number to withdraw from", required = true)
        @PathVariable accountNumber: String,
        @Parameter(description = "Amount to withdraw", required = true)
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Withdrawing $amount from account: $accountNumber")
        return accountService.withDraw(accountNumber, amount)
    }

    @Operation(
        summary = "Transfer money",
        description = "Transfers the specified amount from one account to another",
        responses = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Transfer completed successfully"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Account not found"
            ),
            io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid amount or insufficient funds"
            )
        ]
    )
    @PostMapping("/transfer")
    fun transfer(
        @Parameter(description = "Source account number", required = true)
        @RequestParam fromAccountNumber: String,
        @Parameter(description = "Destination account number", required = true)
        @RequestParam toAccountNumber: String,
        @Parameter(description = "Amount to transfer", required = true)
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Transferring $amount from $fromAccountNumber to $toAccountNumber")
        return accountService.transfer(fromAccountNumber, toAccountNumber, amount)
    }
}
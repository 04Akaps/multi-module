package com.example.bank.api

import com.example.bank.common.ApiResponse
import com.example.bank.domain.dto.AccountView
import com.example.bank.service.AccountWriteService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/accounts/write")
class WriteController(
    private val accountService: AccountWriteService
) {
    private val logger = LoggerFactory.getLogger(WriteController::class.java)

    @PostMapping
    fun createAccount(
        @RequestParam accountHolderName: String,
        @RequestParam initialBalance: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Creating account for: $accountHolderName with initial balance: $initialBalance")
        return accountService.createAccount(accountHolderName, initialBalance)
    }

    @PostMapping("/{accountNumber}/deposit")
    fun deposit(
        @PathVariable accountNumber: String,
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Depositing $amount to account: $accountNumber")
        return accountService.deposit(accountNumber, amount)
    }

    @PostMapping("/{accountNumber}/withdraw")
    fun withdraw(
        @PathVariable accountNumber: String,
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Withdrawing $amount from account: $accountNumber")
        return accountService.withDraw(accountNumber, amount)
    }

    @PostMapping("/transfer")
    fun transfer(
        @RequestParam fromAccountNumber: String,
        @RequestParam toAccountNumber: String,
        @RequestParam amount: BigDecimal
    ): ResponseEntity<ApiResponse<String>> {
        logger.info("Transferring $amount from $fromAccountNumber to $toAccountNumber")
        return accountService.transfer(fromAccountNumber, toAccountNumber, amount)
    }
}
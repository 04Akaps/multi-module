package com.example.bank.api

import com.example.bank.common.ApiResponse
import com.example.bank.domain.dto.AccountBalanceView
import com.example.bank.domain.dto.AccountView
import com.example.bank.domain.dto.TransactionView
import com.example.bank.service.AccountReadService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts/write")
class ReadController(
    private val accountReadService: AccountReadService
) {
    private val logger = LoggerFactory.getLogger(ReadController::class.java)

    @GetMapping("/{accountNumber}")
    fun getAccount(@PathVariable accountNumber: String): ResponseEntity<ApiResponse<AccountView>> {
        logger.info("Getting account: $accountNumber")
        return accountReadService.getAccount(accountNumber)
    }

    @GetMapping("/{accountNumber}/transactions")
    fun getTransactionHistory(
        @PathVariable accountNumber: String,
        @RequestParam(required = false) limit: Int?
    ): ResponseEntity<ApiResponse<List<TransactionView>>> {
        logger.info("Getting transaction history for account: $accountNumber")
        return accountReadService.transactionHistory(accountNumber, limit)
    }

    @GetMapping("/{accountNumber}/balance")
    fun getAccountBalance(@PathVariable accountNumber: String): ResponseEntity<ApiResponse<AccountBalanceView>> {
        logger.info("Getting balance for account: $accountNumber")
        return accountReadService.accountBalance(accountNumber)
    }

    @GetMapping
    fun getAllAccounts(): ResponseEntity<ApiResponse<List<AccountView>>> {
        logger.info("Getting all accounts")
        return accountReadService.allAccount()
    }
}

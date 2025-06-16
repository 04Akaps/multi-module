package com.example.bank.service

import com.example.bank.common.ApiResponse
import com.example.bank.domain.dto.AccountBalanceView
import com.example.bank.domain.dto.AccountView
import com.example.bank.domain.dto.TransactionView
import com.example.bank.domain.model.Account
import com.example.bank.domain.model.Transaction
import com.example.bank.domain.repository.AccountRepository
import com.example.bank.domain.repository.TransactionRepository
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.function.Supplier

@Service
class AccountReadService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    private val logger = LoggerFactory.getLogger(AccountReadService::class.java)
    private val circuitBreakerRegistry = CircuitBreakerRegistry.of(
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .permittedNumberOfCallsInHalfOpenState(2)
            .slidingWindowSize(2)
            .build()
    )

    private val accountServiceBreaker = circuitBreakerRegistry.circuitBreaker("accountReadService")

    @Transactional(readOnly = true)
    fun getAccount(accountNumber: String): ResponseEntity<ApiResponse<AccountView>> {
        return executeWithCircuitBreaker(accountServiceBreaker) {
            logger.info("Getting account: $accountNumber")
            val account = accountRepository.findByAccountNumber(accountNumber)
                ?: return@executeWithCircuitBreaker ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Account not found"))

            ResponseEntity.ok(ApiResponse.success(AccountView.from(account)))
        }
    }

    @Transactional(readOnly = true)
    fun transactionHistory(accountNumber: String, limit: Int?): ResponseEntity<ApiResponse<List<TransactionView>>> {
        return executeWithCircuitBreaker(accountServiceBreaker) {
            logger.info("Getting transaction history for account: $accountNumber")
            val account = accountRepository.findByAccountNumber(accountNumber)
                ?: return@executeWithCircuitBreaker ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Account not found"))

            val transactions = if (limit != null) {
                transactionRepository.findTopByAccountOrderByTimestampDesc(account, limit)
            } else {
                transactionRepository.findByAccountOrderByTimestampDesc(account)
            }

            ResponseEntity.ok(ApiResponse.success(transactions.map { TransactionView.from(it) }))
        }
    }

    @Transactional(readOnly = true)
    fun accountBalance(accountNumber: String): ResponseEntity<ApiResponse<AccountBalanceView>> {
        return executeWithCircuitBreaker(accountServiceBreaker) {
            logger.info("Getting balance for account: $accountNumber")
            val account = accountRepository.findByAccountNumber(accountNumber)
                ?: return@executeWithCircuitBreaker ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Account not found"))

            ResponseEntity.ok(ApiResponse.success(AccountBalanceView.from(account)))
        }
    }

    @Transactional(readOnly = true)
    fun allAccount(): ResponseEntity<ApiResponse<List<AccountView>>> {
        return executeWithCircuitBreaker(accountServiceBreaker) {
            logger.info("Getting all accounts")
            val accounts = accountRepository.findAll()
            ResponseEntity.ok(ApiResponse.success(accounts.map { AccountView.from(it) }))
        }
    }

    private fun <T> executeWithCircuitBreaker(
        circuitBreaker: CircuitBreaker,
        supplier: () -> ResponseEntity<ApiResponse<T>>
    ): ResponseEntity<ApiResponse<T>> {
        return try {
            CircuitBreaker.decorateSupplier(circuitBreaker, Supplier { supplier() }).get()
        } catch (e: Exception) {
            logger.error("Circuit breaker triggered for operation", e)
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Service temporarily unavailable"))
        }
    }
}
package com.example.bank.service

import com.example.bank.common.ApiResponse
import com.example.bank.common.TxAdvice
import com.example.bank.domain.dto.AccountView
import com.example.bank.domain.entity.Account
import com.example.bank.domain.entity.Transaction
import com.example.bank.domain.entity.TransactionType
import com.example.bank.domain.event.AccountCreatedEvent
import com.example.bank.domain.event.TransactionCreatedEvent
import com.example.bank.domain.repository.AccountRepository
import com.example.bank.domain.repository.TransactionRepository
import com.example.bank.event.publisher.DomainEventPublisher
import com.example.bank.exception.AccountNotFoundException
import com.example.bank.exception.InsufficientFundsException
import com.example.bank.exception.InvalidAmountException
import com.example.bank.lock.DistributedLockService
import com.example.bank.monitoring.BankMetrics
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AccountWriteService(
    private val txAdvice: TxAdvice,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: DomainEventPublisher,
    private val bankMetrics: BankMetrics,
    private val distributedLockService: DistributedLockService
) {
    private val logger = LoggerFactory.getLogger(AccountWriteService::class.java)

    private fun generateAccountNumber(): String {
        return System.currentTimeMillis().toString()
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "createAccountFallback")
    @Retry(name = "accountService")
    fun createAccount(name: String, balance : BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return try {
            val account = txAdvice.run {
                val accountNumber = generateAccountNumber()
                val account = Account(
                    accountNumber = accountNumber,
                    balance = balance,
                    accountHolderName = name
                )
                accountRepository.save(account)
            }!!

            bankMetrics.incrementAccountCreated()
            bankMetrics.updateAccountCount(accountRepository.count())

            eventPublisher.publishAsync(
                AccountCreatedEvent(
                    accountId = account.id,
                    accountNumber = account.accountNumber,
                    accountHolderName = account.accountHolderName,
                    initialBalance = account.balance
                )
            )

            ApiResponse.success(
                data = AccountView(
                    id = account.id,
                    accountNumber = account.accountNumber,
                    balance = account.balance,
                    accountHolderName = account.accountHolderName,
                    createdAt = account.createdAt
                ),
                message = "Account created successfully"
            )
        } catch (e: Exception) {
            logger.error("Error creating account", e)
            ApiResponse.error<AccountView>(
                message = "Failed to create account: ${e.message}"
            )
        }
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "depositFallback")
    @Retry(name = "accountService")
    fun deposit(account : String, amount :BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return try {
            // 분산 락을 사용하여 동시성 제어
            val (updatedAccount, transaction) = distributedLockService.executeWithAccountLock(account) {
                txAdvice.run {
                    if (amount <= BigDecimal.ZERO) {
                        throw InvalidAmountException("Deposit amount must be positive")
                    }

                    val account = accountRepository.findByAccountNumber(account)
                        ?: throw AccountNotFoundException(account)

                    account.balance = account.balance.add(amount)

                    val transaction = Transaction(
                        account = account,
                        amount = amount,
                        type = TransactionType.DEPOSIT,
                        description = "Deposit of ${amount}"
                    )
                    val savedTransaction = transactionRepository.save(transaction)
                    val savedAccount = accountRepository.save(account)

                    Pair(savedAccount, savedTransaction)
                }!!
            }

            bankMetrics.incrementTransaction("DEPOSIT")
            bankMetrics.recordTransactionAmount(amount, "DEPOSIT")

            eventPublisher.publishAsync(
                TransactionCreatedEvent(
                    transactionId = transaction.id,
                    accountId = updatedAccount.id,
                    type = TransactionType.DEPOSIT,
                    amount = amount,
                    description = "Deposit of ${amount}",
                    balanceAfter = updatedAccount.balance
                )
            )

            ApiResponse.success(
                data = AccountView(
                    id = updatedAccount.id,
                    accountNumber = updatedAccount.accountNumber,
                    balance = updatedAccount.balance,
                    accountHolderName = updatedAccount.accountHolderName,
                    createdAt = updatedAccount.createdAt
                ),
                message = "Deposit completed successfully"
            )
        } catch (e: Exception) {
            logger.error("Error depositing to account: $account", e)
            ApiResponse.error<AccountView>(
                message = "Failed to deposit: ${e.message}"
            )
        }
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "withdrawFallback")
    @Retry(name = "accountService")
    fun withDraw(accountNumber : String, amount : BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return try {
            // 분산 락을 사용하여 동시성 제어
            val (updatedAccount, transaction) = distributedLockService.executeWithAccountLock(accountNumber) {
                txAdvice.run {
                    if (amount <= BigDecimal.ZERO) {
                        throw InvalidAmountException("Withdrawal amount must be positive")
                    }

                    val account = accountRepository.findByAccountNumber(accountNumber)
                        ?: throw AccountNotFoundException(accountNumber)

                    if (account.balance < amount) {
                        throw InsufficientFundsException(account.balance.toString(), amount.toString())
                    }

                    account.balance = account.balance.subtract(amount)

                    val transaction = Transaction(
                        account = account,
                        amount = amount,
                        type = TransactionType.WITHDRAWAL,
                        description = "Withdrawal of ${amount}"
                    )
                    val savedTransaction = transactionRepository.save(transaction)
                    val savedAccount = accountRepository.save(account)

                    Pair(savedAccount, savedTransaction)
                }!!
            }

            bankMetrics.incrementTransaction("WITHDRAWAL")
            bankMetrics.recordTransactionAmount(amount, "WITHDRAWAL")

            eventPublisher.publishAsync(
                TransactionCreatedEvent(
                    transactionId = transaction.id,
                    accountId = updatedAccount.id,
                    type = TransactionType.WITHDRAWAL,
                    amount = amount,
                    description = "Withdrawal of ${amount}",
                    balanceAfter = updatedAccount.balance
                )
            )
            
            ApiResponse.success(
                data = AccountView(
                    id = updatedAccount.id,
                    accountNumber = updatedAccount.accountNumber,
                    balance = updatedAccount.balance,
                    accountHolderName = updatedAccount.accountHolderName,
                    createdAt = updatedAccount.createdAt
                ),
                message = "Withdrawal completed successfully"
            )
        } catch (e: Exception) {
            logger.error("Error withdrawing from account: $accountNumber", e)
            ApiResponse.error<AccountView>(
                message = "Failed to withdraw: ${e.message}"
            )
        }
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "transferFallback")
    @Retry(name = "accountService")
    fun transfer(from : String, to : String, amount :BigDecimal) : ResponseEntity<ApiResponse<String>> {
        return try {
            // 송금 시 데드락 방지를 위한 분산 락 적용
            val result = distributedLockService.executeWithTransferLock(from, to) {
                txAdvice.run {
                    if (amount <= BigDecimal.ZERO) {
                        throw InvalidAmountException("Transfer amount must be positive")
                    }

                    val fromAccount = accountRepository.findByAccountNumber(from)
                        ?: throw AccountNotFoundException(from)
                    val toAccount = accountRepository.findByAccountNumber(to)
                        ?: throw AccountNotFoundException(to)

                    if (fromAccount.balance < amount) {
                        throw InsufficientFundsException(fromAccount.balance.toString(), amount.toString())
                    }

                    fromAccount.balance = fromAccount.balance.subtract(amount)
                    toAccount.balance = toAccount.balance.add(amount)

                    val fromTransaction = Transaction(
                        account = fromAccount,
                        amount = amount,
                        type = TransactionType.TRANSFER,
                        description = "Transfer to ${to}"
                    )

                    val toTransaction = Transaction(
                        account = toAccount,
                        amount = amount,
                        type = TransactionType.TRANSFER,
                        description = "Transfer from ${from}"
                    )

                    val savedFromTransaction = transactionRepository.save(fromTransaction)
                    val savedToTransaction = transactionRepository.save(toTransaction)

                    val savedFromAccount = accountRepository.save(fromAccount)
                    val savedToAccount = accountRepository.save(toAccount)

                    TransferResult(savedFromTransaction, savedToTransaction, savedFromAccount, savedToAccount)
                }!!
            }

            bankMetrics.incrementTransaction("TRANSFER")
            bankMetrics.incrementTransaction("TRANSFER")
            bankMetrics.recordTransactionAmount(amount, "TRANSFER")

            eventPublisher.publishAllAsync(
                listOf(
                    TransactionCreatedEvent(
                        transactionId = result.fromTransaction.id,
                        accountId = result.fromAccount.id,
                        type = TransactionType.TRANSFER,
                        amount = amount,
                        description = "Transfer to ${to}",
                        balanceAfter = result.fromAccount.balance
                    ),
                    TransactionCreatedEvent(
                        transactionId = result.toTransaction.id,
                        accountId = result.toAccount.id,
                        type = TransactionType.TRANSFER,
                        amount = amount,
                        description = "Transfer from ${from}",
                        balanceAfter = result.toAccount.balance
                    )
                )
            )
            
            ApiResponse.success(
                data = "Transfer completed",
                message = "Transfer completed successfully"
            )
        } catch (e: Exception) {
            logger.error("Error transferring from $from to $to", e)
            ApiResponse.error<String>(
                message = "Failed to transfer: ${e.message}"
            )
        }
    }

    private data class TransferResult(
        val fromTransaction: Transaction,
        val toTransaction: Transaction,
        val fromAccount: Account,
        val toAccount: Account
    )

    // Fallback methods
    private fun createAccountFallback(name: String, balance: BigDecimal, ex: Exception): ResponseEntity<ApiResponse<AccountView>> {
        logger.error("Fallback: Error creating account", ex)
        return ApiResponse.error<AccountView>(
            message = "Service temporarily unavailable. Please try again later."
        )
    }

    private fun depositFallback(accountNumber: String, amount: BigDecimal, ex: Exception): ResponseEntity<ApiResponse<AccountView>> {
        logger.error("Fallback: Error depositing to account", ex)
        return ApiResponse.error<AccountView>(
            message = "Service temporarily unavailable. Please try again later."
        )
    }

    private fun withdrawFallback(accountNumber: String, amount: BigDecimal, ex: Exception): ResponseEntity<ApiResponse<AccountView>> {
        logger.error("Fallback: Error withdrawing from account", ex)
        return ApiResponse.error<AccountView>(
            message = "Service temporarily unavailable. Please try again later."
        )
    }

    private fun transferFallback(from: String, to: String, amount: BigDecimal, ex: Exception): ResponseEntity<ApiResponse<String>> {
        logger.error("Fallback: Error transferring between accounts", ex)
        return ApiResponse.error<String>(
            message = "Service temporarily unavailable. Please try again later."
        )
    }
}


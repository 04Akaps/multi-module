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
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class AccountWriteService(
    private val txAdvice: TxAdvice,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: DomainEventPublisher
) {
    private val logger = LoggerFactory.getLogger(AccountWriteService::class.java)

    private fun generateAccountNumber(): String {
        return System.currentTimeMillis().toString()
    }

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

            eventPublisher.publish(
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

    fun deposit(account : String, amount :BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return try {

            val (updatedAccount, transaction) = txAdvice.run {
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

            eventPublisher.publish(
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
                data =AccountView(
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

    fun withDraw(accountNumber : String, amount : BigDecimal) : ResponseEntity<ApiResponse<AccountView>> {
        return try {

            val (updatedAccount, transaction) = txAdvice.run {
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

            eventPublisher.publish(
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

    fun transfer(from : String, to : String, amount :BigDecimal) : ResponseEntity<ApiResponse<String>> {
        return try {
            val result = txAdvice.run {
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

            eventPublisher.publish(
                TransactionCreatedEvent(
                    transactionId = result.fromTransaction.id,
                    accountId = result.fromAccount.id,
                    type = TransactionType.TRANSFER,
                    amount = amount,
                    description = "Transfer to ${to}",
                    balanceAfter = result.fromAccount.balance
                )
            )

            eventPublisher.publish(
                TransactionCreatedEvent(
                    transactionId = result.toTransaction.id,
                    accountId = result.toAccount.id,
                    type = TransactionType.TRANSFER,
                    amount = amount,
                    description = "Transfer from ${from}",
                    balanceAfter = result.toAccount.balance
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
}


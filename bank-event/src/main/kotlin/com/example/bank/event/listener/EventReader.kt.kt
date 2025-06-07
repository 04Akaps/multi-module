package com.example.bank.event.listener

import com.example.bank.common.TxAdvice
import com.example.bank.domain.entity.AccountReadView
import com.example.bank.domain.entity.TransactionReadView
import com.example.bank.domain.event.AccountCreatedEvent
import com.example.bank.domain.event.TransactionCreatedEvent
import com.example.bank.domain.repository.AccountReadViewRepository
import com.example.bank.domain.repository.AccountRepository
import com.example.bank.domain.repository.TransactionReadViewRepository
import com.example.bank.domain.repository.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime

@Component
open class EventReader(
    private val accountReadViewRepository: AccountReadViewRepository,
    private val transactionReadViewRepository: TransactionReadViewRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val txAdvice: TxAdvice
) {
    private val logger = LoggerFactory.getLogger(EventReader::class.java)

    @EventListener
    open fun handleAccountCreated(event: AccountCreatedEvent) {
        logger.info("Projecting AccountCreatedEvent to read model: accountId=${event.accountId}")

        try {
            txAdvice.runNewTransaction {
                val account = accountRepository.findById(event.accountId)
                    .orElseThrow { IllegalStateException("Account not found: ${event.accountId}") }

                val accountReadView = AccountReadView(
                    id = account.id,
                    accountNumber = account.accountNumber,
                    accountHolderName = account.accountHolderName,
                    balance = account.balance,
                    createdAt = account.createdAt,
                    lastUpdatedAt = LocalDateTime.now(),
                    transactionCount = 0,
                    totalDeposits = BigDecimal.ZERO,
                    totalWithdrawals = BigDecimal.ZERO
                )

                accountReadViewRepository.save(accountReadView)
                logger.info("AccountReadView created successfully: ${accountReadView.accountNumber}")
            }
        } catch (e: Exception) {
            logger.error("Failed to project AccountCreatedEvent: ${event.accountId}", e)
            throw e
        }
    }

    @EventListener
    open fun handleTransactionCreated(event: TransactionCreatedEvent) {
        logger.info("Projecting TransactionCreatedEvent to read model: transactionId=${event.transactionId}")

        try {
            txAdvice.runNewTransaction {
                val transaction = transactionRepository.findById(event.transactionId)
                    .orElseThrow { IllegalStateException("Transaction not found: ${event.transactionId}") }

                val account = accountRepository.findById(event.accountId)
                    .orElseThrow { IllegalStateException("Account not found: ${event.accountId}") }

                // 1. TransactionReadView 생성
                val transactionReadView = TransactionReadView(
                    id = transaction.id,
                    accountId = event.accountId,
                    accountNumber = account.accountNumber,
                    accountHolderName = account.accountHolderName,
                    type = transaction.type,
                    amount = transaction.amount,
                    description = transaction.description,
                    createdAt = transaction.createdAt,
                    balanceAfter = account.balance // 거래 후 잔액
                )

                transactionReadViewRepository.save(transactionReadView)
                logger.info("TransactionReadView created successfully: ${transaction.id}")

                // 2. AccountReadView 업데이트
                val accountReadView = accountReadViewRepository.findById(account.id)
                    .orElseThrow { IllegalStateException("AccountReadView not found: ${account.id}") }

                val updatedAccountReadView = accountReadView.copy(
                    balance = account.balance,
                    lastUpdatedAt = LocalDateTime.now(),
                    transactionCount = accountReadView.transactionCount + 1,
                    totalDeposits = if (transaction.type.name.contains("DEPOSIT"))
                        accountReadView.totalDeposits + transaction.amount
                    else accountReadView.totalDeposits,
                    totalWithdrawals = if (transaction.type.name.contains("WITHDRAWAL"))
                        accountReadView.totalWithdrawals + transaction.amount
                    else accountReadView.totalWithdrawals
                )

                accountReadViewRepository.save(updatedAccountReadView)
                logger.info("AccountReadView updated successfully: ${account.accountNumber}")
            }
        } catch (e: Exception) {
            logger.error("Failed to project TransactionCreatedEvent: ${event.transactionId}", e)
            throw e
        }
    }
}
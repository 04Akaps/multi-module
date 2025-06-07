package com.example.bank.service

import com.example.bank.common.ApiResponse
import com.example.bank.common.TxAdvice
import com.example.bank.domain.dto.AccountBalanceView
import com.example.bank.domain.dto.AccountView
import com.example.bank.domain.dto.TransactionView
import com.example.bank.domain.repository.AccountReadViewRepository
import com.example.bank.domain.repository.TransactionReadViewRepository
import com.example.bank.exception.AccountNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class AccountReadService(
    private val accountReadViewRepository: AccountReadViewRepository,
    private val transactionReadViewRepository: TransactionReadViewRepository,
    private val txAdvice: TxAdvice,
) {
    private val logger = LoggerFactory.getLogger(AccountReadService::class.java)

    fun getAccount(account : String) : ResponseEntity<ApiResponse<AccountView>> {
        return try {

            val result = txAdvice.readOnly {
                val accountReadView = accountReadViewRepository.findByAccountNumber(account)
                    .orElseThrow { AccountNotFoundException(account) }

                AccountView(
                    id = accountReadView.id,
                    accountNumber = accountReadView.accountNumber,
                    balance = accountReadView.balance,
                    accountHolderName = accountReadView.accountHolderName,
                    createdAt = accountReadView.createdAt
                )
            }!!

            ApiResponse.success(
                data = result,
                message = "Account retrieved successfully"
            )
        } catch (e: Exception) {
            logger.error("Error getting account: $account", e)
            ApiResponse.error<AccountView>(
                message = "Failed to get account: ${e.message}"
            )
        }
    }

    fun transactionHistory(account : String, limit : Int?): ResponseEntity<ApiResponse<List<TransactionView>>>  {
        return try {

            val result = txAdvice.readOnly {
                val transactionReadViews = transactionReadViewRepository.findByAccountNumberOrderByCreatedAtDesc(account)
                    .let { if (limit != null) it.take(limit) else it }

                transactionReadViews.map { transactionReadView ->
                    TransactionView(
                        id = transactionReadView.id,
                        accountId = transactionReadView.accountId,
                        accountNumber = transactionReadView.accountNumber,
                        amount = transactionReadView.amount,
                        type = transactionReadView.type,
                        description = transactionReadView.description,
                        createdAt = transactionReadView.createdAt,
                        balanceAfter = transactionReadView.balanceAfter
                    )
                }
            }!!

            ApiResponse.success(
                data = result,
                message = "Transaction history retrieved successfully"
            )
        } catch (e: Exception) {
            logger.error("Error getting transaction history for account: $account", e)
            ApiResponse.error<List<TransactionView>>(
                message = "Failed to get transaction history: ${e.message}"
            )
        }
    }

    fun accountBalance(account : String) : ResponseEntity<ApiResponse<AccountBalanceView>> {
        return try {

            val result =  txAdvice.readOnly {
                val accountReadView = accountReadViewRepository.findByAccountNumber(account)
                    .orElseThrow { AccountNotFoundException(account) }

                AccountBalanceView(
                    accountNumber = accountReadView.accountNumber,
                    balance = accountReadView.balance,
                    accountHolderName = accountReadView.accountHolderName,
                    lastUpdated = accountReadView.lastUpdatedAt
                )
            }!!

            ApiResponse.success(
                data = result,
                message = "Account balance retrieved successfully"
            )
        } catch (e: Exception) {
            logger.error("Error getting balance for account: $account", e)
            ApiResponse.error<AccountBalanceView>(
                message = "Failed to get account balance: ${e.message}"
            )
        }
    }

    fun allAccount() : ResponseEntity<ApiResponse<List<AccountView>>> {
        return try {

            val result =  txAdvice.readOnly {
                accountReadViewRepository.findAll().map { accountReadView ->
                    AccountView(
                        id = accountReadView.id,
                        accountNumber = accountReadView.accountNumber,
                        balance = accountReadView.balance,
                        accountHolderName = accountReadView.accountHolderName,
                        createdAt = accountReadView.createdAt
                    )
                }
            }!!

            ApiResponse.success(
                data = result,
                message = "All accounts retrieved successfully"
            )
        } catch (e: Exception) {
            logger.error("Error getting all accounts", e)
            ApiResponse.error<List<AccountView>>(
                message = "Failed to get accounts: ${e.message}"
            )
        }
    }
}
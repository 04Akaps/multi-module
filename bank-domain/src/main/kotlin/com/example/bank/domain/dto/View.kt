package com.example.bank.domain.dto

import com.example.bank.domain.entity.TransactionType
import java.math.BigDecimal
import java.time.LocalDateTime

// View Models (읽기 전용 데이터 모델)
data class AccountView(
    val id: Long,
    val accountNumber: String,
    val balance: BigDecimal,
    val accountHolderName: String,
    val createdAt: LocalDateTime
)

data class TransactionView(
    val id: Long,
    val accountId: Long,
    val accountNumber: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String,
    val createdAt: LocalDateTime,
    val balanceAfter: BigDecimal
)

data class AccountBalanceView(
    val accountNumber: String,
    val balance: BigDecimal,
    val accountHolderName: String,
    val lastUpdated: LocalDateTime
)
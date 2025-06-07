package com.example.bank.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "account_read_views")
class AccountReadView(
    @Id
    val id: Long = 0,
    
    @Column(nullable = false)
    val accountNumber: String = "",
    
    @Column(nullable = false)
    val accountHolderName: String = "",
    
    @Column(nullable = false, precision = 19, scale = 2)
    val balance: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    val lastUpdatedAt: LocalDateTime = LocalDateTime.now(),
    
    // 읽기 최적화를 위한 추가 필드들
    @Column(nullable = false)
    val transactionCount: Int = 0,
    
    @Column(precision = 19, scale = 2)
    val totalDeposits: BigDecimal = BigDecimal.ZERO,
    
    @Column(precision = 19, scale = 2)
    val totalWithdrawals: BigDecimal = BigDecimal.ZERO
) {
    // JPA를 위한 기본 생성자
    constructor() : this(
        id = 0,
        accountNumber = "",
        accountHolderName = "",
        balance = BigDecimal.ZERO,
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        transactionCount = 0,
        totalDeposits = BigDecimal.ZERO,
        totalWithdrawals = BigDecimal.ZERO
    )
    
    fun copy(
        id: Long = this.id,
        accountNumber: String = this.accountNumber,
        accountHolderName: String = this.accountHolderName,
        balance: BigDecimal = this.balance,
        createdAt: LocalDateTime = this.createdAt,
        lastUpdatedAt: LocalDateTime = this.lastUpdatedAt,
        transactionCount: Int = this.transactionCount,
        totalDeposits: BigDecimal = this.totalDeposits,
        totalWithdrawals: BigDecimal = this.totalWithdrawals
    ): AccountReadView {
        return AccountReadView(
            id = id,
            accountNumber = accountNumber,
            accountHolderName = accountHolderName,
            balance = balance,
            createdAt = createdAt,
            lastUpdatedAt = lastUpdatedAt,
            transactionCount = transactionCount,
            totalDeposits = totalDeposits,
            totalWithdrawals = totalWithdrawals
        )
    }
} 
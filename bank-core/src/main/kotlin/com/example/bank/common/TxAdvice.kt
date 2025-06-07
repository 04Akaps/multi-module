package com.example.bank.common

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface TransactionRunner {
    fun <T> run(function: () -> T?): T?
    fun <T> readOnly(function: () -> T?): T?
    // 새로운 tx 실행으로 기존 트랜잭션과 독립적인 tx 생성
    fun <T> runNewTransaction(function: () -> T?): T?
}

@Component
class TransactionAdvice : TransactionRunner {
    @Transactional
    override fun <T> run(function: () -> T?): T? = function()

    @Transactional(readOnly = true)
    override fun <T> readOnly(function: () -> T?): T? = function()

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun <T> runNewTransaction(function: () -> T?): T? = function()
}

@Component
class TxAdvice(
    private val advice: TransactionAdvice
) {
    fun <T> run(function: () -> T?): T? = advice.run(function)
    fun <T> readOnly(function: () -> T?): T? = advice.readOnly(function)
    fun <T> runNewTransaction(function: () -> T?): T? = advice.runNewTransaction(function)
} 
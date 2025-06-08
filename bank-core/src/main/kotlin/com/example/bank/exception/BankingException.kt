package com.example.bank.exception

/**
 * 은행 서비스 전용 기본 예외 클래스
 */
abstract class BankingException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 계좌를 찾을 수 없는 경우
 */
class AccountNotFoundException(
    accountNumber: String
) : BankingException("Account not found: $accountNumber")

/**
 * 잔액이 부족한 경우
 */
class InsufficientFundsException(
    availableBalance: String,
    requestedAmount: String
) : BankingException("Insufficient funds. Available: $availableBalance, Requested: $requestedAmount")

/**
 * 유효하지 않은 거래 금액인 경우
 */
class InvalidAmountException(
    message: String = "Invalid transaction amount"
) : BankingException(message)

/**
 * 계좌 번호가 중복되는 경우
 */
class DuplicateAccountNumberException(
    accountNumber: String
) : BankingException("Account number already exists: $accountNumber")

/**
 * 비즈니스 규칙 위반 예외
 */
class BusinessRuleViolationException(
    message: String
) : BankingException(message) 
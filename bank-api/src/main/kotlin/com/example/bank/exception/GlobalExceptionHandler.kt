package com.example.bank.exception

import com.example.bank.common.ApiResponse
import com.example.bank.common.FieldErrorDetail
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.sql.SQLException
import jakarta.persistence.EntityNotFoundException

// Bank-core 예외 클래스들 import (다른 모듈에서)

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFoundException(
        ex: AccountNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Account not found: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Account not found",
            errorCode = "ACCOUNT_NOT_FOUND",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(InsufficientFundsException::class)
    fun handleInsufficientFundsException(
        ex: InsufficientFundsException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Insufficient funds: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Insufficient funds for transaction",
            errorCode = "INSUFFICIENT_FUNDS",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(InvalidAmountException::class)
    fun handleInvalidAmountException(
        ex: InvalidAmountException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Invalid amount: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Invalid transaction amount",
            errorCode = "INVALID_AMOUNT",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(DuplicateAccountNumberException::class)
    fun handleDuplicateAccountNumberException(
        ex: DuplicateAccountNumberException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Duplicate account number: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Account number already exists",
            errorCode = "DUPLICATE_ACCOUNT_NUMBER",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(
        ex: BusinessRuleViolationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Business rule violation: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Business rule violation",
            errorCode = "BUSINESS_RULE_VIOLATION",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(BankingException::class)
    fun handleBankingException(
        ex: BankingException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Banking exception: {}", ex.message, ex)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Banking service errorException occurred",
            errorCode = "BANKING_SERVICE_ERROR",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Validation failed: {}", ex.message)
        
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldErrorDetail(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Invalid value"
            )
        }
        
        val response = ApiResponse.validationError<Nothing>(
            message = "Input validation failed",
            fieldErrors = fieldErrors,
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Missing required parameter: {}", ex.parameterName)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "Required parameter '${ex.parameterName}' is missing",
            errorCode = "MISSING_PARAMETER",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Method argument type mismatch: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "Invalid value for parameter '${ex.name}': ${ex.value}",
            errorCode = "INVALID_PARAMETER_TYPE",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("HTTP method not supported: {}", ex.method)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "HTTP method '${ex.method}' is not supported for this endpoint",
            errorCode = "METHOD_NOT_ALLOWED",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("No handler found: {} {}", ex.httpMethod, ex.requestURL)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "The requested resource was not found",
            errorCode = "NOT_FOUND",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(
        ex: DataIntegrityViolationException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Data integrity violation: {}", ex.message, ex)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "A data integrity constraint was violated",
            errorCode = "DATA_INTEGRITY_VIOLATION",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(
        ex: EntityNotFoundException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Entity not found: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Requested entity not found",
            errorCode = "ENTITY_NOT_FOUND",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("HTTP message not readable: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "Request body is malformed or missing",
            errorCode = "MALFORMED_REQUEST",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(
        ex: SQLException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("SQL exception: {}", ex.message, ex)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "A database errorException occurred",
            errorCode = "DATABASE_ERROR",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Illegal argument: {}", ex.message)
        
        val response = ApiResponse.errorException<Nothing>(
            message = ex.message ?: "Invalid argument provided",
            errorCode = "INVALID_ARGUMENT",
            path = getPath(request)
        )
        
        return ResponseEntity.badRequest().body(response)
    }

    /**
     * NullPointerException
     */
    @ExceptionHandler(NullPointerException::class)
    fun handleNullPointerException(
        ex: NullPointerException,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Null pointer exception: {}", ex.message, ex)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "An unexpected errorException occurred",
            errorCode = "INTERNAL_SERVER_ERROR",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception: {}", ex.message, ex)
        
        val response = ApiResponse.errorException<Nothing>(
            message = "An unexpected errorException occurred",
            errorCode = "INTERNAL_SERVER_ERROR",
            path = getPath(request)
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }

    private fun getPath(request: WebRequest): String? {
        return request.getDescription(false)
            .removePrefix("uri=")
            .takeIf { it.isNotBlank() }
    }
} 
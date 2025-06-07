package com.example.bank.common

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val error: ErrorDetail? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {

        fun <T> success(data: T, message: String = "Success"): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.ok(ApiResponse(true,  message, data))
        }

        fun <T> error(
            message: String,
            errorCode: String? = null,
            details: Any? = null,
            path: String? = null
        ): ResponseEntity<ApiResponse<T>> {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = message,
                    error = ErrorDetail(
                        code = errorCode,
                        details = details,
                        path = path
                    )
                )
            )
        }

        fun <T> errorException(
            message: String,
            errorCode: String? = null,
            details: Any? = null,
            path: String? = null
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                error = ErrorDetail(
                    code = errorCode,
                    details = details,
                    path = path
                )
            )
        }

        fun <T> validationError(
            message: String,
            fieldErrors: List<FieldErrorDetail>,
            path: String? = null
        ): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = message,
                error = ErrorDetail(
                    code = "VALIDATION_FAILED",
                    details = fieldErrors,
                    path = path
                )
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val code: String? = null,
    val details: Any? = null,
    val path: String? = null
)

data class FieldErrorDetail(
    val field: String,
    val rejectedValue: Any?,
    val message: String
) 
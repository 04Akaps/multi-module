package com.example.bank.monitoring

import io.micrometer.core.instrument.*
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

@Component
class BankMetrics(private val meterRegistry: MeterRegistry) {
    
    // 현재 계좌 수 게이지
    private val accountCountGauge = AtomicLong(0)
    
    init {
        // 총 계좌 수 게이지 등록
        meterRegistry.gauge("bank.account.total", accountCountGauge) { it.get().toDouble() }
    }
    
    // 계좌 관련 메트릭
    fun incrementAccountCreated() {
        Counter.builder("bank.account.created")
            .description("Number of accounts created")
            .register(meterRegistry)
            .increment()
    }
    
    fun updateAccountCount(count: Long) {
        accountCountGauge.set(count)
    }
    
    // 거래 관련 메트릭
    fun incrementTransaction(type: String) {
        Counter.builder("bank.transaction.count")
            .description("Number of transactions")
            .tag("type", type)
            .register(meterRegistry)
            .increment()
    }
    
    fun recordTransactionAmount(amount: BigDecimal, type: String) {
        DistributionSummary.builder("bank.transaction.amount")
            .description("Transaction amounts distribution")
            .tag("type", type)
            .register(meterRegistry)
            .record(amount.toDouble())
    }
    
    // 이벤트 관련 메트릭
    fun incrementEventPublished(eventType: String) {
        Counter.builder("bank.event.published")
            .description("Number of events published")
            .tag("type", eventType)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementEventProcessed(eventType: String) {
        Counter.builder("bank.event.processed")
            .description("Number of events processed")
            .tag("type", eventType)
            .tag("status", "success")
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementEventFailed(eventType: String) {
        Counter.builder("bank.event.failed")
            .description("Number of events failed")
            .tag("type", eventType)
            .tag("status", "failed")
            .register(meterRegistry)
            .increment()
    }
    
    fun recordEventProcessingTime(duration: Duration, eventType: String) {
        Timer.builder("bank.event.processing.time")
            .description("Event processing time")
            .tag("type", eventType)
            .register(meterRegistry)
            .record(duration)
    }
    
    // 분산 락 관련 메트릭
    fun incrementLockAcquisitionFailure(lockKey: String) {
        Counter.builder("bank.lock.acquisition.failed")
            .description("Number of failed lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }
    
    fun incrementLockAcquisitionSuccess(lockKey: String) {
        Counter.builder("bank.lock.acquisition.success")
            .description("Number of successful lock acquisitions")
            .tag("lock_key", lockKey)
            .register(meterRegistry)
            .increment()
    }
    
    // API 관련 메트릭
    fun recordApiResponseTime(duration: Duration, endpoint: String, method: String) {
        Timer.builder("bank.api.response.time")
            .description("API response time")
            .tag("endpoint", endpoint)
            .tag("method", method)
            .register(meterRegistry)
            .record(duration)
    }
    
    // 비즈니스 메트릭을 위한 헬퍼 메서드들
    fun getAccountCount(): Long = accountCountGauge.get()
}
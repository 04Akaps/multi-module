package com.example.bank.lock

import com.example.bank.exception.LockAcquisitionException
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@ConfigurationProperties(prefix = "bank.lock")
data class LockProperties(
    val timeout: Long = 5000,
    val leaseTime: Long = 10000,
    val retryInterval: Long = 100,
    val maxRetryAttempts: Int = 50
)

@Service
@EnableConfigurationProperties(LockProperties::class)
class DistributedLockService(
    private val redissonClient: RedissonClient,
    private val lockProperties: LockProperties,
) {
    private val logger = LoggerFactory.getLogger(DistributedLockService::class.java)
    
    fun <T> executeWithLock(
        lockKey: String,
        action: () -> T
    ): T {
        val lock = redissonClient.getLock(lockKey)

        return try {
            val acquired = lock.tryLock(
                lockProperties.timeout, 
                lockProperties.leaseTime, 
                TimeUnit.MILLISECONDS
            )
            
            if (!acquired) {
                logger.warn("Failed to acquire lock: $lockKey within ${lockProperties.timeout}ms")
                throw LockAcquisitionException("Unable to acquire lock: $lockKey")
            }

            try {
                // 실제 비즈니스 로직 실행
                action()
            } finally {
                // 락 해제
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
            
        } catch (e: InterruptedException) {
            logger.error("Thread interrupted while waiting for lock: $lockKey", e)
            throw LockAcquisitionException("Thread interrupted while acquiring lock: $lockKey", e)
        } catch (e: Exception) {
            logger.error("Error while executing with lock: $lockKey", e)
            throw e
        }
    }
    
    fun <T> executeWithAccountLock(
        accountNumber: String,
        action: () -> T
    ): T {
        val lockKey = "account:lock:$accountNumber"
        return executeWithLock(lockKey, action)
    }
    
    fun <T> executeWithTransferLock(
        fromAccountNumber: String,
        toAccountNumber: String,
        action: () -> T
    ): T {
        // 송금 시 데드락 방지를 위해 계좌번호 순서로 정렬하여 락 획득
        val sortedAccounts = listOf(fromAccountNumber, toAccountNumber).sorted()
        val lockKey = "transfer:lock:${sortedAccounts[0]}:${sortedAccounts[1]}"
        return executeWithLock(lockKey, action)
    }
}


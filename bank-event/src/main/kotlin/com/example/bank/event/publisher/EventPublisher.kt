package com.example.bank.event.publisher

import com.example.bank.domain.event.DomainEvent
import com.example.bank.monitoring.BankMetrics
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import kotlin.collections.forEach

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
    fun publishAsync(event: DomainEvent)
    fun publishAll(events: List<DomainEvent>)
    fun publishAllAsync(events: List<DomainEvent>)
}

@Component
open class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val bankMetrics: BankMetrics
) : DomainEventPublisher {

    private val logger = LoggerFactory.getLogger(SpringDomainEventPublisher::class.java)

    override fun publish(event: DomainEvent) {
        logger.info("Publishing domain event synchronously: ${event::class.simpleName} with ID: ${event.eventId}")
        try {
            applicationEventPublisher.publishEvent(event)
            bankMetrics.incrementEventPublished(event::class.simpleName ?: "Unknown")
        } catch (e: Exception) {
            logger.error("Failed to publish event synchronously: ${event.eventId}", e)
            throw e
        }
    }

    @Async("taskExecutor")
    override fun publishAsync(event: DomainEvent) {
        logger.info("Publishing domain event asynchronously: ${event::class.simpleName} with ID: ${event.eventId}")
        try {
            applicationEventPublisher.publishEvent(event)
            bankMetrics.incrementEventPublished(event::class.simpleName ?: "Unknown")
            logger.info("Successfully published event asynchronously: ${event.eventId}")
        } catch (e: Exception) {
            logger.error("Failed to publish event asynchronously: ${event.eventId}", e)
            // 에러 발생 시 별도 처리 로직 (예: Dead Letter Queue, 재시도 등)
        }
    }

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }

    @Async("taskExecutor")
    override fun publishAllAsync(events: List<DomainEvent>) {
        logger.info("Publishing ${events.size} domain events asynchronously")
        events.forEach { event ->
            try {
                applicationEventPublisher.publishEvent(event)
                bankMetrics.incrementEventPublished(event::class.simpleName ?: "Unknown")
                logger.debug("Successfully published event asynchronously: ${event.eventId}")
            } catch (e: Exception) {
                logger.error("Failed to publish event asynchronously: ${event.eventId}", e)
            }
        }
    }
}
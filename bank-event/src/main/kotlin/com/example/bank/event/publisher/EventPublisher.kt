package com.example.bank.event.publisher

import com.example.bank.domain.event.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import kotlin.collections.forEach

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
    fun publishAll(events: List<DomainEvent>)
}

@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : DomainEventPublisher {

    private val logger = LoggerFactory.getLogger(SpringDomainEventPublisher::class.java)

    override fun publish(event: DomainEvent) {
        logger.info("Publishing domain event: ${event::class.simpleName} with ID: ${event.eventId}")
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
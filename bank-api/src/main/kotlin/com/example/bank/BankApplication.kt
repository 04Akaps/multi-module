package com.example.bank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.example.bank"])
@EntityScan(basePackages = ["com.example.bank.domain.entity", "com.example.bank.cqrs.view"])
@EnableJpaRepositories(basePackages = ["com.example.bank.domain.repository", "com.example.bank.cqrs.view"])
@EnableAsync
@EnableScheduling
class BankApplication

fun main(args: Array<String>) {
    runApplication<BankApplication>(*args)
} 
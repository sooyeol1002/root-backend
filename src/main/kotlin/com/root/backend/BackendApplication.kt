package com.root.backend

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableAutoConfiguration
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}

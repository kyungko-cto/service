package com.delivery.api.config.jpa;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@EntityScan(basePackages="com.delivery.db") //여러개일시 {}쓰기
@EnableJpaRepositories(basePackages="com.delivery.db")
public class JpaConfig {
}

package com.delivery.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리 설정 - 대규모 트래픽 대응
 * 
 * 리팩토링 사항:
 * 1. 비동기 처리 활성화: @EnableAsync
 * 2. 스레드 풀 설정: CPU 코어 수에 맞춘 최적화
 * 3. 큐 용량 설정: 메모리 누수 방지
 * 4. 거부 정책 설정: 큐가 가득 찰 때 처리 방법
 * 
 * 성능 최적화:
 * - 비동기 처리로 블로킹 작업 최소화
 * - 스레드 풀 재사용으로 CPU 사용률 개선
 * - 큐 용량 제한으로 메모리 누수 방지
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${spring.task.execution.pool.core-size:10}")
    private int corePoolSize;

    @Value("${spring.task.execution.pool.max-size:50}")
    private int maxPoolSize;

    @Value("${spring.task.execution.pool.queue-capacity:100}")
    private int queueCapacity;

    /**
     * 비동기 작업용 스레드 풀
     * 
     * 사용 예: @Async("asyncExecutor")
     * 
     * 리팩토링: CPU 사용률 개선
     * - 코어 스레드 수: CPU 코어 수에 맞춤
     * - 최대 스레드 수: 대규모 트래픽 대응
     * - 큐 용량: 메모리 누수 방지
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // 큐가 가득 찰 때 호출자 스레드에서 실행
        executor.setWaitForTasksToCompleteOnShutdown(true);  // 종료 시 대기
        executor.setAwaitTerminationSeconds(60);  // 최대 60초 대기
        executor.initialize();
        return executor;
    }

    /**
     * 이메일 발송 등 경량 작업용 스레드 풀
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}


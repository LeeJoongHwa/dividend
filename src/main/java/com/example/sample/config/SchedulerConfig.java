package com.example.sample.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    //thread 수를 늘려 작업을 수월하게 함.
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();

        int n = Runtime.getRuntime().availableProcessors();
        //현재 시스템의 프로세스 수에 따라 스레드 풀의 크기를 동적으로 조정
        threadPool.setPoolSize(n);
        threadPool.initialize();
        //threadPool 을 초기화 하는 작업.
        //코어 스레드 수, 최대 스레드 수, 스레드 간격, 스레드 이름 등등

        taskRegistrar.setTaskScheduler(threadPool);
    }
}

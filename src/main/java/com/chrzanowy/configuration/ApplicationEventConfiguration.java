package com.chrzanowy.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class ApplicationEventConfiguration {

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        var eventMulticaster = new SimpleApplicationEventMulticaster();
        var taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setVirtualThreads(true);
        taskExecutor.setConcurrencyLimit(10);
        eventMulticaster.setTaskExecutor(taskExecutor);
        return eventMulticaster;
    }
}

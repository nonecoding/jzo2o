package com.jzo2o.market.config;

import com.jzo2o.redis.properties.RedisSyncProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 线程池配置类
 * @date 2023/11/6 18:55
 */
@Configuration
public class ThreadPoolConfiguration {

    @Bean("syncThreadPool")
    public ThreadPoolExecutor synchronizeThreadPool(RedisSyncProperties redisSyncProperties) {
        // 定义线程池参数
        int corePoolSize = 1; // 核心线程数
        int maxPoolSize = redisSyncProperties.getQueueNum(); // 最大线程数
        long keepAliveTime = 120; // 线程空闲时间
        TimeUnit unit = TimeUnit.SECONDS; // 时间单位
          // 指定拒绝策略为 DiscardPolicy 
        RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.DiscardPolicy();
        // 任务队列，使用SynchronousQueue容量为1，在没有线程去消费时不会保存任务
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit,
                new SynchronousQueue<>(),rejectedHandler);

        return executor;
    }
}
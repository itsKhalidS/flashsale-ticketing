package com.devon.flashsale.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FlashSaleMetricsConfig {

    private final Counter ordersCreatedCounter;
    private final Counter optimisticLockRetriesCounter;

    public FlashSaleMetricsConfig(MeterRegistry registry) {
        this.ordersCreatedCounter = Counter.builder("orders.created")
                .description("Total number of orders created")
                .register(registry);

        this.optimisticLockRetriesCounter = Counter.builder("optimistic.lock.retries")
                .description("Number of optimistic lock retries during concurrent updates")
                .register(registry);
    }

    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    public void incrementOptimisticLockRetries() {
        optimisticLockRetriesCounter.increment();
    }
}
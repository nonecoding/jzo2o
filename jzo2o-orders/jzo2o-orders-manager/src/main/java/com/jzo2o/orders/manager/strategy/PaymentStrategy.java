
package com.jzo2o.orders.manager.strategy;

import java.math.BigDecimal;

// 定义策略接口
public interface PaymentStrategy {
    void pay(BigDecimal amount);
}
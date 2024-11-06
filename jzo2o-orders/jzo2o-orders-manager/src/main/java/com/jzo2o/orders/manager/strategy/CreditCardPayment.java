package com.jzo2o.orders.manager.strategy;

import java.math.BigDecimal;

/**
 * @author Mr.M
 * @version 1.0
 * @description 信用卡支付策略类
 * @date 2023/11/18 18:58
 */
public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(BigDecimal amount) {
        System.out.println("信用卡:" + cardNumber + "支付金额:" + amount);
    }
}


// 环境类，负责维护策略接口
class ShoppingCart {
    private PaymentStrategy paymentStrategy;

    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(BigDecimal amount) {
        paymentStrategy.pay(amount);
    }
}

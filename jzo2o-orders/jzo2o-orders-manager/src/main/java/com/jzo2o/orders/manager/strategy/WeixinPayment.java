package com.jzo2o.orders.manager.strategy;

import java.math.BigDecimal; /**
 * @author Mr.M
 * @version 1.0
 * @description 微信支付策略类
 * @date 2023/11/18 18:58
 */
public class WeixinPayment implements PaymentStrategy {

    private String account;

    public WeixinPayment(String account) {
        this.account = account;
    }

    @Override
    public void pay(BigDecimal amount) {
        System.out.println("微信:" + account + "支付金额:" + amount);
    }
}

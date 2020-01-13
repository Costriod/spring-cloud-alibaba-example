package com.example.demo;

import org.apache.dubbo.config.spring.extension.SpringExtensionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 使用dubbo替换feign client的过程中，发现tracing无法生效，最终定位到SpringExtensionFactory的CONTEXTS是空set，
 * 导致返回的object为null，所以这也是为什么tracing不生效，因为注入的是一个null，目前的解决方案就是
 * 在dubbo的注入对象还未初始化之前执行SpringExtensionFactory.addApplicationContext(applicationContext)
 */
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CustomContextAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }
}

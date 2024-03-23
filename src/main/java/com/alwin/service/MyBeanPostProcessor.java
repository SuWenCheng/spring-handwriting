package com.alwin.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.Proxy;

@Component("myBeanPostProcessor")
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前-" + beanName);
        if (beanName.equals("userService")) {
            ((UserServiceImpl) bean).setComment("初始化前的评价");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后-" + beanName);

        if (beanName.equals("userService")) {
            return Proxy.newProxyInstance(MyBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                System.out.println("执行代理逻辑");
                return method.invoke(bean, args);
            });
        }
        return bean;
    }
}

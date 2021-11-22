package com.alwin.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

@Component("myBeanPostProcessor")
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前-" + beanName);
        if (beanName.equals("userService")) {
            ((UserService) bean).setComment("初始化前的评价");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后-" + beanName);
        return bean;
    }
}

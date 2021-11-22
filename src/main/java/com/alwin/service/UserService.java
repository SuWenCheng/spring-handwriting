package com.alwin.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;

@Component("userService")
public class UserService implements BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test() {
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}

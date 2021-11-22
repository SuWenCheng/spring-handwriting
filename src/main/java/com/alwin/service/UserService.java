package com.alwin.service;

import com.spring.Autowired;
import com.spring.Component;

@Component("userService")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }

}

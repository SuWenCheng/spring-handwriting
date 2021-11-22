package com.alwin;

import com.spring.MyApplicationContext;


public class Test {

    public static void main(String[] args) {

        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);

        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));

        System.out.println(applicationContext.getBean("orderService"));
        System.out.println(applicationContext.getBean("orderService"));
        System.out.println(applicationContext.getBean("orderService"));



    }

}

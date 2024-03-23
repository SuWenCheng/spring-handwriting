package com.spring;

import com.alwin.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyApplicationContextTest {

    private MyApplicationContext context;

    @BeforeEach
    void setUp() {
        context = new MyApplicationContext(AppConfig.class);
    }

    @Test
    void testGetBean() {
        // Test getting a singleton bean
        Object singletonBean = context.getBean("userService");
        assertNotNull(singletonBean);

        // Test getting a prototype bean
        Object prototypeBean = context.getBean("orderService");
        assertNotNull(prototypeBean);

        // Test getting a non-existent bean
        assertThrows(NullPointerException.class, () -> context.getBean("nonExistentBean"));
    }
}

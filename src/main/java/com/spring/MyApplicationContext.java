package com.spring;


import com.alwin.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MyApplicationContext {

    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyApplicationContext(Class<AppConfig> configClass) {

        // ComponentScan扫描
        scan(configClass);

        // 生成单例的bean
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                singletonMap.put(beanName, createBean(beanName, beanDefinition));
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        try {
            Object newInstance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(newInstance, bean);
                }
            }

            // Aware回调
            if (newInstance instanceof BeanNameAware) {
                ((BeanNameAware)newInstance).setBeanName(beanName);
            }

            // 初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                newInstance = beanPostProcessor.postProcessBeforeInitialization(newInstance, beanName);
            }

            // 初始化
            if (newInstance instanceof InitializingBean) {
                ((InitializingBean)newInstance).afterPropertiesSet();
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                newInstance = beanPostProcessor.postProcessAfterInitialization(newInstance, beanName);
            }

            return newInstance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void scan(Class<AppConfig> configClass) {
        ComponentScan declaredAnnotation = configClass.getDeclaredAnnotation(ComponentScan.class);

        String path = declaredAnnotation.value();
        path = path.replace('.', '/');

        ClassLoader classLoader = MyApplicationContext.class.getClassLoader(); // APP ClassLoader
        URL resource = classLoader.getResource(path);
        assert resource != null;
        File resFile = new File(resource.getFile());

        if (resFile.isDirectory()) {
            File[] files = resFile.listFiles();
            assert files != null;
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                if (absolutePath.endsWith(".class")) {
                    String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    className = className.replace("\\", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            // 设置BeanDefinition
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }
                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    } catch (ClassNotFoundException | InstantiationException | InvocationTargetException
                            | NoSuchMethodException | IllegalAccessException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinition == null) {
            // 不存在对应的bean
            throw new NullPointerException();
        }

        if (beanDefinition.getScope().equals("singleton")) {
            return singletonMap.get(beanName);
        }

        return createBean(beanName, beanDefinition);
    }

}

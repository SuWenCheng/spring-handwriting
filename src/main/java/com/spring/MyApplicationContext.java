package com.spring;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private Class configClass;

    public MyApplicationContext(Class configClass) {
        this.configClass = configClass;

        // ComponentScan扫描
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                singletonMap.put(beanName, createBean(beanDefinition));
            }
        }

    }

    private Object createBean(BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object newInstance = clazz.getDeclaredConstructor().newInstance();
            return newInstance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan declaredAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);

        String path = declaredAnnotation.value();
        path = path.replace('.', '/');

        ClassLoader classLoader = MyApplicationContext.class.getClassLoader(); // APP ClassLoader
        URL resource = classLoader.getResource(path);
        File resFile = new File(resource.getFile());

        if (resFile.isDirectory()) {
            File[] files = resFile.listFiles();
            for (File file : files) {
                String absolutePath = file.getAbsolutePath();
                if (absolutePath.endsWith(".class")) {
                    String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                    className = className.replace("\\", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
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
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
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

        return createBean(beanDefinition);
    }

}

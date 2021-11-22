package com.spring;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyApplicationContext {

    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private Class<com.alwin.AppConfig> configClass;

    public MyApplicationContext(Class<com.alwin.AppConfig> configClass) {
        this.configClass = configClass;

        // ComponentScan扫描
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                singletonMap.put(beanName, createBean(beanName, beanDefinition));
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
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

            if (newInstance instanceof BeanNameAware) {
                ((BeanNameAware)newInstance).setBeanName(beanName);
            }

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

    private void scan(Class<com.alwin.AppConfig> configClass) {
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

        return createBean(beanName, beanDefinition);
    }

}

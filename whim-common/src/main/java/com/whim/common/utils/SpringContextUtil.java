package com.whim.common.utils;

import lombok.Getter;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Jince
 * date: 2024/10/20 01:11
 * description: Spring工具类，方便在非Spring管理环境中获取Bean。
 * 这个类实现了BeanFactoryPostProcessor和ApplicationContextAware接口，
 * 因此在Spring容器启动时可以获取到BeanFactory和ApplicationContext的引用。
 * 主要提供获取Bean及其他Bean相关信息的方法。
 */
@Component
public class SpringContextUtil implements BeanFactoryPostProcessor, ApplicationContextAware {
    private static ConfigurableListableBeanFactory beanFactory;
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) {
        SpringContextUtil.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static ListableBeanFactory getBeanFactory() {
        return beanFactory == null ? applicationContext : beanFactory;
    }

    /**
     * 获取Bean实例
     *
     * @param name Bean Name
     * @return 一个以所给名称注册的Bean的实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过类型获取Bean实例
     *
     * @param tClass Bean Class
     * @return 一个以所给类型注册的Bean的实例
     */
    public static <T> T getBean(Class<T> tClass) {
        return getBeanFactory().getBean(tClass);
    }

    /**
     * 通过名称和类型获取Bean实例
     *
     * @param name   Bean Name
     * @param tClass Bean Class
     * @return 一个以所给名称和类型注册的Bean的实例
     */
    public static <T> T getBean(String name, Class<T> tClass) {
        return getBeanFactory().getBean(name, tClass);
    }

    /**
     * 检查是否包含指定名称的Bean
     *
     * @param name Bean Name
     * @return 如果包含则返回true，否则返回false
     */
    public static Boolean containsBean(String name) {
        return getBeanFactory().containsBean(name);
    }

    /**
     * 检查指定名称的Bean是否为单例
     *
     * @param name Bean Name
     * @return 如果是单例则返回true，否则返回false
     */
    public static boolean isSingleton(String name) {
        return getBeanFactory().isSingleton(name);
    }

    /**
     * 获取指定名称的Bean的类型
     *
     * @param name Bean Name
     * @return Bean的类型，如果没有找到则返回null
     */
    public static Class<?> getType(String name) {
        return getBeanFactory().getType(name);
    }

    /**
     * 获取指定名称的Bean的别名
     *
     * @param name Bean Name
     * @return 一个包含Bean别名的数组
     */
    public static String[] getAliases(String name) {
        return getBeanFactory().getAliases(name);
    }

    /**
     * 获取当前AOP代理对象
     *
     * @param invoker 当前调用者
     * @return 当前AOP代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    /**
     * 获取当前激活的Spring Profile
     *
     * @return 一个包含当前激活的Profile的数组
     */
    public static String[] getActiveProfiles() {
        return getApplicationContext().getEnvironment().getActiveProfiles();
    }

    /**
     * 获取指定的环境属性值
     *
     * @param key 属性键
     * @return 属性值
     */
    public static String getRequiredProperty(String key) {
        return getApplicationContext().getEnvironment().getRequiredProperty(key);
    }
}

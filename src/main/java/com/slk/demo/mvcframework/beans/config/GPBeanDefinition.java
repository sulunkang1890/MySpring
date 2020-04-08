package com.slk.demo.mvcframework.beans.config;

public class GPBeanDefinition {
    /**
     * 在工厂里面叫什么名字 这个类对应的工厂是什么
     */
    private String factoryBeanName;
    /**
     * 这个bean对应的是哪个class
     */
    private String beanClassName;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}

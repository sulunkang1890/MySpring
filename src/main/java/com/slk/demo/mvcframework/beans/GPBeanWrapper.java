package com.slk.demo.mvcframework.beans;

public class GPBeanWrapper {
    /**
     * 把IOC容器的对象用装饰器包装下 因为IOC容器中可能用到代理对象
     */
    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public GPBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClass=this.wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public void setWrapperInstance(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }

    public void setWrapperClass(Class<?> wrapperClass) {
        this.wrapperClass = wrapperClass;
    }


}

package com.slk.demo.framework.aop.aspect;
import java.lang.reflect.Method;


/**
 * advice 相当于通知
 */
public class GPAdvice {
    private Object aspect; //这个通知的方法是哪个切面的
    private Method adviceMethod; // 具体的通知方法
    private String throwName; //根据异常的名字来匹配 // 异常处理

    public GPAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    public Object getAspect() {
        return aspect;
    }

    public void setAspect(Object aspect) {
        this.aspect = aspect;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public void setAdviceMethod(Method adviceMethod) {
        this.adviceMethod = adviceMethod;
    }

    public String getThrowName() {
        return throwName;
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }

    @Override
    public String toString() {
        return "GPAdvice{" +
                "aspect=" + aspect +
                ", adviceMethod=" + adviceMethod +
                ", throwName='" + throwName + '\'' +
                '}';
    }
}

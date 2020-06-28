package com.slk.demo.framework.aop.support;

import com.slk.demo.framework.aop.aspect.GPAdvice;
import com.slk.demo.framework.aop.config.GPAopConfig;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;
/**
 * 解析AOP配置的工具类
 */
public class GPAdvisedSupport {
    private GPAopConfig config; //aop config 文件
    private Object target; // 需要织入的目标类对象
    private Class targetClass; // 需要织入的目标类方法
    private Pattern pointCutClassPattern; //切入规则正则表达表达式
    /**
     * 一个方法对应多个通知
     * Map<String,GPAdvice> key:before，after ， value具体的通知
     */
    private Map<Method, Map<String, GPAdvice>> methodCache;

    public GPAopConfig getConfig() {
        return config;
    }

    public void setConfig(GPAopConfig config) {
        this.config = config;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Pattern getPointCutClassPattern() {
        return pointCutClassPattern;
    }

    public void setPointCutClassPattern(Pattern pointCutClassPattern) {
        this.pointCutClassPattern = pointCutClassPattern;
    }

    public Map<Method, Map<String, GPAdvice>> getMethodCache() {
        return methodCache;
    }

    public void setMethodCache(Map<Method, Map<String, GPAdvice>> methodCache) {
        this.methodCache = methodCache;
    }

    @Override
    public String toString() {
        return "GPAdvisedSupport{" +
                "config=" + config +
                ", target=" + target +
                ", targetClass=" + targetClass +
                ", pointCutClassPattern=" + pointCutClassPattern +
                ", methodCache=" + methodCache +
                '}';
    }
}
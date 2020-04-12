package com.slk.demo.mvcframework.context;

import com.slk.demo.mvcframework.annotation.GPAutowired;
import com.slk.demo.mvcframework.annotation.GPController;
import com.slk.demo.mvcframework.annotation.GPService;
import com.slk.demo.mvcframework.beans.GPBeanWrapper;
import com.slk.demo.mvcframework.beans.config.GPBeanDefinition;
import com.slk.demo.mvcframework.beans.support.GPBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  GPApplicationContext 完成bean的创建和DI
 */
public class GPApplicationContext {
    private GPBeanDefinitionReader beanDefinitionReader;
    private Map<String, GPBeanDefinition> beanDefinitionMap=new HashMap<String, GPBeanDefinition>();
    /**
     * factoryBeanInstanceCache 这个IOC容器中放入的是beanWrapper 装饰过的对象
     */
    private Map<String,GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String, GPBeanWrapper>();
    /**
     * factoryBeanObjectCache 放入的是原生的对象
     */
    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();
    public GPApplicationContext(String ... configLocations) {
        //加载配置文件
        beanDefinitionReader=new GPBeanDefinitionReader(configLocations);
        //解析配置文件 封装成BeanDefinition
        List<GPBeanDefinition> definitionlist=beanDefinitionReader.loadBeanDefinitions();
        //把BeanDefinition缓存起来 注册到缓存中
        try {
            doRegisterBeanDefinition(definitionlist);
        } catch (Exception e) {
            e.printStackTrace();
        }
        doAutowrited();

    }

    private void doAutowrited() {
        //所有的bean 没有被真正实例化 目前只是配置阶段
        for (Map.Entry<String, GPBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName=entry.getKey();
            getBean(beanName);
        }
    }


    private void doRegisterBeanDefinition(List<GPBeanDefinition> definitionlist) throws Exception {
        for (GPBeanDefinition beanDefinition : definitionlist) {
            if(beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The"+beanDefinition.getFactoryBeanName()+"has exsit");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }

    //bean的实例化 实在 getbean开始
    public Object getBean(String beanName){
        //1.先拿到配置信息
        GPBeanDefinition beanDefinition=this.beanDefinitionMap.get(beanName);
        //2.反射实例化对象 new Instance
        Object instance = instantiateBean(beanName,beanDefinition);
        //3、封装成一个叫做BeanWrapper
        GPBeanWrapper beanWrapper=new GPBeanWrapper(instance);
        //4.放入到IOC容器中
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);
        //5.DI 依赖注入 返回之前 必须要进行依赖注入 否则会出现空指针
        populateBean(beanName,beanDefinition,beanWrapper);
        return beanWrapper.getWrapperInstance();
    }

    /**
     * 依赖注入
     * @param beanName
     * @param beanDefinition
     * @param beanWrapper
     */
//    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper beanWrapper) {
//        //可能涉及到循环依赖？
//        //A{ B b}
//        //B{ A b}
//        //用两个缓存，循环两次
//        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
//        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
//
//        Object instance = beanWrapper.getWrapperInstance();
//
//        Class<?> clazz = beanWrapper.getWrapperClass();
//
//        //在Spring中@Component
//        if(!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))){
//            return;
//        }
//
//        //把所有的包括private/protected/default/public 修饰字段都取出来
//        for (Field field : clazz.getDeclaredFields()) {
//            if(!field.isAnnotationPresent(GPAutowired.class)){ continue; }
//
//            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
//
//            //如果用户没有自定义的beanName，就默认根据类型注入
//            String autowiredBeanName = autowired.value().trim();
//            if("".equals(autowiredBeanName)){
//                //field.getType().getName() 获取字段的类型
//                autowiredBeanName = field.getType().getName();
//            }
//
//            //暴力访问
//            field.setAccessible(true);
//
//            try {
//                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
//                    continue;
//                }
//                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper beanWrapper) {
        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A b}
        //用两个缓存，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值

        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrapperClass();

        //在Spring中@Component
        if(!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))){
            return;
        }

        //把所有的包括private/protected/default/public 修饰字段都取出来
        for (Field field : clazz.getDeclaredFields()) {
            if(!field.isAnnotationPresent(GPAutowired.class)){ continue; }

            GPAutowired autowired = field.getAnnotation(GPAutowired.class);

            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                //field.getType().getName() 获取字段的类型
                autowiredBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try {
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                //ioc.get(beanName) 相当于通过接口的全名拿到接口的实现的实例
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }

    }


    /**
     * 实例化对象
     * @param beanName
     * @param beanDefinition
     * @return
     */
//    private Object instantiateBean(String beanName, GPBeanDefinition beanDefinition) {
//        String className = beanDefinition.getBeanClassName();
//        Object instance=null;
//        try {
//            /**
//             * IOC容器中应该是单例的 所以加入之前需要先判断容器中是否已经存在
//             */
//            if(this.factoryBeanObjectCache.containsKey(className)){
//                return this.factoryBeanInstanceCache.get(className);
//            }
//            Class<?> clazz=Class.forName(className);
//
//            instance=clazz.newInstance();
//            this.factoryBeanObjectCache.put(beanName,instance);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return instance;
//    }
    private Object instantiateBean(String beanName, GPBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {

            if(this.factoryBeanObjectCache.containsKey(beanName)){
                instance = this.factoryBeanObjectCache.get(beanName);
            }else {

                Class<?> clazz = Class.forName(className);
                //2、默认的类名首字母小写
                instance = clazz.newInstance();
                this.factoryBeanObjectCache.put(beanName, instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(Class<?> beanClass){
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitionCount() {
       return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }
}

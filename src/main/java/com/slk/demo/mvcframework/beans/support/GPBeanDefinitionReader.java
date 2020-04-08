package com.slk.demo.mvcframework.beans.support;

import com.slk.demo.mvcframework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 单一职责 复制读取配置文件
 */
public class GPBeanDefinitionReader {
    private Properties contextConfig=new Properties();
    /**
     * 保存扫描的结果
     */
    private List<String> registryBeanClasses=new ArrayList<String>();
    public GPBeanDefinitionReader(String ... configLocations) {
       doLoadConfig(configLocations[0]);
       //扫描配置文件的相关类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public List<GPBeanDefinition> loadBeanDefinition() {
        List<GPBeanDefinition> result=new ArrayList<GPBeanDefinition>();
        for (String className : registryBeanClasses) {
            try {
                Class<?> beanClass=Class.forName(className);
                //先判读是不是普通类
                //自定义命名的类
                 result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName() ));
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
                //接口类
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars=simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    private GPBeanDefinition doCreateBeanDefinition(String beanName,String beanClassName) {
        GPBeanDefinition beanDefinition=new GPBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private void doLoadConfig(String contextConfigLocation) {
        /**
         *
         * 通过classLoder 读取这个配置文件 application.properties 转化成流
         *  <init-param>
         *       <param-name>contextConfigLocation</param-name>
         *       <param-value>application.properties</param-value>
         *   </init-param>
         *   通过 inputStream  变成Properties contextConfig 保存
         */
        InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(inputStream!=null){
                try {
                    inputStream.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    private void doScanner(String scanPackage) {
        //转换文件路径
        URL url=this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));
        File classPaths=new File(url.getFile());
        for(File file :classPaths.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else {
                if(!file.getName().endsWith(".class")) {
                    continue;
                }
                String className=(scanPackage+"."+file.getName()).replace(".class","");
                registryBeanClasses.add(className);
            }
        }

    }
}

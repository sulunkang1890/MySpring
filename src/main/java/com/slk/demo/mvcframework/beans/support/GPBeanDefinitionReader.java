package com.slk.demo.mvcframework.beans.support;


import com.slk.demo.mvcframework.annotation.GPController;
import com.slk.demo.mvcframework.annotation.GPService;
import com.slk.demo.mvcframework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Tom.
 */
public class GPBeanDefinitionReader {

    //保存扫描的结果
    private List<String> regitryBeanClasses = new ArrayList<String>();
    private Properties contextConfig = new Properties();

    public GPBeanDefinitionReader(String... configLocations) {
        doLoadConfig(configLocations[0]);

        //扫描配置文件中的配置的相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
        try {
            for (String className : regitryBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if(beanClass.isAnnotationPresent(GPController.class)) {
                    //key提取出来了，把value也搞出来

                    result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                }else if(beanClass.isAnnotationPresent(GPService.class)){
                    //1、在多个包下出现相同的类名，只能寄几（自己）起一个全局唯一的名字
                    //自定义命名
                    String beanName = beanClass.getAnnotation(GPService.class).value();
                    if("".equals(beanName.trim())){
                        result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                    }
                    //2、默认的类名首字母小写
                    //3、如果是接口
                    //判断有多少个实现类，如果只有一个，默认就选择这个实现类
                    //如果有多个，只能抛异常
                    for (Class<?> i : beanClass.getInterfaces()) {
                        result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                    }
                }else{
                    continue;
                }


            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    private GPBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }


    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        //jar 、 war 、zip 、rar
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());

        //当成是一个ClassPath文件夹
        for (File file : classPath.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                if(!file.getName().endsWith(".class")){continue;}
                //全类名 = 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                //Class.forName(className);
                regitryBeanClasses.add(className);
            }
        }
    }


    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }

}

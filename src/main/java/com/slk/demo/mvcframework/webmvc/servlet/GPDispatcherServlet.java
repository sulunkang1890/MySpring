package com.slk.demo.mvcframework.webmvc.servlet;

import com.slk.demo.mvcframework.annotation.*;
import com.slk.demo.mvcframework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 委派模式 任务调度
 */
public class GPDispatcherServlet extends HttpServlet {
    //保存application.properties 配置文件中的内容  现在的内容是包名
    private Properties contextConfig=new Properties();
    //保存所有的类名
    private List<String> classNames=new ArrayList<String>();
    //IOC 容器
//    private Map<String,Object> ioc=new HashMap<String, Object>();
    //保存url和Method的对应关系
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    private GPApplicationContext applicationContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //委派通过url找到对应的执行方法
        try {
            doDispatch(req,resp);

        }catch (Exception e){
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //获取绝对路径 /springdemo1_war/query
        String url=req.getRequestURI();
        //获取项目虚拟目录 /springdemo1_war/query
        String contextPath=req.getContextPath();
        //处理成相对路径  localhost/springdemo1_war/query?name=slk=>/query
        url=url.replaceAll(contextPath,"").replaceAll("/+","/");
        if(!this.handlerMapping.containsKey(url)){
            try {
                resp.getWriter().write("404: not Found!!");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Method method=this.handlerMapping.get(url);
        //拿到get请求的的参数
        Map<String,String[]> params=req.getParameterMap();
        //获取方法的形参列表
        Class<?>[] parameterTypes=method.getParameterTypes();
        Object [] paramValues=new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType=parameterTypes[i];
            if(parameterType==HttpServletRequest.class){
                paramValues[i]=req;
            }else if (parameterType==HttpServletResponse.class){
                paramValues[i]=resp;
            }else if(parameterType==String.class){
                Annotation [] [] annotations=method.getParameterAnnotations();
                //method.getParameterAnnotations(); 返回值是二维数组  第一个维度对应参数列表里参数的数目，第二个维度对应参数列表里对应的注解
                //annotations【i】 是i的原因  是因为 这里面 从第i 个开始遍历 前面的 已经不需要遍历了
                for (int k=0;k<annotations[i].length;k++){
                     if(annotations[i][k] instanceof GPRequestParam){
                         String paramName = ((GPRequestParam) annotations[i][k]).value();
                         if(!"".equals(paramName.trim())){
                             String temp=Arrays.toString(params.get(paramName));
                            String value = Arrays.toString(params.get(paramName))
                                .replaceAll("\\[|\\]","")
                                .replaceAll("\\s",",");
                            paramValues[i] = value;

                         }
                    }
                }
            }
        }
        String beanName  = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
//       method.invoke(ioc.get(beanName),paramValues);
        method.invoke(applicationContext.getBean(beanName),paramValues);
    }



    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心IoC容器
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
        //5、初始化HandlerMapping
        initHandlerMapping();
    }
    //完成 url-method 之间的对应
    private void initHandlerMapping() {
        if(this.applicationContext.getBeanDefinitionCount()==0) return;
        for (String beanName:this.applicationContext.getBeanDefinitionNames()){
            Object instance=this.applicationContext.getBean(beanName);
            Class<?> clzz=instance.getClass();
            if (!clzz.isAnnotationPresent(GPController.class)){
                continue;
            }
            //保存类中的/xxxx

            String baseUrl="";
            if(clzz.isAnnotationPresent(GPRequestMapping.class)){
                GPRequestMapping requestMapping=clzz.getAnnotation(GPRequestMapping.class);
                baseUrl=requestMapping.value();
            }
            Method [] methods=clzz.getMethods();
            for (Method method:methods){
                if(!method.isAnnotationPresent(GPRequestMapping.class)){
                    continue;
                }
                GPRequestMapping requestMapping=method.getAnnotation(GPRequestMapping.class);
                String url=("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
            }
        }

    }


    public String toLowerFirstCase(String simpleName) {
        char [] chars=simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    //扫描相关的类
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
                classNames.add(className);
            }
        }

    }
    //加载配置文件
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
        InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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

}

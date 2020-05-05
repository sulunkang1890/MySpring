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

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 委派模式 任务调度
 */
public class GPDispatcherServlet extends HttpServlet {
    //保存application.properties 配置文件中的内容  现在的内容是包名
    private Properties contextConfig=new Properties();
    //保存所有的类名
    private List<String> classNames=new ArrayList<String>();
    private List<GPHandlerMapping> handlerMappings=new ArrayList<GPHandlerMapping>();
    private Map<GPHandlerMapping,GPHandlerAdapter> handlerAdapters =new HashMap<GPHandlerMapping, GPHandlerAdapter>();
    private GPApplicationContext applicationContext;
    private List<GPViewResolver> viewResolvers = new ArrayList<GPViewResolver>();

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
        //完成了对HandlerMapping的封装
        //完成了对方法返回值的封装ModelAndView
        //1、通过URL获得一个HandlerMapping
        GPHandlerMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new GPModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMaping获得一个HandlerAdapter
        GPHandlerAdapter ha = getHandlerAdapter(handler);
        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        GPModelAndView mv = ha.handler(req,resp,handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);

    }




    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, GPModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}

        for (GPViewResolver viewResolver : this.viewResolvers) {
            GPView view = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器输出
            view.render(mv.getModel(),req,resp);
            return;
        }
    }
    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return null;
        }
        //获取绝对路径 /springdemo1_war/query
        String url=req.getRequestURI();
        //获取项目虚拟目录 /springdemo1_war/query
        String contextPath=req.getContextPath();
        //处理成相对路径  localhost/springdemo1_war/query?name=slk=>/query
        url=url.replaceAll(contextPath,"").replaceAll("/+","/");
        for (GPHandlerMapping mapping : handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
            return mapping;
        }
        return null;
    }
    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(handler);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心IoC容器
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
        //完成了IoC、DI和MVC部分对接
        //初始化九大组件
        initStrategies(applicationContext);
        System.out.println("GP Spring framework is init.");
    }
    //完成 url-method 之间的对应
    private void initHandlerMappings(GPApplicationContext context) {
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
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                //handlerMapping.put(url,method);
                handlerMappings.add(new GPHandlerMapping(pattern,instance,method));
                System.out.println("Mapped : " + regex + "," + method);
            }
        }

    }
    private void initStrategies(GPApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器

        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }



    private void initHandlerAdapters(GPApplicationContext context) {
        for (GPHandlerMapping handlerMapping : handlerMappings) {
             this.handlerAdapters.put(handlerMapping,new GPHandlerAdapter());
        }

    }

    private void initViewResolvers(GPApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        URL url=this.getClass().getClassLoader().getResource(templateRoot);
        String temp=url.getFile();
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new GPViewResolver(templateRoot));
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
         *
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

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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private Map<String,Object> ioc=new HashMap<String, Object>();
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

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
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
        try {
            method.invoke(ioc.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName())),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

//    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//        //绝对路径
//        String url = req.getRequestURI();
//        //处理成相对路径 得到项目的虚拟路径
//        String contextPath = req.getContextPath();
//        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
//
//        if(!this.handlerMapping.containsKey(url)){
//            resp.getWriter().write("404 Not Found!!!");
//            return;
//        }
//
//        Method method = this.handlerMapping.get(url);
//
//        //从reqest中拿到url传过来的参数
//        Map<String,String[]> params = req.getParameterMap();
//
//        //获取方法的形参列表
//        Class<?> [] parameterTypes = method.getParameterTypes();
//
//        Object [] paramValues = new Object[parameterTypes.length];
//
//        for (int i = 0; i < parameterTypes.length; i ++) {
//            Class parameterType = parameterTypes[i];
//            //不能用instanceof，parameterType它不是实参，而是形参
//            if(parameterType == HttpServletRequest.class){
//                paramValues[i] = req;
//                continue;
//            }else if(parameterType == HttpServletResponse.class){
//                paramValues[i] = resp;
//                continue;
//            }else if(parameterType == String.class){
////                GPRequestParam requestParam = (GPRequestParam)parameterType.getAnnotation(GPRequestParam.class);
//                //提取方法中加了注解的参数
//                //把方法上的注解拿到，得到的是一个二维数组
//                //因为一个参数可以有多个注解，而一个方法又有多个参数
//                Annotation[] [] pa = method.getParameterAnnotations();
//                //method.getParameterAnnotations(); 返回值是二维数组  第一个维度对应参数列表里参数的数目，第二个维度对应参数列表里对应的注解
//                for (int j = 0; j< pa.length ; j ++) {
//                    for(Annotation a : pa[i]){
//                        if(a instanceof GPRequestParam){
//                            String paramName = ((GPRequestParam) a).value();
//                            if(!"".equals(paramName.trim())){
//                                String value = Arrays.toString(params.get(paramName))
//                                        .replaceAll("\\[|\\]","")
//                                        .replaceAll("\\s",",");
//                                paramValues[i] = value;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        //投机取巧的方式
//        //通过反射拿到method所在class，拿到class之后还是拿到class的名称
//        //再调用toLowerFirstCase获得beanName
//        String beanName  = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
//        method.invoke(ioc.get(beanName),paramValues);
//    }


    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化Spring核心IoC容器
        applicationContext = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
////        //1. 加载配置文件
////        doLoadConfig(config.getInitParameter("contextConfigLocation"));
//        //2.扫描相关的类
//        doScanner(contextConfig.getProperty("scanPackage"));
//        //3.实例化对象 初始化扫描到的类 并且将他们放入IOC容器中
//        doInstance();
//        doAutowired();
        //1 初始化spring IOC 核心容器
        applicationContext=new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
        //5、初始化HandlerMapping
        initHandlerMapping();
    }
    //完成 url-method 之间的对应
    private void initHandlerMapping() {
        if(ioc==null) return;
        for (Map.Entry<String,Object> entry: ioc.entrySet()){
            Class<?> clzz=entry.getValue().getClass();
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

    //DI 操作
    private void doAutowired() {
        if(ioc==null){
            return;
        }
        for (Map.Entry<String,Object> entry :ioc.entrySet()){
            // 拿到IOC容器中对象的所有字段
            //拿到所有的实例对象对应类中的所有字段 包括 public private protected
            Field[] fields=entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields) {
                //如果字段没有自动注入的注解则不需要自动注入 忽略
                if(!field.isAnnotationPresent(GPAutowired.class)) continue;
                GPAutowired gpAutowired=field.getAnnotation(GPAutowired.class);
                String beanName=gpAutowired.value();
                if("".equals(beanName)){
                    //如果用户没有自定义beanName，默认就根据类型注入
                    //通过类型的名称去IOC容器中获取对应的实例对象
                    beanName=field.getType().getName();
                }
                //设置的GPAutowired的字段 即使是私有的也需求强制访问
                field.setAccessible(true);
                try {
                    // set(Object,Object)第一参数 为 该字段存在的类  第二个参数为该字段要设置的值
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 初始化实例对象 初始化扫描的类并且将他们放到IOC容器中
     */
    private void doInstance() {
        if(classNames==null) {return;}
        try {
            for (String classname :classNames){
                //什么样的类才初始化
                Class<?> clazz=Class.forName(classname);
                //加了注解的类初始化
                //1. 判断是不是controller

                if (clazz.isAnnotationPresent(GPController.class)){
                    Object instacne=clazz.newInstance();
                    String beanName=toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instacne);
                }else if(clazz.isAnnotationPresent(GPService.class)){
                    //2.判断是不是service
                        //2.1 service 中判断是否含有自定义的beanName
                        GPService gpService=clazz.getAnnotation(GPService.class);
                        String beanName =gpService.value();
                        if("".equals(beanName.trim())){
                            // 2.2 默认的类是 首字母小写
                            beanName=toLowerFirstCase(clazz.getSimpleName());
                        }
                       Object instance=clazz.newInstance();
                        ioc.put(beanName,instance);
                        // 2.3判断这个类 是不是实现类 找到他的接口
                        for (Class<?> i:clazz.getInterfaces()){
                            /* 拿到接口的全类名 在IOC容器中查找 如果出现过 证明 这个接口有很多实现类 并且没有自定义 这时候要抛出异常
                                因为sprig中的 value 不能重复
                            */
                            if(ioc.containsKey(i.getName())){
                                throw  new Exception("The"+i.getName()+"is exists!");
                            }
                            ioc.put(i.getName(),instance);
                        }
                }else {
                    continue;
                }

            }
        }catch (Exception e){
            e.printStackTrace();
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
    //初始化url和Method的一对一对应关系
//    private void initHandlerMapping() {
//        if(ioc.isEmpty()){ return; }
//
//        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
//            Class<?> clazz = entry.getValue().getClass();
//
//            if(!clazz.isAnnotationPresent(GPController.class)){continue;}
//
//            //保存写在类上面的@GPRequestMapping("/demo")
//            String baseUrl = "";
//            if(clazz.isAnnotationPresent(GPRequestMapping.class)){
//                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
//                baseUrl = requestMapping.value();
//            }
//
//            //默认获取所有的public方法
//            for (Method method : clazz.getMethods()) {
//                if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}
//
//                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
//                //优化
//                // //demo///query
//                String url = ("/" + baseUrl + "/" + requestMapping.value())
//                        .replaceAll("/+","/");
//                handlerMapping.put(url,method);
//                System.out.println("Mapped :" + url + "," + method);
//
//            }
//
//        }
//
//    }
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
//    private void doAutowired() {
//        if(ioc.isEmpty()){return;}
//
//        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
//            //Declared 所有的，特定的 字段，包括private/protected/default
//            //正常来说，普通的OOP编程只能拿到public的属性
//            Field[] fields = entry.getValue().getClass().getDeclaredFields();
//            for (Field field : fields) {
//                if(!field.isAnnotationPresent(GPAutowired.class)){continue;}
//                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
//
//                //如果用户没有自定义beanName，默认就根据类型注入
//                //这个地方省去了对类名首字母小写的情况的判断，这个作为课后作业
//                //小伙伴们自己去完善
//                String beanName = autowired.value().trim();
//                if("".equals(beanName)){
//                    //获得接口的类型，作为key待会拿这个key到ioc容器中去取值
//                    beanName = field.getType().getName();
//                }
//
//                //如果是public以外的修饰符，只要加了@Autowired注解，都要强制赋值
//                //反射中叫做暴力访问， 强吻
//                field.setAccessible(true);
//
//                try {
//                    //用反射机制，动态给字段赋值
//                    field.set(entry.getValue(),ioc.get(beanName));
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        }

 //   }
}

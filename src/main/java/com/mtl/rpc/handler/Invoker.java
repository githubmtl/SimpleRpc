package com.mtl.rpc.handler;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.ResponseImpl;
import com.mtl.rpc.message.ResponseStatus;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 说明：
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 14:50
 */
public class Invoker implements Runnable {
    Logger logger= LoggerFactory.getLogger(Invoker.class);
    private ChannelHandlerContext handlerContext;
    private ApplicationContext applicationContext;
    private RequestImpl request;

    public Invoker(ChannelHandlerContext handlerContext, RequestImpl request,ApplicationContext applicationContext) {
        this.handlerContext = handlerContext;
        this.request = request;
        this.applicationContext=applicationContext;
    }

    @Override
    public void run() {
        try {
            //通过反射拿到接口对象的class对象
            Class<?> clazz = Class.forName(request.getItfName());
            //再从Spring容器里面拿出其实现类
            Object bean = applicationContext.getBean(clazz);
            //获取实现类的class对象
            Class<?> implClass = bean.getClass();
            //获取请求方法的Method对象
            Method method = implClass.getMethod(request.getMethodName(), request.getParamClassType(request.getArgs()));
            //通过反射执行其方法
            Object retObj = method.invoke(bean, request.getArgs());
            //组织返回对象
            ResponseImpl response=new ResponseImpl(request.getRequestId(),ResponseStatus.OK);
            response.setMessageType(request.getMessageType());
            if (retObj==null||retObj instanceof Void){
                response.setResponseType(Void.class);
            }else{
                response.setContent(JSON.toJSONString(retObj));
                boolean isarray = isArray(retObj);
                boolean iscollection = isCollection(retObj);
                response.setArrayFlag(isarray);
                response.setCollectionFlag(iscollection);
                Class<?> retClass = retObj.getClass();
                if (response.isArrayFlag()){
                    response.setArrayType(retClass);
                    response.setResponseType(retClass.getComponentType());
                }else if (response.isCollectionFlag()){
                    Collection collection = (Collection) retObj;
                    for (Object o : collection) {
                        response.setResponseType(o.getClass());
                        break;
                    }
                }else{
                    response.setResponseType(retClass);
                };
            }
            handlerContext.channel().writeAndFlush(response);
        }catch (ClassNotFoundException e){
            logger.error("server not found error! channel:"+handlerContext.channel()+"\n RequestMessage:"+request,e);
            ResponseImpl response=new ResponseImpl(request.getRequestId(), ResponseStatus.ERROR);
            response.setErrorMsg("server [" +request.getItfName()+ "] not found!");
            handlerContext.channel().writeAndFlush(response);
        }catch (NoSuchMethodException me){
            logger.error("method not found error! channel:"+handlerContext.channel()+"\n RequestMessage:"+request,me);
            ResponseImpl response=new ResponseImpl(request.getRequestId(), ResponseStatus.ERROR);
            response.setErrorMsg("method [" +request.getMethodName()+ "] not found!");
            handlerContext.channel().writeAndFlush(response);
        }catch (Exception ee){
            logger.error("method invoke error! channel:"+handlerContext.channel()+"\n RequestMessage:"+request,ee);
            ResponseImpl response=new ResponseImpl(request.getRequestId(), ResponseStatus.ERROR);
            response.setErrorMsg("method [" +request.getMethodName()+ "] invoke error!"+ee.toString());
            handlerContext.channel().writeAndFlush(response);
        }
    }

    private boolean isArray(Object o){
        if (o instanceof Object[]
                ||o instanceof int[] ||o instanceof byte[]
                ||o instanceof long[]||o instanceof float[] ||o instanceof double[])
            return true;
        return false;
    }
    private boolean isCollection(Object o){
        if (o instanceof Collection)
            return true;
        return false;
    }
}

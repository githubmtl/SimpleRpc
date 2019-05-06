package com.mtl.rpc.proxy;

import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.ServerSelector;
import com.mtl.rpc.config.RpcClientConfiguration;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.message.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * 说明：使用JDK动态代理实现
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 13:28
 */
public class ClientProxyInvocation implements InvocationHandler {

    private static final Logger logger= LoggerFactory.getLogger(ClientProxyInvocation.class);

    private Class itfclass;

    private List<ServerInfo> serverList;

    public ClientProxyInvocation(List<ServerInfo> serverList,Class itfclass) {
        this.serverList = serverList;
        this.itfclass=itfclass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获取到连接
        ServerSelector serverSelector = RpcClientConfiguration.applicationContext.getBean(RpcClientConfiguration.class).getServerSelector();
        //组装请求对象
        RequestImpl request=new RequestImpl(MessageType.SERVER);
        request.setItfName(itfclass.getName());
        request.setMethodName(method.getName());
        request.setResponseType(method.getReturnType().getName());
        request.setArgs(args);
        SynchronousQueue<Response> responses = new SynchronousQueue<>();
        ServerInfo.msgTransferMap.put(request.getRequestId(),responses);
        Response response=null;
        try {
            //发送到远程服务器
            boolean flag=false;
            do {
                flag=send(serverSelector,request);
            }while (!flag);
            //获取结果
            response = responses.take();
        }finally {
            ServerInfo.msgTransferMap.remove(request.getRequestId());
        }
        if (response.getStatus()== ResponseStatus.ERROR){
            throw new AppException(response.getErrorMsg());
        }
        return response.returnObj();
    }

    private boolean send(ServerSelector serverSelector,Request request){
        ServerInfo serverInfo = serverSelector.select(serverList,request);
        Channel channel = serverInfo.getChannel();
        if (!channel.isActive()){
            logger.debug("the channel {} is not acttive,remove it!",channel);
            serverList.remove(serverInfo);
            return false;
        }
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if (!channelFuture.isSuccess()) {
                serverList.remove(serverInfo);
                return false;
            }
        }catch (Exception e){
            logger.error("send request error,will close it!channel:"+channel, e);
            channel.close();
            serverList.remove(serverInfo);
            return false;
        }
        return true;
    }
}

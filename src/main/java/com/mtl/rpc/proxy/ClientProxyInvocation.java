package com.mtl.rpc.proxy;

import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.ServerSelector;
import com.mtl.rpc.config.RpcClientConfiguration;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.message.MessageType;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.ResponseImpl;
import io.netty.channel.Channel;

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
        ServerInfo serverInfo = serverSelector.select(serverList);
        if (serverInfo==null){
            throw new AppException("can not get the Channel!");
        }
        Channel channel = serverInfo.getChannel();
        //组装请求对象
        RequestImpl request=new RequestImpl(MessageType.SERVER);
        request.setItfName(itfclass.getName());
        request.setMethodName(method.getName());
        request.setResponseType(method.getReturnType().getName());
        request.setArgs(args);
        SynchronousQueue<ResponseImpl> responses = new SynchronousQueue<>();
        serverInfo.msgTransferMap.put(request.getRequestId(),responses);
        ResponseImpl take=null;
        try {
            //发送到远程服务器
            channel.writeAndFlush(request);
            //获取结果
            take = responses.take();
        }finally {
            serverInfo.msgTransferMap.remove(request.getRequestId());
        }
        return take.returnObj();
    }
}

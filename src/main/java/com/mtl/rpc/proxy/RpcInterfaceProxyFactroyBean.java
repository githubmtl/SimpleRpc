package com.mtl.rpc.proxy;

import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.config.Constant;
import com.mtl.rpc.config.NettyConfig;
import com.mtl.rpc.config.RedisRegistCenterConfig;
import com.mtl.rpc.config.RpcClientConfiguration;
import com.mtl.rpc.exception.AppException;
import com.mtl.util.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 说明：用于生产每个Rpc接口服务的代理类
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 13:24
 */
public class RpcInterfaceProxyFactroyBean<T> implements FactoryBean {
    private Class<T> itfClass;
    private NettyConfig nettyConfig;
    private RedisRegistCenterConfig registCenterConfig;

    public RpcInterfaceProxyFactroyBean(Class<T> itfClass) {
        this.itfClass = itfClass;
    }

    @Override
    public T getObject() throws Exception {
        //初始化配置
        if (nettyConfig==null){
            nettyConfig=new NettyConfig();
            RpcClientConfiguration.applicationContext.getBean(RpcClientConfiguration.class).setNettyConfig(nettyConfig);
        }
        if (RedisRegistCenterConfig.getJedisPool()==null){
            if (registCenterConfig==null){
                registCenterConfig=new RedisRegistCenterConfig();
            }
            registCenterConfig.init();
        }
        //获取可以提供该接口服务的IP地址和端口
        List<ServerInfo> serverList=new ArrayList<>();
        Jedis resource = RedisRegistCenterConfig.getJedisPool().getResource();
        try {
            synchronized (ServerInfo.isInit){
                //如果服务器列表没有获取过，则先在redis获取服务器列表
                if (!ServerInfo.isInit){
                    //获取服务列表
                    Set<String> servers = resource.smembers(Constant.REDIS_SERVER_LIST);
                    ServerInfo.isInit=true;
                    if (servers==null||servers.size()==0){
                        throw new AppException("has no servers in center!");
                    }
                    ServerInfo.serverList.addAll(servers);
                }
            }
            //循环获取每台服务里面的服务列表
            for (String server : ServerInfo.serverList) {
                Set<String> serverNames=null;
                //如果服务列表没有获取过，则从redis服务器中获取
                if (!ServerInfo.serverNameMap.containsKey(server)){
                    serverNames = resource.smembers(server);
                    if (serverNames==null||serverNames.size()==0){
                        resource.srem(Constant.REDIS_SERVER_LIST, server);
                    }
                    ServerInfo.serverNameMap.put(server,serverNames);
                }
                if (serverNames!=null&&serverNames.contains(itfClass.getName())){
                    String[] ipAndPort = StringUtils.analysisIpAndPort(server);
                    //分解服务器IP和端口成功
                    if (ipAndPort!=null){
                        //如果该客户端已经和该服务器连接，则不发起连接
                        if (ServerInfo.serverChannels.containsKey(server)){
                            serverList.add(new ServerInfo(ipAndPort[0],Integer.parseInt(ipAndPort[1])));
                        }else{//否则和服务器发起连接
                            nettyConfig.clientInit(ipAndPort[0],Integer.parseInt(ipAndPort[1]));
                            serverList.add(new ServerInfo(ipAndPort[0],Integer.parseInt(ipAndPort[1])));
                        }
                    }
                }
            }
        }finally {
            resource.close();
        }
        if (serverList.size()==0){
            throw new AppException("["+itfClass.getName() +"] has no implements in register center! ");
        }
        //生成连接对象
        Object o = Proxy.newProxyInstance(itfClass.getClassLoader(), new Class[]{itfClass}, new ClientProxyInvocation(serverList,itfClass));
        return (T)o;
    }

    @Override
    public Class<T> getObjectType() {
        return itfClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<T> getItfClass() {
        return itfClass;
    }

    public void setItfClass(Class<T> itfClass) {
        this.itfClass = itfClass;
    }

    public NettyConfig getNettyConfig() {
        return nettyConfig;
    }

    public void setNettyConfig(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    public RedisRegistCenterConfig getRegistCenterConfig() {
        return registCenterConfig;
    }

    public void setRegistCenterConfig(RedisRegistCenterConfig registCenterConfig) {
        this.registCenterConfig = registCenterConfig;
    }

}

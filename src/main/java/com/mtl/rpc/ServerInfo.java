package com.mtl.rpc;

import com.mtl.rpc.config.Constant;
import com.mtl.rpc.message.Response;
import com.mtl.rpc.message.ResponseImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

/**
 * 说明：服务信息
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 14:28
 */
public class ServerInfo {
    private String ip;
    private int port;
    private ChannelHandlerContext handlerContext;
    //服务相关信息是否初始化
    public static volatile Boolean isInit=false;
    //redis里面的服务列表
    public static Set<String> serverList= Collections.synchronizedSet(new HashSet<>());
    //对应redis服务每台服务器提供的服务列表
    public static ConcurrentHashMap<String,Set<String>> serverNameMap=new ConcurrentHashMap<>();
    //保存所有的服务信息，key是ip_port，value是对应的其连接
    public static ConcurrentHashMap<String, Channel> serverChannels=new ConcurrentHashMap<>();
    //用于临时存放响应消息
    public static ConcurrentHashMap<String, SynchronousQueue<Response>> msgTransferMap=new ConcurrentHashMap<>(512);

    public ServerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ServerInfo() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ChannelHandlerContext getHandlerContext() {
        return handlerContext;
    }

    public void setHandlerContext(ChannelHandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    /**
     * 获取该ServerInfo的连接
     * @return
     */
    public Channel getChannel(){
        return serverChannels.get(ip+ Constant.IpAndPortSep+port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return port == that.port &&
                ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }
}

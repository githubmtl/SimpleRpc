package com.mtl.rpc;

import com.mtl.rpc.config.Constant;
import com.mtl.rpc.config.NettyConfig;
import com.mtl.rpc.config.RpcClientConfiguration;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.message.Request;
import com.mtl.rpc.proxy.RpcInterfaceProxyFactroyBean;
import com.mtl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 说明：随机选择器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 16:24
 */
public class RandomServerSelector implements ServerSelector {
    private static final Logger logger= LoggerFactory.getLogger(RandomServerSelector.class);
    private SecureRandom random=new SecureRandom();
    @Override
    public ServerInfo select(List<ServerInfo> serverList, Request request) {
        if (serverList==null||serverList.size()==0){//重新扫描注册中心，并重新连接服务
            RpcClientConfiguration clientConfiguration = RpcClientConfiguration.applicationContext.getBean(RpcClientConfiguration.class);
            NettyConfig nettyConfig = clientConfiguration.getNettyConfig();
            JedisPool jedisPool = RpcInterfaceProxyFactroyBean.getJedisPool();
            Jedis resource = jedisPool.getResource();
            try {
                Set<String> servers = resource.smembers(Constant.REDIS_SERVER_LIST);
                ServerInfo.serverList.addAll(servers);
                Iterator<String> iterator = ServerInfo.serverList.iterator();
                while (iterator.hasNext()){//循环所有服务器列表
                    String server = iterator.next();
                    Set<String> serverNames = resource.smembers(server);
                    if (serverNames==null||serverNames.size()==0){//如果为空，则证明服务器已经没有服务，则删掉
                        iterator.remove();
                        resource.srem(Constant.REDIS_SERVER_LIST, server);
                    }else{
                        if (serverNames.contains(request.getItfName())){//此服务器包含此服务
                            String[] strings = StringUtils.analysisIpAndPort(server);
                            try {
                                nettyConfig.clientInit(strings[0],Integer.parseInt(strings[1]));
                            }catch (Exception e){
                                logger.error("connect server "+ Arrays.toString(strings)+" error!", e);
                                continue;
                            }
                            serverList.add(new ServerInfo(strings[0],Integer.parseInt(strings[1])));
                        }
                    }
                }
            }finally {
                resource.close();
            }
        }
        if(serverList.size()==0){
            throw new  AppException("cant not get the server "+request.getItfName()+" from register center!");
        }
        int i = random.nextInt(serverList.size());
        return serverList.get(i);
    }
}

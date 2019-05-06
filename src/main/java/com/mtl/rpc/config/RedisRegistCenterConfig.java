package com.mtl.rpc.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * 说明：redis注册中心配置
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:14
 */
public class RedisRegistCenterConfig {
    private static final Logger logger= LoggerFactory.getLogger(RedisRegistCenterConfig.class);
    private static volatile JedisPool jedisPool;
    private String ip=Constant.REDIS_DEFAULT_IP;
    private int port=Constant.REDIS_DEFAULT_PORT;
    private String password;
    //连接超时时间
    private int timeOut=Constant.REDIS_CONNECT_TIME_OUT;
    //服务在redis里面的过期时间，服务器会不断向redis注册中心发送expire命令，已保持该服务在线，如果服务挂掉，则不会再发送expire命令，expireSeconds秒后自动从注册中心去掉
    private int expireSeconds=Constant.REDIS_EXPIRE_SECONDS;
    private GenericObjectPoolConfig poolConfig=new GenericObjectPoolConfig();

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
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

    public int getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(int expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public JedisPool init(){
        if (jedisPool!=null){
            return jedisPool;
        }
        RedisRegistCenterConfig.jedisPool=new JedisPool(this.getPoolConfig(),
                this.getIp(),this.getPort(),
                this.getTimeOut(),this.getPassword());
        return jedisPool;
    }

}

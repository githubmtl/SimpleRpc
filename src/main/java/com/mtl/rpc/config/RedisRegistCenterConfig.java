package com.mtl.rpc.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

/**
 * 说明：redis注册中心配置
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:14
 */
public class RedisRegistCenterConfig {
    private String ip;
    private int port;
    private String password;
    private int timeOut;
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

    public JedisPool init(){
        return new JedisPool(this.getPoolConfig(),
                this.getIp(),this.getPort(),
                this.getTimeOut(),this.getPassword());
    }
}

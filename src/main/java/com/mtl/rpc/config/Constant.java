package com.mtl.rpc.config;

/**
 * 说明：定义了一些常量，比如默认配置
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:09
 */
public class Constant {
    //Netty bossgroup 默认线程数
    public static final int NETTY_BOSSGROUP_DEFAULT=1;
    //Netty workergroup 默认线程数
    public static final int NETTY_WORKERROUP_DEFAULT=10;
    //redis里服务器列表的表名
    public static final String REDIS_SERVER_LIST="server_list";
    //本机IP地址
    public static String LOCAL_ADRESS;
    //默认业务线程池核心线程数
    public static final int INVOKE_THREADPOOL_CORE=5;
    //默认业务线程池最大线程数
    public static final int INVOKE_THREADPOOL_MAX=10;
    //默认业务线程池最大队列数
    public static final int INVOKE_THREADPOOL_QUEUE_NUM=10000;
    //默认业务线程池超出核心线程的存活时间，单位秒
    public static final int INVOKE_THREADPOOL_K_L=60;
    //IP prot分隔符
    public static final String IpAndPortSep="_";
    //channel TimeOut Attr Key
    public static final String HEARTBEAT_TIME="HEARTBEAT_TIME";
    //默认参数：服务和redis注册中心之间保持心跳的时间，也是redis中服务的过期时间
    public static final int REDIS_EXPIRE_SECONDS=60;
    //redis连接超时时间
    public static final int REDIS_CONNECT_TIME_OUT=30000;
    //redis默认端口
    public static final int REDIS_DEFAULT_PORT=6379;
    //redis默认IP
    public static final String REDIS_DEFAULT_IP="127.0.0.1";
}

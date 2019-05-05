package com.mtl.rpc.config;

import com.mtl.rpc.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 说明：服务整体配置类
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 14:52
 */
public class RpcServiceConfiguration implements BeanPostProcessor, ApplicationContextAware, InitializingBean, DisposableBean {
    private static Logger logger= LoggerFactory.getLogger(RpcServiceConfiguration.class);

    //SpringContext
    private static ApplicationContext applicationContext;

    private NettyConfig nettyConfig;
    private RedisRegistCenterConfig registCenterConfig;
    //服务监听端口
    private int prot;
    private JedisPool jedisPool;
    private volatile boolean isRegisted=false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RpcServiceConfiguration.applicationContext=applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (clazz.isAnnotationPresent(RpcService.class)){//含有RpcService注解的类
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces!=null&&interfaces.length==1){
                Class<?> ift = interfaces[0];
                String name = ift.getName();
                Jedis resource = jedisPool.getResource();
                try {
                    Long sadd = resource.sadd(Constant.LOCAL_ADRESS, name);
                    if (sadd>0){
                        logger.debug("{} server is published!",clazz.getName());
                        //如果有一个服务已经注册，则开始轮询发送expire命令
                        if (!isRegisted){
                            Timer timer=new Timer(true);
                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    Jedis jedis=null;
                                    try {
                                        jedis=jedisPool.getResource();
                                        Long expire = jedis.expire(Constant.LOCAL_ADRESS, registCenterConfig.getExpireSeconds());
                                        if (expire>0){
                                            logger.debug("[{}] server send expire command successful!",Constant.LOCAL_ADRESS);
                                        }
                                    }catch (Exception e){
                                        logger.error("server send expire command error!", e);
                                    }finally {
                                        if (jedis!=null){
                                            jedis.close();
                                        }
                                    }
                                }
                            }, 0,registCenterConfig.getExpireSeconds()*800);
                            isRegisted=true;
                        }
                    }
                }finally {
                    resource.close();
                }
            }else{
                logger.warn("{} has more one interface or no interface!",clazz.getName());
            }
        }
        return bean;
    }

    @Override
    public void afterPropertiesSet(){
        if (nettyConfig==null){
            nettyConfig=new NettyConfig();
        }

        //初始化Socket服务

        nettyConfig.serverInit(prot);

        //初始化redis连接池
        if (registCenterConfig==null){
            jedisPool=new JedisPool();
        }else{
            jedisPool=registCenterConfig.init();
        }
        Jedis resource = jedisPool.getResource();
        try {
            String lcoalAdress = InetAddress.getLocalHost().getHostAddress()+Constant.IpAndPortSep+prot;
            resource.sadd(Constant.REDIS_SERVER_LIST, lcoalAdress);
            Constant.LOCAL_ADRESS=lcoalAdress;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("add server to redis server_list error!",e);
            jedisPool.close();
            System.exit(-1);
        }finally {
            resource.close();
        }
        logger.debug("init jedisPool successful！");
        //初始化netty
    }

    @Override
    public void destroy(){
        //从注册中心去掉服务
        Jedis resource = jedisPool.getResource();
        try {
            resource.srem(Constant.REDIS_SERVER_LIST,Constant.LOCAL_ADRESS);
            resource.del(Constant.LOCAL_ADRESS);
        }finally {
            resource.close();
        }
        //关闭socket服务
        nettyConfig.close();
        //关闭redis连接池
        if (jedisPool!=null&&!jedisPool.isClosed()){
            jedisPool.close();
            logger.debug("destroy jedisPool successful！");
        }
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

    public int getProt() {
        return prot;
    }

    public void setProt(int prot) {
        this.prot = prot;
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
}

package com.mtl.rpc.config;

import com.mtl.rpc.RandomServerSelector;
import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.ServerSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 说明：RPC客户端配置
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 13:03
 */
public class RpcClientConfiguration implements ApplicationListener<ContextRefreshedEvent>,BeanDefinitionRegistryPostProcessor, ApplicationContextAware, DisposableBean {
    private static Logger logger= LoggerFactory.getLogger(RpcClientConfiguration.class);
    private NettyConfig nettyConfig;
    private RedisRegistCenterConfig registCenterConfig;
    private ServerSelector serverSelector=new RandomServerSelector();
    public static ApplicationContext applicationContext;
    /**
     * 客户端同步服务的间隔时间
     */
    private int synRedisSecond=Constant.SYN_REDIS_SECONDS;
    //Spring容器是否已经启动完成
    private volatile boolean contextStart=false;
    //和注册中心保持服务同步定时器
    private Timer timer;
    /**
     * Rpc接口包名
     */
    private String[] basePachage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        RpcInterfaceScanner scanner=new RpcInterfaceScanner(registry);
        scanner.scan(basePachage);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RpcClientConfiguration.applicationContext=applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    //容器启动完成时，开始定时任务
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!contextStart){
            //启动定时任务，同步redis注册中心的服务
            logger.debug("applicationContext has started,start... the timer!");
            timer=new Timer(true);
            timer.scheduleAtFixedRate(new SynRedisTimerTask(), synRedisSecond*1000, synRedisSecond*1000);
            contextStart=true;
        }
    }

    @Override
    public void destroy() throws Exception {
        //关闭socket服务
        nettyConfig.close();
        //取消定时任务
        if (timer!=null) timer.cancel();
    }

    public String[] getBasePachage() {
        return basePachage;
    }

    public void setBasePachage(String[] basePachage) {
        this.basePachage = basePachage;
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

    public ServerSelector getServerSelector() {
        return serverSelector;
    }

    public void setServerSelector(ServerSelector serverSelector) {
        this.serverSelector = serverSelector;
    }

    public int getSynRedisSecond() {
        return synRedisSecond;
    }

    public void setSynRedisSecond(int synRedisSecond) {
        this.synRedisSecond = synRedisSecond;
    }

    private static class SynRedisTimerTask extends TimerTask{
        @Override
        public void run() {
            RedisRegistCenterConfig registCenterConfig = applicationContext.getBean(RpcClientConfiguration.class).getRegistCenterConfig();
            if (registCenterConfig!=null){
                Jedis resource = null;
                try {
                    resource=registCenterConfig.init().getResource();
                    Set<String> smembers = resource.smembers(Constant.REDIS_SERVER_LIST);
                    Set<String> serverList = ServerInfo.serverList;
                    //如果本地服务列表中没有redis上的服务，则添加到本地
                    for (String smember : smembers) {
                        if (!serverList.contains(smember)) {
                            //如果服务器上面没有服务，从服务器列表删除
                            Long scard = resource.scard(smember);
                            if (scard==0) resource.srem(Constant.REDIS_SERVER_LIST, smember);
                            else{//同步服务列表
                                Set<String> serverNames = resource.smembers(smember);
                                ServerInfo.serverList.add(smember);
                                ServerInfo.newServerNameMap.put(smember, serverNames);
                                logger.debug("同步到新的服务{},服务列表{}"+smember,serverNames);
                            }
                        }
                    }
                    //如果本地有的服务，在redis上没有，则删除他
                    if (serverList.size()>smembers.size()){
                        Iterator<String> iterator = serverList.iterator();
                        while (iterator.hasNext()){
                            String next = iterator.next();
                            if (!smembers.contains(next)) iterator.remove();
                        }
                    }
                    logger.debug("client synchronize redis's servers successful!");
                }catch (Exception e){
                    logger.error("client synchronize redis's servers error!", e);
                }finally {
                    if (resource!=null){
                        resource.close();
                    }
                }
            }
        }
    }
}

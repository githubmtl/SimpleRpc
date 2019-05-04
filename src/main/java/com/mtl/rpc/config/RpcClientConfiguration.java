package com.mtl.rpc.config;

import com.mtl.rpc.RandomServerSelector;
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

/**
 * 说明：RPC客户端配置
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 13:03
 */
public class RpcClientConfiguration implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware, DisposableBean {
    private static Logger logger= LoggerFactory.getLogger(RpcClientConfiguration.class);
    private NettyConfig nettyConfig;
    private RedisRegistCenterConfig registCenterConfig;
    private ServerSelector serverSelector=new RandomServerSelector();
    public static ApplicationContext applicationContext;
    /**
     * Rpc接口包名
     */
    private String[] basePachage;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        RpcInterfaceScanner scanner=new RpcInterfaceScanner(registry,nettyConfig,registCenterConfig);
        scanner.scan(basePachage);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RpcClientConfiguration.applicationContext=applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void destroy() throws Exception {
        //关闭socket服务
        nettyConfig.close();
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
}

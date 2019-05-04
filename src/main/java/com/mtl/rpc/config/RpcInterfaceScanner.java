package com.mtl.rpc.config;

import com.mtl.rpc.proxy.RpcInterfaceProxyFactroyBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Set;

/**
 * 说明：扫描固定包下的接口生产代理类并注入到Spring
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 13:05
 */
public class RpcInterfaceScanner extends ClassPathBeanDefinitionScanner {
    private NettyConfig nettyConfig;
    private RedisRegistCenterConfig registCenterConfig;
    public RpcInterfaceScanner(BeanDefinitionRegistry registry,NettyConfig nettyConfig,RedisRegistCenterConfig redisRegistCenterConfig) {
        super(registry, false);
        this.nettyConfig=nettyConfig;
        this.registCenterConfig=redisRegistCenterConfig;
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        addFliter();
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (beanDefinitionHolders!=null&&beanDefinitionHolders.size()>0){
            //生成接口的动态代理类，类似MyBatis为mapper生成代理类
            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(beanDefinition.getBeanClassName());
                //通过FactoryBean创建
                beanDefinition.setBeanClass(RpcInterfaceProxyFactroyBean.class);
                beanDefinition.getPropertyValues().add("nettyConfig",nettyConfig);
                beanDefinition.getPropertyValues().add("registCenterConfig",registCenterConfig);
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
        return beanDefinitionHolders;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface()&&metadata.isIndependent();
    }

    private void addFliter(){
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                return true;
            }
        });
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

package com.mtl.rpc.annotation;

import java.lang.annotation.*;

/**
 * 说明：用此注解标记的实现类，表示它是一个远程调用服务，在Spring启动的时候，会将此服务发布到远程注册中心
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 10:58
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {
    /**
     * 可以指定一个服务名称
     */
    String value() default "";
}

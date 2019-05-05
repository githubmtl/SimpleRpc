package com.mtl.rpc.message;

/**
 * 说明：
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:46
 */
public interface Request {

    /**
     * 获取远程调用方法参数类型
     * @param args
     * @return
     */
    Class<?>[] getParamClassType(Object[] args);

    /**
     * 获取接口名称
     * @return
     */
    public String getItfName();

    /**
     * 获取请求方法名称
     * @return
     */
    public String getMethodName();

    /**
     * 获取请求方法参数
     * @return
     */
    public Object[] getArgs();
}

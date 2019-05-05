package com.mtl.rpc.message;

import com.mtl.util.StringUtils;

import java.util.Arrays;

/**
 * 说明：Request的简单实现
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:49
 */
public class AbstractRequest implements Request {
    /**
     * 远程调用的接口全名
     */
    private String itfName;
    /**
     * 远程调用的方法名
     */
    private String methodName;
    /**
     * 远程调用的参数
     */
    private Object[] args;

    @Override
    public Class<?>[] getParamClassType(Object[] args) {
        if (args==null||args.length==0) return null;
        Class<?>[] clazzs=new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i]==null) throw new IllegalArgumentException("args can not be null! index :"+i);
            clazzs[i]=args[i].getClass();
        }
        return clazzs;
    }

    @Override
    public String getItfName() {
        return itfName;
    }

    public void setItfName(String itfName) {
        this.itfName = itfName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "AbstractRequest{" +
                "itfName='" + itfName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}


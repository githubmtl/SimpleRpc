package com.mtl.rpc.message;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.exception.AppException;
import com.mtl.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /**
     * 参数的Json格式
     */
    private List<String> paramContents;

    /**
     * 方法参数的class全名
     */
    protected List<String> paramClassNames;

    @Override
    public Class<?>[] getParamClassType() {
        if (paramClassNames==null||paramClassNames.size()==0) return null;
        Class clazzs[]=new Class[paramClassNames.size()];
        for (int i = 0; i < paramClassNames.size(); i++) {
            try {
                clazzs[i]=Class.forName(paramClassNames.get(i));
            }catch (ClassNotFoundException e){
                throw new AppException("param class ["+paramClassNames.get(i)+"] not found!");
            }
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


    public void setArgs(Object[] args) {
        this.args = args;
        if (args==null||args.length==0) {
            paramClassNames=null;
            return ;
        }
        paramClassNames=new ArrayList<>();
        paramContents=new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i]==null) throw new IllegalArgumentException("args can not be null! index :"+i);
            paramClassNames.add(args[i].getClass().getName());
            paramContents.add(JSON.toJSONString(args[i]));
        }
    }

    public List<String> getParamClassNames() {
        return paramClassNames;
    }

    public void setParamClassNames(List<String> paramClassNames) {
        this.paramClassNames = paramClassNames;
    }

    @Override
    public Object[] getArgs() {
        if (paramClassNames==null) return null;
        Class<?>[] paramClassType = getParamClassType();
        Object[] objects=new Object[paramClassNames.size()];
        for (int i = 0; i < paramClassType.length; i++) {
            objects[i]=JSON.parseObject(paramContents.get(i), paramClassType[i]);
        }
        return objects;
    }

    public List<String> getParamContents() {
        return paramContents;
    }

    public void setParamContents(List<String> paramContents) {
        this.paramContents = paramContents;
    }

    @Override
    public String toString() {
        return "AbstractRequest{" +
                "itfName='" + itfName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", paramContents=" + paramContents +
                ", paramClassNames=" + paramClassNames +
                '}';
    }
}


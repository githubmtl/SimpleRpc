package com.mtl.rpc.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.mtl.rpc.exception.AppException;

/**
 * 说明：抽象返回对象
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 13:44
 */
public abstract class AbstractResponse implements Response {
    /**
     * 调用的状态码
     */
    private ResponseStatus status;
    /**
     * 调用的错误信息
     */
    private String errorMsg;

    /**
     * 返回对象的json串
     */
    private String content;
    /**
     * 返回对象是否是数组
     */
    private boolean arrayFlag;

    /**
     * 返回对象是否为集合
     */
    private boolean collectionFlag;

    /**
     * 返回值类型
     */
    private Class responseType;

    /**
     * 数组对象类型
     */
    private Class arrayType;

    /**
     * 请求类型
     */
    private MessageType messageType;

    public AbstractResponse(ResponseStatus status) {
        this.status = status;
    }

    public AbstractResponse() {
    }


    @Override
    public Object returnObj() throws AppException {
        Object o=null;
        if (content!=null){
            try {
                if (arrayFlag){
                    o= JSONArray.parseObject(content,arrayType);
                }else{
                    o=JSON.parseObject(content,responseType);
                }
            }catch (Exception e){
                throw new AppException("parse json error!",e);
            }
        }
        return o;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setArrayFlag(boolean arrayFlag) {
        this.arrayFlag = arrayFlag;
    }

    public boolean isArrayFlag() {
        return arrayFlag;
    }

    public String getContent() {
        return content;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isCollectionFlag() {
        return collectionFlag;
    }

    public void setCollectionFlag(boolean collectionFlag) {
        this.collectionFlag = collectionFlag;
    }

    public Class getResponseType() {
        return responseType;
    }

    public void setResponseType(Class responseType) {
        this.responseType = responseType;
    }

    public Class getArrayType() {
        return arrayType;
    }

    public void setArrayType(Class arrayType) {
        this.arrayType = arrayType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "AbstractResponse{" +
                "status=" + status +
                ", errorMsg='" + errorMsg + '\'' +
                ", content='" + content + '\'' +
                ", arrayFlag=" + arrayFlag +
                ", collectionFlag=" + collectionFlag +
                ", responseType=" + responseType +
                ", arrayType=" + arrayType +
                ", messageType=" + messageType +
                '}';
    }
}

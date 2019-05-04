package com.mtl.rpc.message;

import java.util.UUID;

/**
 * 说明：远程调用请求数据
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:40
 */
public class RequestImpl extends AbstractRequest{
    //请求ID，唯一标识每一个请求
    private String requestId;
    //请求时的时间戳
    private long requestTimestamp;
    //超时时间
    private int timeOut;
    //客户端IP
    private String remoteIp;
    //加密字符串
    private String md5;
    //是否MD5加密
    private boolean encryptFlag;
    //消息类型
    private MessageType messageType;
    //方法返回类型，没有返回值，传void
    private String responseType;
    //返回值是否是数组
    private String arrayFlag;

    public RequestImpl() {
    }

    public RequestImpl(MessageType messageType) {
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestamp = System.currentTimeMillis();
        this.messageType=messageType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isEncryptFlag() {
        return encryptFlag;
    }

    public void setEncryptFlag(boolean encryptFlag) {
        this.encryptFlag = encryptFlag;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getArrayFlag() {
        return arrayFlag;
    }

    public void setArrayFlag(String arrayFlag) {
        this.arrayFlag = arrayFlag;
    }


    @Override
    public String toString() {
        return "RequestImpl{" +
                "requestId='" + requestId + '\'' +
                ", requestTimestamp=" + requestTimestamp +
                ", timeOut=" + timeOut +
                ", remoteIp='" + remoteIp + '\'' +
                ", md5='" + md5 + '\'' +
                ", encryptFlag=" + encryptFlag +
                ", messageType=" + messageType +
                ", responseType='" + responseType + '\'' +
                ", arrayFlag='" + arrayFlag + '\'' +
                "} " + super.toString();
    }
}

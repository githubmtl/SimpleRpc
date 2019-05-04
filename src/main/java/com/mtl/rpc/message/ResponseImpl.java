package com.mtl.rpc.message;

/**
 * 说明：返回对象的简单实现
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 14:19
 */
public class ResponseImpl extends AbstractResponse {
    //请求ID
    private String requestId;
    //返回时间
    private long resposeTimestamp;
    //加密字符串
    private String md5;
    //是否MD5加密
    private boolean encryptFlag;

    public ResponseImpl() {
        resposeTimestamp=System.currentTimeMillis();
        encryptFlag=false;
    }

    public ResponseImpl(String requestId,ResponseStatus responseStatus) {
        super(responseStatus);
        this.requestId = requestId;
        resposeTimestamp=System.currentTimeMillis();
        encryptFlag=false;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getResposeTimestamp() {
        return resposeTimestamp;
    }

    public void setResposeTimestamp(long resposeTimestamp) {
        this.resposeTimestamp = resposeTimestamp;
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

    @Override
    public String toString() {
        return "ResponseImpl{" +
                "requestId='" + requestId + '\'' +
                ", resposeTimestamp=" + resposeTimestamp +
                ", md5='" + md5 + '\'' +
                ", encryptFlag=" + encryptFlag +
                "} " + super.toString();
    }
}

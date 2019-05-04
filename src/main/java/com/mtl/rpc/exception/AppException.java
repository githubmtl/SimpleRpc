package com.mtl.rpc.exception;

/**
 * 说明：异常对象
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 13:52
 */
public class AppException extends RuntimeException {
    public AppException() {
    }

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppException(Throwable cause) {
        super(cause);
    }

    public AppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

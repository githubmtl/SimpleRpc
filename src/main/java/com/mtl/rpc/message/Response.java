package com.mtl.rpc.message;

import com.mtl.rpc.exception.AppException;

/**
 * 说明：远程调用返回对象
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 13:30
 */
public interface Response {
    //返回真正的方法返回值
    public Object returnObj() throws AppException;
}

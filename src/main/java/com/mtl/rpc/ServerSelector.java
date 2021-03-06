package com.mtl.rpc;

import com.mtl.rpc.message.Request;

import java.util.List;

/**
 * 说明：服务选择器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 16:23
 */
public interface ServerSelector  {
    /**
     * 选择一台服务器进行请求
     * @param serverList
     * @param request
     * @return
     */
    public ServerInfo select(List<ServerInfo> serverList, Request request);
}

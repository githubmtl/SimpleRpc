package com.mtl.rpc.codec;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.message.RequestImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 说明：客户端Json编码器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 16:34
 */
public class ClientJsonEncoder extends MessageToMessageEncoder<RequestImpl> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestImpl request, List<Object> list) throws Exception {
        list.add(JSON.toJSONString(request));
    }
}

package com.mtl.rpc.codec;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.message.ResponseImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * 说明：将Resonse对象编码成字符串
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 14:06
 */
public class ServerJsonEncoder extends MessageToMessageEncoder<ResponseImpl> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ResponseImpl response, List<Object> list) throws Exception {
        String s = JSON.toJSONString(response);
        list.add(s);
    }
}

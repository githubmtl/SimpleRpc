package com.mtl.rpc.codec;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.ResponseImpl;
import com.mtl.rpc.message.ResponseStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 说明：客户端Json解码器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 16:28
 */
public class ClientJsonDecoder extends MessageToMessageDecoder<String> {
    private Logger logger= LoggerFactory.getLogger(ClientJsonDecoder.class);
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        try {
            ResponseImpl resp = JSON.parseObject(s, ResponseImpl.class);
            list.add(resp);
        }catch (Exception e){
            logger.error("json parse error! channel :"+channelHandlerContext.channel()+"\nresponseString:"+s,e);
            throw new AppException("client parse json error!{0}"+s);
        }
    }
}

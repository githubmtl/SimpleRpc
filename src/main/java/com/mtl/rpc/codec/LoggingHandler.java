package com.mtl.rpc.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 说明：自定义日志记录
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 14:57
 */
public class LoggingHandler extends MessageToMessageDecoder<String> {
    private Logger logger= LoggerFactory.getLogger(LoggingHandler.class);
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        if (logger.isDebugEnabled()){
            System.out.println("received message :"+s);
        }
        logger.info("received message :"+s);
        list.add(s);
    }
}

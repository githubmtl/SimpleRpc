package com.mtl.rpc.codec;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.ResponseImpl;
import com.mtl.rpc.message.ResponseStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 说明：将JSon字符串转换为JAVA对象
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 14:10
 */
public class ServerJsonDecoder extends MessageToMessageDecoder<String> {
    private static Logger logger= LoggerFactory.getLogger(ServerJsonDecoder.class);
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String request, List<Object> list) throws Exception {
        try {
            RequestImpl req = JSON.parseObject(request, RequestImpl.class);
            list.add(req);
        }catch (Exception e){
            logger.error("json parse error! channel :"+channelHandlerContext.channel()+"\nrequestString:"+request,e);
            ResponseImpl response=new ResponseImpl();
            response.setStatus(ResponseStatus.ERROR);
            response.setErrorMsg(e.toString());
            channelHandlerContext.channel().writeAndFlush(response);
        }
    }
}

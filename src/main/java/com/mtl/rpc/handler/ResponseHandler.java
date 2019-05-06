package com.mtl.rpc.handler;

import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.config.Constant;
import com.mtl.rpc.message.MessageType;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.Response;
import com.mtl.rpc.message.ResponseImpl;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.SynchronousQueue;

/**
 * 说明：客户端结果处理器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/03 16:00
 */
public class ResponseHandler extends SimpleChannelInboundHandler<ResponseImpl> {
    private static Logger logger= LoggerFactory.getLogger(ResponseHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseImpl response) throws Exception {
        if (response.getMessageType()==MessageType.HEARTBEAT){//如果收到的是心跳，则将心跳次数减一
            Attribute<Object> attr = channelHandlerContext.channel().attr(AttributeKey.valueOf(Constant.HEARTBEAT_TIME));
            if (attr.get()!=null){
                Integer times = (Integer) attr.get();
                attr.set(--times);
                logger.debug("receive heartbeat and response successful!");
            }
        }else if (response.getMessageType()==MessageType.SERVER){
            logger.debug("recrive message response successful!messageId={}"+response.getRequestId());
            SynchronousQueue<Response> queue = ServerInfo.msgTransferMap.get(response.getRequestId());
            queue.put(response);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleState state = ((IdleStateEvent) evt).state();
        if (state==IdleState.ALL_IDLE){//如果超过1分钟客户端和服务端没有通信，则发送心跳
            Attribute<Object> attr = ctx.channel().attr(AttributeKey.valueOf(Constant.HEARTBEAT_TIME));
            if (attr.get()==null){
                attr.set(new Integer(0));
            }
            //已经发出去的心跳检查次数（未收到回复，收到回复将其值-1）
            Integer times = (Integer) attr.get();
            if (times<=3){//再次发送心跳
                RequestImpl request=new RequestImpl(MessageType.HEARTBEAT);
                ctx.channel().writeAndFlush(request).sync();
                attr.set(++times);
            }else{
                //尝试重连
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("handl response error!",cause);
        cause.printStackTrace();
    }
}

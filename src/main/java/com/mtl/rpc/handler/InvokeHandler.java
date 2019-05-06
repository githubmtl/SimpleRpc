package com.mtl.rpc.handler;

import com.alibaba.fastjson.JSON;
import com.mtl.rpc.config.NettyConfig;
import com.mtl.rpc.config.RpcServiceConfiguration;
import com.mtl.rpc.message.MessageType;
import com.mtl.rpc.message.RequestImpl;
import com.mtl.rpc.message.ResponseImpl;
import com.mtl.rpc.message.ResponseStatus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 说明：业务处理器
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/02 14:19
 */
public class InvokeHandler extends SimpleChannelInboundHandler<RequestImpl> {
    private static Logger logger= LoggerFactory.getLogger(InvokeHandler.class);

    private static ThreadPoolExecutor executor=null;

    //初始化业务线程池
    static {
        NettyConfig nettyConfig = RpcServiceConfiguration.getApplicationContext().getBean(RpcServiceConfiguration.class).getNettyConfig();
        executor=new ThreadPoolExecutor(nettyConfig.getInvokeThreadpoolCore(), nettyConfig.getInvokeThreadpoolmax(),
                nettyConfig.getInvokeThreadpoolKeepAliveTime(), TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(nettyConfig.getInvokeThreadpoolQueueNum()),
                new InvokeThreadFactoy(), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                throw new RejectedExecutionException();
            }
        });
    }

    private static class InvokeThreadFactoy implements ThreadFactory{
        private int count;
        @Override
        public Thread newThread(Runnable r) {
            Thread t=new Thread(r);
            t.setName("InvokeThread-"+(count++));
            return t;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestImpl request) throws Exception {
        if (request.getMessageType()== MessageType.HEARTBEAT){//如果是心跳。直接返回
            logger.debug("{} heartbeat successful!",channelHandlerContext.channel());
            ResponseImpl response=new ResponseImpl(request.getRequestId(), ResponseStatus.OK);
            response.setMessageType(request.getMessageType());
            channelHandlerContext.channel().writeAndFlush(response);
        }else if (request.getMessageType()== MessageType.SERVER){
            RpcServiceConfiguration rpcServiceConfiguration = RpcServiceConfiguration.getApplicationContext().getBean(RpcServiceConfiguration.class);
            //判断Spring服务是否已经启动
            if (rpcServiceConfiguration==null||!rpcServiceConfiguration.isContextStart()){
                ResponseImpl response=new ResponseImpl(request.getRequestId(),ResponseStatus.ERROR);
                response.setErrorMsg("system has not started!,please try again later!");
                channelHandlerContext.channel().writeAndFlush(response);
            }
            try {
                executor.submit(new Invoker(channelHandlerContext,request,RpcServiceConfiguration.getApplicationContext()));
            }catch (RejectedExecutionException re){
                logger.error("server busy error! channel:"+channelHandlerContext.channel()+" \nRequestMessage:"+request,re);
                ResponseImpl response=new ResponseImpl(request.getRequestId(),ResponseStatus.ERROR);
                response.setErrorMsg("system busy,please try again later!");
                channelHandlerContext.channel().writeAndFlush(response);
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent e = (IdleStateEvent) evt;
            IdleState state = e.state();
            if (state==IdleState.READER_IDLE){
                logger.error("channel {} timeout!will be close it!",ctx.channel());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("system error! will be close the channel! channel:"+ctx.channel(),cause);
        ctx.close();
    }


    /**
     * 关闭业务线程池
     */
    public static void closeThreadPool(){
        if (executor!=null){
            executor.shutdown();
            logger.debug("handler business threadPool closed!");
        }
    }
}
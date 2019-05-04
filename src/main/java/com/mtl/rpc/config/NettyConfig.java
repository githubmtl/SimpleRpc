package com.mtl.rpc.config;

import com.mtl.rpc.ServerInfo;
import com.mtl.rpc.codec.*;
import com.mtl.rpc.exception.AppException;
import com.mtl.rpc.handler.InvokeHandler;
import com.mtl.rpc.handler.ResponseHandler;
import com.mtl.rpc.message.MessageType;
import com.mtl.rpc.message.Request;
import com.mtl.rpc.message.RequestImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * 说明：Netty配置信息
 *
 * @author 莫天龙
 * @email 92317919@qq.com
 * @dateTime 2019/05/01 11:36
 */
public class NettyConfig {
    private static Logger logger= LoggerFactory.getLogger(NettyConfig.class);
    private int bossGroupCount=Constant.NETTY_BOSSGROUP_DEFAULT;
    private int workerGroupCount=Constant.NETTY_WORKERROUP_DEFAULT;
    //业务线程池相关配置
    private int invokeThreadpoolCore=Constant.INVOKE_THREADPOOL_CORE;
    private int invokeThreadpoolmax=Constant.INVOKE_THREADPOOL_MAX;
    private int invokeThreadpoolQueueNum=Constant.INVOKE_THREADPOOL_QUEUE_NUM;
    private long invokeThreadpoolKeepAliveTime=Constant.INVOKE_THREADPOOL_K_L;

    private volatile NioEventLoopGroup worker=null;
    private volatile NioEventLoopGroup boss=null;
    /**
     * 用于加密数据，保证网络传输数据不会被串改
     */
    private String encryptSalt;

    public int getBossGroupCount() {
        return bossGroupCount;
    }

    public void setBossGroupCount(int bossGroupCount) {
        this.bossGroupCount = bossGroupCount;
    }

    public int getWorkerGroupCount() {
        return workerGroupCount;
    }

    public void setWorkerGroupCount(int workerGroupCount) {
        this.workerGroupCount = workerGroupCount;
    }

    public String getEncryptSalt() {
        return encryptSalt;
    }

    public void setEncryptSalt(String encryptSalt) {
        this.encryptSalt = encryptSalt;
    }

    public NioEventLoopGroup getWorker() {
        return worker;
    }

    public void setWorker(NioEventLoopGroup worker) {
        this.worker = worker;
    }

    public int getInvokeThreadpoolCore() {
        return invokeThreadpoolCore;
    }

    public int getInvokeThreadpoolmax() {
        return invokeThreadpoolmax;
    }

    public int getInvokeThreadpoolQueueNum() {
        return invokeThreadpoolQueueNum;
    }

    public void setInvokeThreadpoolCore(int invokeThreadpoolCore) {
        this.invokeThreadpoolCore = invokeThreadpoolCore;
    }

    public void setInvokeThreadpoolmax(int invokeThreadpoolmax) {
        this.invokeThreadpoolmax = invokeThreadpoolmax;
    }

    public void setInvokeThreadpoolQueueNum(int invokeThreadpoolQueueNum) {
        this.invokeThreadpoolQueueNum = invokeThreadpoolQueueNum;
    }

    public long getInvokeThreadpoolKeepAliveTime() {
        return invokeThreadpoolKeepAliveTime;
    }

    public void setInvokeThreadpoolKeepAliveTime(long invokeThreadpoolKeepAliveTime) {
        this.invokeThreadpoolKeepAliveTime = invokeThreadpoolKeepAliveTime;
    }

    public void setBoss(NioEventLoopGroup boss) {
        this.boss = boss;
    }

    public NioEventLoopGroup getBoss() {
        return boss;
    }

    //初始化服务socket服务
    protected synchronized void serverInit(final int port){
        final NettyConfig neetyConfig=this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                NioEventLoopGroup boss = new NioEventLoopGroup(bossGroupCount);
                NioEventLoopGroup worker = null;
                if (neetyConfig.getWorker()==null){
                   worker = new NioEventLoopGroup(workerGroupCount);
                }
                neetyConfig.setWorker(worker);
                neetyConfig.setWorker(boss);
                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(boss, worker).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 512)
                            .childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true);
                    serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //监控超时时间
                            pipeline.addLast(new IdleStateHandler(5l,5l,5l, TimeUnit.MINUTES));
                            //报文4个字节长度
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            //添加自己的日志记录处理器
                            pipeline.addLast(new LoggingHandler());
                            //Json编解码器
                            pipeline.addLast(new ServerJsonDecoder());
                            pipeline.addLast(new ServerJsonEncoder());
                            //业务处理Handler
                            pipeline.addLast(new InvokeHandler());
                        }
                    });
                    ChannelFuture future = serverBootstrap.bind(port).sync();
                    logger.debug("start socket server successful! port: {0}",port);
                    ChannelFuture sync = future.channel().closeFuture().sync();
                } catch (Exception e) {
                    logger.error("init socket server failed!", e);
                    if (RpcServiceConfiguration.getApplicationContext() instanceof ConfigurableApplicationContext) {
                        ((ConfigurableApplicationContext) RpcServiceConfiguration.getApplicationContext()).close();
                    }
                } finally {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                    logger.debug("socket server close successful!");
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    //客户端连接服务初始化
    public synchronized void clientInit(String ip,int port){
        NioEventLoopGroup worker=null;
        if (this.getWorker()==null) worker=new NioEventLoopGroup(workerGroupCount);

        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(worker).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS,1000*20);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new IdleStateHandler(1,1,1,TimeUnit.MINUTES));
                //报文4个字节长度
                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new StringDecoder());
                pipeline.addLast(new StringEncoder());
                //Json编解码器
                pipeline.addLast(new ClientJsonDecoder());
                pipeline.addLast(new ClientJsonEncoder());
                pipeline.addLast(new ResponseHandler());
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            ServerInfo.serverChannels.put(ip+Constant.IpAndPortSep+port,channelFuture.channel());
        }catch (Exception ie){
            throw new AppException("connect server["+ip+":"+port+"] error!",ie);
        }
    }

    protected void close(){
        if (this.getBoss()!=null){
            this.getBoss().shutdownGracefully();
            this.setBoss(null);
            logger.debug("socket boss close successful!");
        }
        if (this.getWorker()!=null){
            this.getWorker().shutdownGracefully();
            this.setWorker(null);
            logger.debug("socket worker close successful!");
        }
    }
}

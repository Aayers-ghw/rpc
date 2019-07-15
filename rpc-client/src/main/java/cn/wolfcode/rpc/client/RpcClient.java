package cn.wolfcode.rpc.client;

import cn.wolfcode.rpc.common.RpcDecoder;
import cn.wolfcode.rpc.common.RpcEncoder;
import cn.wolfcode.rpc.common.RpcRequest;
import cn.wolfcode.rpc.common.RpcResponse;
import cn.wolfcode.rpc.register.RpcDiscover;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * RPC通信客户端,往服务端发送请求,并且接受服务端的响应
 *
 * @author Aayers-ghw
 * @date 2019/7/15 15:28
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    /**
     * 消息响应对象
     */
    private RpcResponse rpcResponse;
    /**
     * 消息请求对象
     */
    private RpcRequest rpcRequest;
    /**
     * 同步锁 资源对象
     */
    private Object object = new Object();
    /**
     * 用于获取服务地址列表信息
     */
    private RpcDiscover rpcDiscover;

    /**
     * 构造函数
     * @param rpcRequest
     * @param rpcDiscover
     */
    public RpcClient(RpcRequest rpcRequest, RpcDiscover rpcDiscover) {
        this.rpcDiscover = rpcDiscover;
        this.rpcRequest = rpcRequest;
    }

    /**
     * 该方法用于接收从客户端接收的信息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg)
            throws Exception {
        //响应消息
        this.rpcResponse = msg;
        synchronized (object) {
            ctx.flush();//刷新缓存
            object.notifyAll();//唤醒等待
        }
    }

    /**
     * 发送消息
     *
     * @return
     * @throws Exception
     */
    public RpcResponse send() throws Exception {
        //创建一个socket通信对象
        Bootstrap client = new Bootstrap();
        //创建一个通信组,负责Channel(通道)的I/O事件的处理
        NioEventLoopGroup loopGroup = new NioEventLoopGroup();
        try {
            //设置参数
            client.group(loopGroup)
                    //使用异步socket通信
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcEncoder(RpcRequest.class))//编码请求对象
                                    .addLast(new RpcDecoder(RpcResponse.class))//解码响应对象
                                    .addLast(RpcClient.this);//发送请求对象
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            //获取一个服务器地址
            String serverAddress = rpcDiscover.discover();
            String host = serverAddress.split(":")[0];
            int port = Integer.valueOf(serverAddress.split(":")[1]);
            ChannelFuture future = client.connect(host, port).sync();
            System.out.println("客户端准备发送数据:" + rpcRequest);
            future.channel().writeAndFlush(rpcRequest).sync();
            synchronized (object) {
                object.wait();//线程等待,等待客户端响应
            }
            if (rpcResponse != null) {
                future.channel().closeFuture().sync();//等待服务端关闭socket
            }
            return rpcResponse;
        } finally {
            loopGroup.shutdownGracefully();//优雅关闭socket
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}

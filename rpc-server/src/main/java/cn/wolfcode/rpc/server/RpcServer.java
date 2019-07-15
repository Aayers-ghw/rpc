package cn.wolfcode.rpc.server;

import cn.wolfcode.rpc.common.RpcDecoder;
import cn.wolfcode.rpc.common.RpcEncoder;
import cn.wolfcode.rpc.common.RpcRequest;
import cn.wolfcode.rpc.common.RpcResponse;
import cn.wolfcode.rpc.register.RpcRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC服务端启动,实现Spring的感知接口
 *
 * @author Aayers-ghw
 * @date 2019/7/15 14:02
 */
@Data
public class RpcServer implements ApplicationContextAware, InitializingBean {
    /**
     * 用于保存所有提供服务的方法, 其中key为类的全路径名, value是所有的实现类
     */
    private final Map<String, Object> serviceBeanMap = new HashMap<String, Object>();
    /**
     * rpcRegistry 用于注册相关的地址信息
     */
    private RpcRegistry rpcRegistry;
    /**
     * 提供服务的地址信息 格式为 192.168.158.151:9000 类似
     */
    private String serverAddress;

    /**
     * 在Spring容器启动完成后会执行该方法
     *
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //获取到所有贴了RpcService注解的Bean对象
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object object : serviceBeanMap.values()) {
                //获取到类的路径名称
                String serviceName = object.getClass().getAnnotation(RpcService.class).value().getName();
                //把获取到的信息保存到serviceBeanMap中
                this.serviceBeanMap.put(serviceName, object);
            }
        }
        System.out.println("服务器: " + serverAddress + " 提供的服务列表: " + serviceBeanMap);
    }

    /**
     * 初始化完成后执行
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        //创建服务端的通信对象 ServerBootstrap负责建立服务端
        ServerBootstrap server = new ServerBootstrap();
        // 创建异步通信的事件组 用于建立TCP连接的
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 创建异步通信的事件组 用于处理Channel(通道)的I/O事件
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //开始设置server的相关参数
            server.group(bossGroup, workerGroup)
                    //启动异步ServerSocket 指定使用NioServerSocketChannel产生一个Channel用来接收连接
                    .channel(NioServerSocketChannel.class)
                    //初始化通道信息 用于向你的Channel当中添加ChannelInboundHandler的实现
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new RpcDecoder(RpcRequest.class))//1 解码请求参数
                                    .addLast(new RpcEncoder(RpcResponse.class))//2 编码响应信息
                                    .addLast(new RpcServerHandler(serviceBeanMap));//3 请求处理
                        }
                    })
                    //对Channel进行一些配置
                    //注意以下是socket的标准参数
                    //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    //Option是为了NioServerSocketChannel设置的，用来接收传入连接的
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    //childOption是用来给父级ServerChannel之下的Channels设置参数的
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            //获取到主机地址
            String host = serverAddress.split(":")[0];
            //端口
            int port = Integer.valueOf(serverAddress.split(":")[1]);
            //开启异步通信服务
            ChannelFuture future = server.bind(host, port).sync();
            System.out.println("服务器启动成功:" + future.channel().localAddress());
            rpcRegistry.createNode(serverAddress);
            System.out.println("向zkServer注册服务地址信息");
            future.channel().closeFuture().sync();//等待通信完成
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //优雅的关闭socket
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

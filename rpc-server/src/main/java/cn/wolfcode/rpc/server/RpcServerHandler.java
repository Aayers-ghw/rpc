package cn.wolfcode.rpc.server;

import cn.wolfcode.rpc.common.RpcRequest;
import cn.wolfcode.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

/**
 * @author Aayers-ghw
 * @date 2019/7/15 14:18
 */
@Setter
@Getter
@NoArgsConstructor
public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    private Map<String, Object> serviceBeanMap;

    public RpcServerHandler(Map<String, Object> serviceBeanMap) {
        this.serviceBeanMap = serviceBeanMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("RpcServerHandler.channelRead");
        System.out.println(msg);
        RpcRequest rpcRequest = (RpcRequest) msg;
        RpcResponse rpcResponse = handler(rpcRequest);
        //告诉客户端,关闭socket连接
        ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private RpcResponse handler(RpcRequest rpcRequest) {
        //创建一个响应消息对象
        RpcResponse rpcResponse = new RpcResponse();
        //设置响应消息ID
        rpcResponse.setResponseId(UUID.randomUUID().toString());
        //请求消息ID
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            //获取到类名(接口名称)
            String className = rpcRequest.getClassName();
            //获取到方法名
            String methodName = rpcRequest.getMethodName();
            //获取到参数类型列表
            Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
            //获取到参数列表
            Object[] parameters = rpcRequest.getParameters();
            //获取到具字节码对象
            Class<?> clz = Class.forName(className);
            //获取到实现类
            Object serviceBean = serviceBeanMap.get(className);
            if (serviceBean == null) {
                throw new RuntimeException(className + "没有找到对应的serviceBean:" + className + ":beanMap:" + serviceBeanMap);
            }
            //反射调用方法
            Method method = clz.getMethod(methodName, parameterTypes);
            if (method == null) {
                throw new RuntimeException("没有找到对应的方法");
            }
            Object result = method.invoke(serviceBean, parameters);
            rpcResponse.setSuccess(true);
            //设置方法调用的结果
            rpcResponse.setResult(result);
        } catch (Exception e) {
            rpcResponse.setSuccess(false);
            rpcResponse.setThrowable(e);
            e.printStackTrace();
        }
        return rpcResponse;
    }
}

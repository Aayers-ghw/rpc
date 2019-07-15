package cn.wolfcode.rpc.client;

import cn.wolfcode.rpc.common.RpcRequest;
import cn.wolfcode.rpc.common.RpcResponse;
import cn.wolfcode.rpc.register.RpcDiscover;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 动态代理类,用于获取到每个类的代理对象
 * 对于被代理对象的所有的方法调用都会执行invoke方法
 *
 * @author Aayers-ghw
 * @date 2019/7/15 15:24
 */
@Getter
@Setter
public class RpcProxy {
    /**
     * 用于获取到RPC-Server的地址信息
     */
    private RpcDiscover rpcDiscover;

    @SuppressWarnings("all")
    public <T> T getInstance(Class<T> interfaceClass) {

        T instance = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}, new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //创建请求对象
                        RpcRequest rpcRequest = new RpcRequest();
                        //获取到被调用的类名 和RPC-Server中的serviceMap中的key进行匹配
                        String className = method.getDeclaringClass().getName();
                        //获取到方法的参数列表
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        //生成一个请求的id
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(className);//类名
                        rpcRequest.setParameterTypes(parameterTypes);//参数类型列表
                        rpcRequest.setParameters(args);//参数列表
                        rpcRequest.setMethodName(method.getName());//调用的方法名称
                        //创建一个RPCclient对象,并且发送消息到服务端
                        RpcResponse rpcResponse = new RpcClient(rpcRequest, rpcDiscover).send();
                        //返回调用结果
                        return rpcResponse.getResult();
                    }
                });
        //返回一个代理对象
        return instance;
    }
}

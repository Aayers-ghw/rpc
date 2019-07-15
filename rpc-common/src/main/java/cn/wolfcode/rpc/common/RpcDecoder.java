package cn.wolfcode.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 对传递的消息进行解码, 接受到的数据是字节数组,需要把数组转换为对应的请求/响应消息对象
 *
 * @author Aayers-ghw
 */
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * 传递的数据的对象类型
     */
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //消息的长度
        int size = in.readableBytes();
        //保证所有的消息都完全接受完成
        if (size < 4) {
            return;
        }
        byte[] bytes = new byte[size];
        //把传递的字节数组读取到bytes中
        in.readBytes(bytes);
        // 反序列化为对象(RPCRequest/RPCResponse对象)
        Object object = SerializationUtil.deserialize(bytes, genericClass);
        //输出对象
        out.add(object);
        //刷新缓存
        ctx.flush();
    }
}

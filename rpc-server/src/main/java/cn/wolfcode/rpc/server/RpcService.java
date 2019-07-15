package cn.wolfcode.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这个注解用于贴在每个提供服务的实现类
 * 在Spring容器启动的时候,自动扫描到贴了该注解的所有的服务
 *
 * @author Aayers-ghw
 * @date 2019/7/15 14:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RpcService {
    Class<?> value();
}

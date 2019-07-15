package cn.wolfcode.rpc.register;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aayers-ghw
 */
@Setter
@Getter
@AllArgsConstructor()
@NoArgsConstructor
public class RpcRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger(RpcRegistry.class);
    /**
     * zkServer的地址信息
     */
    private String registryAddress;
    /**
     * zk客户端程序
     */
    private ZooKeeper zooKeeper;

    public void createNode(String data) throws Exception {
        //创建一个客户端程序, 对于注册可以不用监听事件
        zooKeeper = new ZooKeeper(registryAddress, Contant.SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent event) {

            }
        });
        if (zooKeeper != null) {
            try {
                //判断注册的目录是否存在
                Stat exists = zooKeeper.exists(Contant.REGISTRY_PATH, false);
                if (exists == null) {
                    //如果不存在，则创建一个
                    zooKeeper.create(Contant.REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
                //创建一个临时的序列节点,并且保存数据信息
                zooKeeper.create(Contant.DATA_PATH, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                LOGGER.info("创建一个节点成功");
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        } else {
            LOGGER.debug("zooKeeper connect is null");
        }
    }

}

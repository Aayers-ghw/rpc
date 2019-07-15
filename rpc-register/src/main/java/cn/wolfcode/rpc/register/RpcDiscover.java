package cn.wolfcode.rpc.register;

import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 地址发现,用于实时的获取最新的RPC服务信息
 *
 * @author Aayers-ghw
 */
@Setter
@Getter
public class RpcDiscover {
    public static final Logger LOGGER = LoggerFactory.getLogger(RpcDiscover.class);
    /**
     * 服务端地址 zkServer的地址
     */
    private String registryAddress;
    /**
     * 获取到的所有提供服务的服务器列表
     */
    private volatile List<String> dataList = new ArrayList<String>();

    private ZooKeeper zooKeeper;

    /**
     * 初始化zkClient客户端
     *
     * @param registryAddress
     */
    public RpcDiscover(String registryAddress) throws Exception {
        this.registryAddress = registryAddress;
        zooKeeper = new ZooKeeper(registryAddress, Contant.SESSION_TIMEOUT, new Watcher() {
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    //监听zkServer的服务器列表变化
                    watchNode();
                }
            }
        });
        //获取节点相关数据
        watchNode();
    }

    /**
     * 从dataList列表随机获取一个可用的服务端的地址信息给rpc-client
     */
    public String discover() {
        int size = dataList.size();
        if (size > 0) {
            int index = new Random().nextInt(size);
            return dataList.get(index);
        }
        throw new RuntimeException("没有找到对应的服务器");
    }

    /**
     * 监听服务端列表信息
     */
    public void watchNode() {
        try {
            //获取子结点信息
            List<String> nodeList = zooKeeper.getChildren(Contant.DATA_PATH, true);
            List<String> dataList = new ArrayList<String>();
            for (String node : nodeList) {
                byte[] bytes = zooKeeper.getData(Contant.REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            this.dataList = dataList;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}

package cn.wolfcode.app.client;

import cn.wolfcode.app.common.IProductService;
import cn.wolfcode.app.common.Product;
import cn.wolfcode.rpc.client.RpcProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

/**
 * 模拟客户端启动
 *
 * @author Aayers-ghw
 * @date 2019/7/15 15:59
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application.xml")
public class ApplicationTest {
    @Autowired
    private RpcProxy rpcProxy;

    private IProductService productService;

    @Before
    public void init() {
        productService = rpcProxy.getInstance(IProductService.class);
    }


    @Test
    public void testSave() throws Exception {
        productService.save(new Product(2L, "001", "烤鸭", BigDecimal.TEN));
    }

    @Test
    public void testDelete() throws Exception {
        productService.deleteById(2L);
    }

    @Test
    public void testUpdate() throws Exception {
        productService.update(new Product(2L, "001", "烤鸭", BigDecimal.ONE));
    }

    @Test
    public void testGet() throws Exception {
        Product product = productService.get(1L);
        System.out.println("获取到的产品信息为:" + product);
    }
}

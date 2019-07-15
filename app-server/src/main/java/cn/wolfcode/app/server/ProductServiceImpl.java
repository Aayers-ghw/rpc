package cn.wolfcode.app.server;

import cn.wolfcode.app.common.IProductService;
import cn.wolfcode.app.common.Product;
import cn.wolfcode.rpc.server.RpcService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Aayers-ghw
 * @date 2019/7/15 15:51
 */
@Component
@RpcService(IProductService.class)
public class ProductServiceImpl implements IProductService {

    public void save(Product product) {
        System.out.println("产品保存成功: "+product);
    }

    public void deleteById(Long productId) {
        System.out.println("产品删除成功: "+ productId);
    }

    public void update(Product product) {
        System.out.println("产品修改成功: "+ product);
    }

    public Product get(Long productId) {
        System.out.println("产品获取成功");
        return new Product(1L,"001","笔记本电脑", BigDecimal.TEN);
    }
}

package cn.wolfcode.app.common;

import lombok.*;

import java.math.BigDecimal;

/**
 * 产品信息
 * @author Aayers-ghw
 * @date 2019/7/15 15:46
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    /**
     * id
     */
    private Long id;
    /**
     * 产品编号
     */
    private String sn;
    /**
     * 产品名称
     */
    private String name;
    /**
     * 产品价格
     */
    private BigDecimal price;
}

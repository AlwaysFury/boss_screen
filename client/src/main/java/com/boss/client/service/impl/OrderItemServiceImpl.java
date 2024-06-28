package com.boss.client.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.client.dao.CostDao;
import com.boss.client.dao.EscrowItemDao;
import com.boss.client.dao.OrderItemDao;
import com.boss.client.service.OrderItemService;
import com.boss.client.vo.OrderEscrowItemVO;
import com.boss.client.enities.Cost;
import com.boss.common.enities.EscrowItem;
import com.boss.common.enities.OrderItem;
import com.boss.common.util.BeanCopyUtils;
import com.boss.common.vo.SelectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItem> implements OrderItemService {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private EscrowItemDao escrowItemDao;

    @Autowired
    private CostServiceImpl costService;

    @Autowired
    private CostDao costDao;

    @Override
    public List<OrderEscrowItemVO> getOrderEscrowItemVOBySn(String orderSn) {

        // 初始化衣服数量 map
        Map<String, Integer> clothesCountMap = new HashMap<>();
        // 双面
        clothesCountMap.put("double", 0);
        for (SelectVO vo : costService.getCostType()) {
            clothesCountMap.put(vo.getValue(), 0);
        }

        List<OrderEscrowItemVO> orderEscrowItemVOList = orderItemDao.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn)).stream().map(
                orderItem -> {
                    OrderEscrowItemVO orderEscrowItemVO = BeanCopyUtils.copyObject(orderItem, OrderEscrowItemVO.class);

                    // 计算衣服数量
                    String clothesType = getClothesType(orderItem.getModelSku().toLowerCase());
                    if (clothesCountMap.get(clothesType) != null) {
                        clothesCountMap.put(clothesType, clothesCountMap.get(clothesType) + 1);
                    }

                    // 计算双面
                    String itemSku = orderItem.getItemSku();
                    if (!"notsure".equals(itemSku) && isEnglish(itemSku.substring(0, 1)) && isEnglish(itemSku.substring(1, 2))) {
                        clothesCountMap.put("double", clothesCountMap.get("double") + 1);
                    }

                    EscrowItem escrowItem = escrowItemDao.selectOne(new QueryWrapper<EscrowItem>()
                            .eq("order_sn", orderSn)
                            .eq("item_id", orderEscrowItemVO.getItemId())
                            .eq("model_id", orderEscrowItemVO.getModelId()));
                    if (escrowItem != null) {
                        orderEscrowItemVO.setOriginalPrice(escrowItem.getOriginalPrice());
                        orderEscrowItemVO.setSellingPrice(escrowItem.getSellingPrice());
                        orderEscrowItemVO.setDiscountedPrice(escrowItem.getDiscountedPrice());
                        orderEscrowItemVO.setSellerDiscount(escrowItem.getSellerDiscount());
                        orderEscrowItemVO.setActivityId(escrowItem.getActivityId());
                        orderEscrowItemVO.setActivityType(escrowItem.getActivityType());
                        orderEscrowItemVO.setCount(escrowItem.getCount());

                        // 计算成本利润
                        JSONObject costObject = getCostObject(clothesType, orderItem.getCreateTime());
                        // 成本
                        BigDecimal costPrice = costObject.getBigDecimal("cost");
                        // 利率
                        double rate = costObject.getDouble("rate");

                        BigDecimal cost = costPrice.multiply(BigDecimal.valueOf(orderEscrowItemVO.getCount())).divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
                        BigDecimal oldPrice = orderEscrowItemVO.getSellerDiscount();
                        if (oldPrice.compareTo(BigDecimal.valueOf(0.0)) == 0) {
                            oldPrice = orderEscrowItemVO.getSellingPrice();
                        }
                        BigDecimal price = oldPrice.multiply(BigDecimal.valueOf(orderEscrowItemVO.getCount())).divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
                        BigDecimal profit = price.subtract(cost);
                        float profitRate = 1;
                        if (price.compareTo(BigDecimal.valueOf(0.0)) != 0) {
                            profitRate = profit.divide(price, 2, RoundingMode.HALF_UP).floatValue();
                        }

                        orderEscrowItemVO.setCost(cost);
                        orderEscrowItemVO.setProfit(profit);
                        orderEscrowItemVO.setProfitRate(profitRate);
                    }

                    return orderEscrowItemVO;
                }
        ).collect(Collectors.toList());

        return orderEscrowItemVOList;
    }

    private JSONObject getCostObject(String type, LocalDateTime orderCreateTime) {
        BigDecimal costPrice = new BigDecimal(0.00);
        double rate = 1.00;
        QueryWrapper<Cost> costQueryWrapper = new QueryWrapper<>();
        costQueryWrapper.select("price", "start_time", "end_time", "exchange_rate").eq("type", type);
        List<Cost> costList = costDao.selectList(costQueryWrapper);
        for (Cost cost : costList) {
            if (orderCreateTime.isBefore(cost.getEndTime()) && orderCreateTime.isAfter(cost.getStartTime())) {
                costPrice = cost.getPrice();
                rate = cost.getExchangeRate();
                break;
            }
        }

        JSONObject object = new JSONObject();
        object.put("cost", costPrice);
        object.put("rate", rate);

        return object;
    }

    /**
     * 获取衣服类型
     * @param modelSku
     */
    private String getClothesType(String modelSku) {
        String type = "";
        if (modelSku.contains("t-shirt")) {
            // 如果包含 t-shirt 就直接添加
            type = "t-shirt";
        } else {
            String[] skuSplit = modelSku.substring(0, modelSku.indexOf('(')).split("-");
            if (skuSplit.length == 3) {
                // 如果只有三段，默认为 100%cotton
                type = "100%cotton";
            } else {
                // 其他直接获取第二个 - 的值
                type = skuSplit[1];
            }
        }
        return type;
    }

    private static final Pattern ENGLISH_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private boolean isEnglish(String str) {
        if (str == null) {
            return false;
        }
        Matcher matcher = ENGLISH_PATTERN.matcher(str);
        return matcher.matches();
    }
}

package com.boss.task.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.boss.common.enities.Shop;
import com.boss.task.dao.ShopDao;
import com.boss.task.service.FeishuService;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/7/11
 */
@Service
@Slf4j
public class FeishuServiceImpl implements FeishuService {

    @Value("${feishu.url}")
    private String feishuUrl;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Override
    public void sendAdsMessage() {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取订单
        long shopId;
        String accessToken;

        JSONArray contentArray = new JSONArray();
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));

            JSONObject resultObject = ShopeeUtil.getTotalBalance(accessToken, shopId);

            if (resultObject.getString("error").contains("error") || resultObject == null || resultObject.getJSONObject("response") == null) {
                continue;
            }

            BigDecimal bigDecimal = resultObject.getJSONObject("response").getBigDecimal("total_balance");
            if (bigDecimal.compareTo(new BigDecimal(100)) <= 0) {
                JSONArray singleContentArray = new JSONArray();
                JSONObject contentObject = new JSONObject();
                contentObject.put("tag", "text");
                contentObject.put("text", "店铺 " + shopId + " 广告余额不足 100，当前余额：" + bigDecimal);

                singleContentArray.add(contentObject);

                contentArray.add(singleContentArray);
            }

        }

        if (contentArray.size() != 0) {
            JSONObject bodyObject = new JSONObject();

            JSONArray singleContentArray = new JSONArray();
            JSONObject contentObject = new JSONObject();
            contentObject.put("tag", "at");
            contentObject.put("user_id", "all");
            singleContentArray.add(contentObject);
            contentArray.add(singleContentArray);

            JSONObject postContent = new JSONObject();
            postContent.put("content", contentArray);
            postContent.put("title", "广告余额告警：");

            JSONObject post = new JSONObject();
            post.put("zh_cn", postContent);

            JSONObject allContentObject = new JSONObject();
            allContentObject.put("post", post);

            bodyObject.put("msg_type", "post");
            bodyObject.put("content", allContentObject);

            log.info("广告余额告警发送内容：{}", bodyObject);

            String result = HttpUtil.post(feishuUrl, bodyObject.toJSONString());

            log.info("广告余额告警发送结果：{}", result);
        }
    }
}

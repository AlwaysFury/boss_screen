package com.boss.task.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.PayoutInfo;
import com.boss.common.enities.Shop;
import com.boss.task.dao.PayoutInfoDao;
import com.boss.task.dao.ShopDao;
import com.boss.task.service.PayoutInfoService;
import com.boss.task.util.CommonUtil;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志服务
 */
@Service
@Slf4j
public class PayoutInfoServiceImpl extends ServiceImpl<PayoutInfoDao, PayoutInfo> implements PayoutInfoService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ShopServiceImpl shopService;

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refreshPayoutInfoByTime(String startTime, String endTime) {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取订单
        long shopId;
        String accessToken;
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));
            List<Long[]> result = CommonUtil.splitIntoEveryNDaysTimestamp(startTime, endTime, 14);

            JSONArray allArray = new JSONArray();
            for (Long[] pair : result) {
                JSONArray array = ShopeeUtil.getPayoutInfo(accessToken, shopId, pair[0], pair[1], 0, new JSONArray());
                if (array.isEmpty()) {
                    continue;
                }
                allArray.addAll(array);
            }

            for (int i = 0; i < allArray.size(); i++) {
                refreshBillingTransactionInfo(allArray.getJSONObject(i), shopId);
            }
        }
    }

    private void refreshBillingTransactionInfo(JSONObject object, long shopId) {
        long payoutTime = object.getLong("payout_time");
        JSONArray ids = new JSONArray();
        ids.add(object.getString("encrypted_payout_id"));
        String accessToken = shopService.getAccessTokenByShopId(String.valueOf(shopId));
        JSONArray array = ShopeeUtil.getBillingTransactionInfo(accessToken, shopId, ids, "", new JSONArray());

        if (array.isEmpty()) {
            return;
        }

        Map<String, JSONArray> map = new HashMap<>();

        for (int i = 0; i < array.size(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);

            String level = jsonObject.getString("level");

            if ("ADJUSTMENT".equals(jsonObject.getString("billing_transaction_type")) && "ORDER".equals(level)) {
                 String orderSn = jsonObject.getString("order_sn");
                JSONArray tempArray;
                if (map.get(orderSn) != null) {
                    tempArray = map.get(orderSn);
                } else {
                    tempArray = new JSONArray();
                }
                tempArray.add(jsonObject);
                map.put(orderSn, tempArray);
            }
        }

        List<String> orderSnList = new ArrayList<>();
        List<PayoutInfo> payoutInfoList = new ArrayList<>();
        for (String key : map.keySet()) {
            orderSnList.add(key);
            PayoutInfo payoutInfo = PayoutInfo.builder()
                    .id(key)
                    .orderSn(key)
                    .data(map.get(key).toJSONString())
                    .payoutTime(payoutTime).build();
            payoutInfoList.add(payoutInfo);
        }

        this.saveOrUpdateBatch(payoutInfoList);

        List<List<String>> newOrderSnList = new ArrayList<>();
        if (orderSnList.size() > 20) {
            for (int i = 0; i < orderSnList.size(); i += 20) {
                newOrderSnList.add(orderSnList.subList(i, Math.min(i + 20, orderSnList.size())));
            }
        } else {
            newOrderSnList.add(orderSnList);
        }

        escrowInfoService.refreshEscrowBySn(newOrderSnList, shopId);

    }


}

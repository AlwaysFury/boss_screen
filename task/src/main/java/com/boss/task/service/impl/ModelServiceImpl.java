package com.boss.task.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.common.enities.Model;
import com.boss.common.enities.OperationLog;
import com.boss.task.dao.ModelDao;
import com.boss.task.dao.OperationLogDao;
import com.boss.task.dao.OrderItemDao;
import com.boss.task.service.ModelService;
import com.boss.task.util.RedisUtil;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;

import static com.boss.common.constant.OptTypeConst.SYSTEM_LOG;
import static com.boss.common.constant.RedisPrefixConst.CLOTHES_TYPE;
import static com.boss.common.constant.RedisPrefixConst.PRODUCT_ITEM_MODEL;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class ModelServiceImpl extends ServiceImpl<ModelDao, Model> implements ModelService {

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private OperationLogDao operationLogDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Override
    public void getModel(long itemId, String token, long shopId, List<Model> modelList) {
        try {
            JSONObject result = ShopeeUtil.getModelList(token, shopId, itemId);
            if (result == null || result.getString("error").contains("error")) {
                return;
            }

            JSONArray modelArray = result.getJSONObject("response").getJSONArray("model");
            JSONArray imageInfoArray = new JSONArray();
            if (result.getJSONObject("response").getJSONArray("tier_variation").size() != 0) {
                imageInfoArray = result.getJSONObject("response").getJSONArray("tier_variation").getJSONObject(0).getJSONArray("option_list");
            }

            if (modelArray.size() == 0) {
                return;
            }

            JSONObject modelObject;
            for (int i = 0; i < modelArray.size(); i++) {
                modelObject = modelArray.getJSONObject(i);

                JSONObject priceInfoObject = modelObject.getJSONArray("price_info").getJSONObject(0);

                if (priceInfoObject.size() == 0) {
                    continue;
                }

                JSONObject stockObject = modelObject.getJSONObject("stock_info_v2").getJSONArray("seller_stock").getJSONObject(0);

                String modelName = modelObject.getString("model_name");
                long modelId = modelObject.getLong("model_id");
                String modelSku = modelObject.getString("model_sku");
                Model model = Model.builder()
                        .id(modelId)
                        .modelId(modelId)
                        .modelName(modelName)
                        .modelSku(modelSku)
                        .skuName(modelSku.split("-")[0])
                        .status(modelObject.getString("model_status"))
                        .currentPrice(priceInfoObject.getBigDecimal("current_price"))
                        .originalPrice(priceInfoObject.getBigDecimal("original_price"))
                        .stock(stockObject.getInteger("stock"))
                        .promotionId(modelObject.getLong("promotion_id"))
                        .itemId(itemId)
                        .shopId(shopId)
                        .build();

                if (imageInfoArray.size() != 0 && modelName.contains(",")) {
                    String imageKey = modelName.split(",")[0];
                    JSONObject imageObject;
                    for (int j = 0; j < imageInfoArray.size(); j++) {
                        imageObject = imageInfoArray.getJSONObject(j);
                        if (imageKey.equals(imageObject.getString("option")) && imageObject.getJSONObject("image") != null) {
                            model.setImageId(imageObject.getJSONObject("image").getString("image_id"));
                            model.setImageUrl(imageObject.getJSONObject("image").getString("image_url"));
                        }
                    }
                }

                String judgeResult = RedisUtil.judgeRedis(redisService, PRODUCT_ITEM_MODEL + itemId + "_" + modelId, modelList, model, Model.class);
                if (!"".equals(judgeResult)) {
                    JSONArray diffArray = JSON.parseObject(judgeResult).getJSONArray("defectsList");
                    if (diffArray != null && !diffArray.isEmpty()) {
                        StringJoiner joiner = new StringJoiner(",");
                        OperationLog operationLog = new OperationLog();
                        operationLog.setOptType(SYSTEM_LOG);
                        for (int j = 0; j < diffArray.size(); j++) {
                            JSONObject defectsObject = diffArray.getJSONObject(j);
                            String key = defectsObject.getJSONObject("travelPath").getString("abstractTravelPath");
                            // "{\"imageId\":\"sg-11134201-7qvfk-lj49s5nrs2kgf7\",\"originalPrice\":299,\"modelId\":184422415413,\"currentPrice\":165,\"modelSku\":\"A742-White-3XL(85-100kg)\",\"promotionId\":444890270810112,\"skuName\":\"A742\",\"itemId\":10199520044,\"modelName\":\"White,3XL(85-100kg)\",\"imageUrl\":\"https://cf.shopee.co.th/file/sg-11134201-7qvfk-lj49s5nrs2kgf7\",\"shopId\":874244879,\"id\":184422415413,\"stock\":9999,\"status\":\"MODEL_NORMAL\"}"
                            if (key.contains("currentPrice")) {
                                joiner.add("价格由 " + defectsObject.getString("expect") + " 变为 " + defectsObject.getString("actual"));
                            }

                        }
                        operationLog.setOptDesc("产品 " + itemId + " 规格 " + modelId + " 发生变化：" + joiner);
                        operationLogDao.insert(operationLog);
                    }
                }

                // 统计衣服种类
                saveClothesType(modelSku.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取衣服类型
     * @param modelSku
     */
    private void saveClothesType(String modelSku) {
        if (modelSku.contains("t-shirt")) {
            // 如果包含 t-shirt 就直接添加
            redisService.set(CLOTHES_TYPE + "t-shirt", "t-shirt");
        } else {
            String[] skuSplit = modelSku.substring(0, modelSku.indexOf('(')).split("-");
            if (skuSplit.length == 3) {
                // 如果只有三段，默认为 100%cotton
                redisService.set(CLOTHES_TYPE + "100%cotton", "100%cotton");
            } else {
                // 其他直接获取第二个 - 的值
                redisService.set(CLOTHES_TYPE + skuSplit[1], skuSplit[1]);
            }
        }
    }
}

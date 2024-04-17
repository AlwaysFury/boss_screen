package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ModelDao;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.service.ModelService;
import com.boss.bossscreen.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Model> getModel(long itemId, String token, long shopId, List<Model> modelList) {
        JSONObject result = ShopeeUtil.getModelList(token, shopId, itemId);
        if (result.getString("error").contains("error")) {
            return modelList;
        }

        JSONArray modelArray = result.getJSONObject("response").getJSONArray("model");

        JSONArray imageInfoArray = result.getJSONObject("response").getJSONArray("tier_variation").getJSONObject(0).getJSONArray("option_list");

        if (modelArray.size() == 0) {
            return modelList;
        }

        JSONObject modelObject;
        for (int i = 0; i < modelArray.size(); i++) {
            modelObject = modelArray.getJSONObject(i);

            JSONObject priceInfoObject = modelObject.getJSONArray("price_info").getJSONObject(0);

            if (priceInfoObject.size() == 0) {
                continue;
            }

            JSONObject stockObject = modelObject.getJSONObject("stock_info_v2").getJSONArray("seller_stock").getJSONObject(0);

            // 为了图片
            String modelName = modelObject.getString("model_name");


            // todo 图片
            Model model = Model.builder()
                    .modelId(modelObject.getLong("model_id"))
                    .modelName(modelName)
                    .modelSku(modelObject.getString("model_sku"))
                    .status(modelObject.getString("model_status"))
                    .currentPrice(priceInfoObject.getBigDecimal("current_price"))
                    .originalPrice(priceInfoObject.getBigDecimal("original_price"))
                    .stock(stockObject.getInteger("stock"))
                    .promotionId(modelObject.getLong("promotion_id"))
                    .itemId(itemId)
                    .build();

            if (modelName.contains(",")) {
                String imageKey = modelName.split(",")[0];
                JSONObject imageObject;
                for (int j = 0; j < imageInfoArray.size(); j++) {
                    imageObject = imageInfoArray.getJSONObject(j);
                    if (imageKey.equals(imageObject.getString("option"))) {
                        model.setImageId(imageObject.getJSONObject("image").getString("image_id"));
                        model.setImageUrl(imageObject.getJSONObject("image").getString("image_url"));
                    }
                }
            }

            modelList.add(model);
        }

        return modelList;
    }
}

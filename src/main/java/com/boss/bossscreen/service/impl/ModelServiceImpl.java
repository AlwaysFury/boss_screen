package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ModelDao;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.service.ModelService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.ModelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.boss.bossscreen.constant.RedisPrefixConst.PRODUCT_ITEM_MODEL;

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
    private RedisServiceImpl redisService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getModel(long itemId, String token, long shopId, List<Model> modelList, List<Model> updateModeList) {
        JSONObject result = ShopeeUtil.getModelList(token, shopId, itemId);
        if (result.getString("error").contains("error")) {
            return;
        }

        JSONArray modelArray = result.getJSONObject("response").getJSONArray("model");

        JSONArray imageInfoArray = result.getJSONObject("response").getJSONArray("tier_variation").getJSONObject(0).getJSONArray("option_list");

        if (modelArray.size() == 0) {
            return;
        }

        JSONObject modelObject;
        Object redisResult;
        for (int i = 0; i < modelArray.size(); i++) {
            modelObject = modelArray.getJSONObject(i);

            JSONObject priceInfoObject = modelObject.getJSONArray("price_info").getJSONObject(0);

            if (priceInfoObject.size() == 0) {
                continue;
            }

            JSONObject stockObject = modelObject.getJSONObject("stock_info_v2").getJSONArray("seller_stock").getJSONObject(0);

            // 为了图片
            String modelName = modelObject.getString("model_name");
            long modelId = modelObject.getLong("model_id");
            Model model = Model.builder()
                    .modelId(modelId)
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

            CommonUtil.judgeRedis(redisService, PRODUCT_ITEM_MODEL + modelId, modelList, updateModeList, model, Model.class);

//            redisResult = redisService.get(PRODUCT_ITEM_MODEL + modelId);
//            String productJsonString = JSON.toJSONString(model, SerializerFeature.WriteMapNullValue);
//            if (Objects.isNull(redisResult)) {
//                // 为空入库
//                redisService.set(PRODUCT_ITEM_MODEL + modelId, productJsonString);
//                modelList.add(model);
//            } else {
//                // 不为空判断更新
//                if (!productJsonString.equals(redisResult)) {
//                    updateModeList.add(JSON.parseObject(redisResult.toString(), Model.class));
//                }
//            }
        }
    }

    public List<ModelVO> getModelVOListByItemId(Long itemId) {
        List<ModelVO> modelVOList = modelDao.selectList(new QueryWrapper<Model>().eq("item_id", itemId))
                .stream().map(model ->
                        BeanCopyUtils.copyObject(model, ModelVO.class)
                ).collect(Collectors.toList());
        return modelVOList;
    }
}

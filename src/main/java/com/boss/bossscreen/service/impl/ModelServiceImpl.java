package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ModelDao;
import com.boss.bossscreen.dao.OperationLogDao;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.enities.OperationLog;
import com.boss.bossscreen.service.ModelService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.CommonUtil;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.ModelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.boss.bossscreen.constant.OptTypeConst.SYSTEM_LOG;
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
    private OperationLogDao operationLogDao;

    @Autowired
    private RedisServiceImpl redisService;

    @Override
    public void getModel(long itemId, String token, long shopId, List<Model> modelList, List<Model> updateModeList) {
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

                String judgeResult = CommonUtil.judgeRedis(redisService, PRODUCT_ITEM_MODEL + itemId + "_" + modelId, modelList, updateModeList, model, Model.class);
                if (!"".equals(judgeResult)) {
                    JSONArray diffArray = JSON.parseObject(judgeResult).getJSONArray("defectsList");
                    if (diffArray.size() != 0) {
                        StringJoiner joiner = new StringJoiner(",");
                        OperationLog operationLog = new OperationLog();
                        operationLog.setOptType(SYSTEM_LOG);
                        operationLog.setStatus(1);
                        for (int j = 0; j < diffArray.size(); j++) {
                            String key = diffArray.getJSONObject(i).getJSONObject("travelPath").getString("abstractTravelPath");
                            joiner.add(key.substring(key.indexOf(".") + 1, key.length()));
                        }
                        operationLog.setOptDesc("产品 " + itemId + " 规格字段发生变化：" + joiner.toString());
                        operationLogDao.insert(operationLog);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

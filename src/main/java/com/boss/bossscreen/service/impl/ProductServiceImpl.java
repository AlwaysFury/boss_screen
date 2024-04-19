package com.boss.bossscreen.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.boss.bossscreen.dao.ProductDao;
import com.boss.bossscreen.dao.ShopDao;
import com.boss.bossscreen.dto.ConditionDTO;
import com.boss.bossscreen.enities.Model;
import com.boss.bossscreen.enities.Product;
import com.boss.bossscreen.enities.Shop;
import com.boss.bossscreen.service.ProductService;
import com.boss.bossscreen.util.BeanCopyUtils;
import com.boss.bossscreen.util.PageUtils;
import com.boss.bossscreen.util.ShopeeUtil;
import com.boss.bossscreen.vo.PageResult;
import com.boss.bossscreen.vo.ProductInfoVO;
import com.boss.bossscreen.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/11
 */

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductDao, Product> implements ProductService {

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private ModelServiceImpl modelService;

    @Autowired
    private ProductDao productDao;

    // todo 优化下拉
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateProduct() {
        // 遍历所有未冻结店铺获取 token 和 shopId
        QueryWrapper<Shop> shopQueryWrapper = new QueryWrapper<>();
        shopQueryWrapper.select("shop_id", "access_token").eq("status", "1");
        List<Shop> shopList = shopDao.selectList(shopQueryWrapper);

        // 根据每个店铺的 token 和 shopId 获取产品
        List<Product> productList = new ArrayList<>();
        List<Model> modelList =  new ArrayList<>();
        long shopId;
        String accessToken = "";
        JSONObject result = new JSONObject();
        for (Shop shop : shopList) {
            shopId = shop.getShopId();
            accessToken = shop.getAccessToken();

            // todo 优化为集合查询
            result = ShopeeUtil.getProducts(accessToken, shopId);

            if (result.getString("error").contains("error")) {
                continue;
            }

            JSONArray itemArray = result.getJSONObject("response").getJSONArray("item");
            if (itemArray.size() == 0) {
                continue;
            }


            for (int i = 0; i < itemArray.size(); i++) {
                JSONObject itemObject = itemArray.getJSONObject(i);
                long itemId = itemObject.getLong("item_id");
                productList = getProductDetail(itemId, accessToken, shopId, productList);
                modelList = modelService.getModel(itemId, accessToken, shopId, modelList);
            }
        }
        // todo 检测入库
        // 将新旧数据全部数据缓存进入 redis
        // 新数据与旧数据进行比较：时间戳
        // key：product:产品id:时间戳
        // value：数据 json 格式化
        // 全量检查！！！！！！
        // 新增：将数据存入新增集合，存入 redis 和 mysql
        // 更新：将更新数据存入更新集合，更新 reids 和 mysql 中的数据
        // 删除：指示标记该条数据被删除！！！不是物理删除，存入删除集合，并在更新 redis 和 mysql 中的数据
        System.out.println(JSONArray.toJSONString(productList));
        this.saveBatch(productList);
        System.out.println(JSONArray.toJSONString(modelList));
        modelService.saveBatch(modelList);
    }

    private List<Product> getProductDetail(long itemId, String token, long shopId, List<Product> productList) {
        JSONObject result = ShopeeUtil.getProductInfo(token, shopId, itemId);

        if (result.getString("error").contains("error")) {
            return productList;
        }

        JSONArray itemArray = result.getJSONObject("response").getJSONArray("item_list");

        JSONObject itemObject;
        JSONArray imgIdArray = new JSONArray();
        JSONArray imgUrlArray = new JSONArray();
        for (int i = 0; i < itemArray.size(); i++) {
            itemObject = itemArray.getJSONObject(i);

            imgIdArray = itemObject.getJSONObject("image").getJSONArray("image_id_list");
            imgUrlArray = itemObject.getJSONObject("image").getJSONArray("image_url_list");

            Product product = Product.builder()
                    .shopId(shopId)
                    .itemId(itemId)
                    .itemName(itemObject.getString("item_name"))
                    .categoryId(itemObject.getLong("category_id"))
                    .createTime(itemObject.getLong("create_time"))
                    .updateTime(itemObject.getLong("update_time"))
                    .itemSku(itemObject.getString("item_sku"))
                    .mainImgUrl(imgUrlArray.getString(0))
                    .mainImgId(imgIdArray.getString(0))
                    .status(itemObject.getString("item_status"))
                    .build();


            imgIdArray = itemObject.getJSONObject("image").getJSONArray("image_id_list");
            if (imgIdArray.size() > 0) {
                product.setMainImgId(imgIdArray.getString(0));
            }
            imgUrlArray = itemObject.getJSONObject("image").getJSONArray("image_url_list");
            if (imgUrlArray.size() > 0) {
                product.setMainImgUrl(imgUrlArray.getString(0));
            }

            productList.add(product);


        }

        return productList;
    }

    public PageResult<ProductVO> productListByCondition(ConditionDTO condition) {
        // 查询分类数量
        Integer count = productDao.productCount(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询分类列表
        List<ProductVO> productList = productDao.productList(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(productList, count);
    }

    @Override
    public ProductInfoVO getProductInfo(Long itemId) {

        Product product = productDao.selectOne(new QueryWrapper<Product>().eq("item_id", itemId));

        ProductInfoVO productInfoVO = BeanCopyUtils.copyObject(product, ProductInfoVO.class);

        productInfoVO.setModelVOList(modelService.getModelVOListByItemId(itemId));

        return productInfoVO;
    }


}

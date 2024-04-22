package com.boss.bossscreen;

import com.alibaba.fastjson.JSONObject;
import com.boss.bossscreen.service.impl.OrderServiceImpl;
import com.boss.bossscreen.service.impl.ProductServiceImpl;
import com.boss.bossscreen.service.impl.ShopServiceImpl;
import com.boss.bossscreen.util.ShopeeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;

@SpringBootTest
class BossScreenApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(ShopeeUtil.getAuthUrl("shop"));
    }

    @Test
    void getToken() throws ParseException, IOException {
        JSONObject object = ShopeeUtil.getShopAccessToken("46684d745556476944714e7878714a62", 1049733134);
        System.out.println("首次获取 token 结果：" + object);

        String access_token = object.getString("access_token");
        String refresh_token = object.getString("refresh_token");

//        JSONObject refreshObject = ShopeeUtil.refreshToken(refresh_token, 1141020,"62484d6c546c7a474c53456b646154464d4b736f6c79437266564c4d6346756b", 109746);
//        System.out.println("刷新 token 结果：" + refreshObject);
    }

    @Test
    void refreshTokenTest() throws ParseException, IOException {
        String refresh_token = "647854746842674a78424d5a6f694353";
        JSONObject refreshObject = ShopeeUtil.refreshToken(refresh_token, 104973197, "shop");
        System.out.println("刷新 token 结果：" + refreshObject);
    }

    @Test
    void getMainToken() throws ParseException, IOException {
        JSONObject object = ShopeeUtil.getAccountAccessToken("6b5a4f6a636e4e737056556c72566257",1859747);
        System.out.println(object);
    }

    @Test
    void getProductsTest() {
        String accessToken = "4b64616e46486f63654369776f694247";
        JSONObject object = ShopeeUtil.getProducts(accessToken, 1017169304);
        System.out.println(object);
    }


    @Test
    void getProductInfoTest() {
        String accessToken = "4b64616e46486f63654369776f694247";
        long itemId = Long.valueOf("25812541180");
        JSONObject object = ShopeeUtil.getProductInfo(accessToken, 1017169304, itemId);
        System.out.println(object);
    }

    @Test
    void getModelListTest() {
        String accessToken = "4b64616e46486f63654369776f694247";
        long itemId = Long.valueOf("25812541180");
        JSONObject object = ShopeeUtil.getModelList(accessToken, 1017169304, itemId);
        System.out.println(object);
    }

    @Test
    void getAttributesTest() {
        String accessToken = "4b64616e46486f63654369776f694247";
        long category_id = Long.valueOf("100352");
        JSONObject object = ShopeeUtil.getAttributes(accessToken, 1017169304, category_id);
        System.out.println(object);
    }

    @Test
    void getCategoryTest() {
        String accessToken = "4b64616e46486f63654369776f694247";
        long category_id = Long.valueOf("100352");
        JSONObject object = ShopeeUtil.getCategory(accessToken, 1017169304, category_id);
        System.out.println(object);
    }

    @Autowired
    private ProductServiceImpl productService;

    @Test
    void saveOrUpdateProduct() {
        productService.saveOrUpdateProduct();
    }

    @Test
    void getOrderList() {
        String accessToken = "4b64616e46486f63654369776f694247";
        JSONObject object = ShopeeUtil.getOrderList(accessToken, 1017169304);
        System.out.println(object);
    }

    @Test
    void getOrderDetail() {
        JSONObject object = ShopeeUtil.getOrderDetail("4b64616e46486f63654369776f694247", 1017169304, "240409APT11HFB");
        System.out.println(object);
    }

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    void saveOrUpdateOrder() {
        orderService.saveOrUpdateOrder();
    }

    @Autowired
    private ShopServiceImpl shopService;
    @Test
    void refreshShopToken() {
        shopService.refreshShopTokenByAccount();
    }

    @Test
    void getItemPromotionTest() {
        JSONObject object = ShopeeUtil.getItemPromotion("4b64616e46486f63654369776f694247", 1017169304, "23248487563");
        System.out.println(object);
    }


    @Test
    void getEscrowDetailTest() {
        JSONObject object = ShopeeUtil.getEscrowDetail("4869704e624374516f7957646b747571", 1017169304, "240405UHE0VN3H");
        System.out.println(object);
    }

    @Test
    void getTrackingNumberTest() {
        JSONObject object = ShopeeUtil.getTrackingNumber("42756d4753794544747a595673537671", 1017169304, "240405UHE0VN3H");
        System.out.println(object);
    }

    @Test
    void createShippingDocumentTest() {
        JSONObject object = ShopeeUtil.createShippingDocument("42756d4753794544747a595673537671", 1017169304, "2404208P1PBPG8");
        System.out.println(object);
    }

}

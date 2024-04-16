package com.boss.bossscreen;

import com.alibaba.fastjson2.JSONObject;
import com.boss.bossscreen.util.ShopeeUtil;
import org.junit.jupiter.api.Test;
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
        String accessToken = "5a52646b6b425a59706f6b4159615169";
        JSONObject object = ShopeeUtil.getProducts(accessToken, 1017169304);
        System.out.println(object);
    }


    @Test
    void getProductInfoTest() {
        String accessToken = "5a52646b6b425a59706f6b4159615169";
        String itemId = "20392169588";
        JSONObject object = ShopeeUtil.getProductInfo(accessToken, 1017169304, itemId);
        System.out.println(object);
    }

    @Test
    void getModelListTest() {
        String accessToken = "7a546e7872636869714458556b786a64";
        String itemId = "20392169588";
        JSONObject object = ShopeeUtil.getModelList(accessToken, 1017169304, itemId);
        System.out.println(object);
    }



}

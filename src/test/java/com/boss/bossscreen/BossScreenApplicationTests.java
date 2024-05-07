package com.boss.bossscreen;

import com.alibaba.fastjson.JSONObject;
import com.boss.bossscreen.service.impl.*;
import com.boss.bossscreen.util.ShopeeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.boss.bossscreen.constant.RedisPrefixConst.CLOTHES_TYPE;

@SpringBootTest
class BossScreenApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(ShopeeUtil.getAuthUrl("shop", "1"));
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
        String accessToken = "5661665276626747684365696f4d5552";
        List<String> itemList = ShopeeUtil.getProducts(accessToken, 1017169304, 0, new ArrayList<String>());
        System.out.println(itemList);
    }


    @Test
    void getProductInfoTest() {
        String accessToken = "52477748795470596b6b684f4b54486f";
        JSONObject object = ShopeeUtil.getProductInfo(accessToken, 1017169304, "24325905585,20287532598");
        System.out.println(object);
    }

    @Test
    void getModelListTest() {
        String accessToken = "6e657a574b544f6a72794f6c6e557044";
        long itemId = Long.valueOf("25302418012");
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
        String startTimeStr = "2023-01-01 00:00:00";
        List<String> orderSnList = new ArrayList<>();
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");

        // 将字符串转换为LocalDate对象
        LocalDate startTime = LocalDate.parse(startTimeStr, formatter);

        LocalDate endTime = LocalDate.from(LocalDateTime.now());

        List<LocalDate[]> timeList = splitIntoEvery15DaysTimestamp(startTime, endTime);
        System.out.println(timeList);
        for (LocalDate[] time : timeList) {
            System.out.println(time[0] + " ======== " + time[1]);
            long start = time[0].atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
            long end = time[1].atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000L;
            List<String> object = ShopeeUtil.getOrderList("6e657a574b544f6a72794f6c6e557044", 1017169304, 0, new ArrayList<>(), start, end);
            System.out.println("多少件："+object.size());
            orderSnList.addAll(object);
        }
        System.out.println("多少件："+orderSnList.size());


    }

    public static List<LocalDate[]> splitIntoEvery15DaysTimestamp(LocalDate startDate, LocalDate endDate) {
        List<LocalDate[]> timestampPairs = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            LocalDate endOfSplitDate = startDate.plusDays(14);
            if (endOfSplitDate.isAfter(endDate)) {
                endOfSplitDate = endDate;
            }
            LocalDate[] pair = new LocalDate[]{
                startDate,
                endOfSplitDate
            };
            timestampPairs.add(pair);
            startDate = endOfSplitDate.plusDays(1);
        }
        return timestampPairs;
    }

    @Test
    void getOrderDetail() {
        JSONObject object = ShopeeUtil.getOrderDetail("64757a6748704a6f5a5350715a4a446a", 1017169304, "240102SQ95FRB7");
        System.out.println(object);
    }

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    void saveOrUpdateOrder() {
        orderService.saveOrUpdateOrder("2024-01-01 00:00:00");
    }

    @Autowired
    private ShopServiceImpl shopService;
    @Test
    void refreshShopToken() {
        shopService.refreshShopToken();
    }

    @Test
    void getItemPromotionTest() {
        JSONObject object = ShopeeUtil.getItemPromotion("7a4e526a4d706679536a69435a4d4e74", 1017169304, "23248487563");
        System.out.println(object);
    }


    @Test
    void getEscrowDetailTest() {
        JSONObject object = ShopeeUtil.getEscrowDetail("5a576f4a704979657953745051435149", 1017169304, "2405029EFSMHJ1");
        System.out.println(object);
    }

    @Test
    void getTrackingNumberTest() {
        JSONObject object = ShopeeUtil.getTrackingNumber("5a576f4a704979657953745051435149", 1017169304, "2405029EFSMHJ1");
        System.out.println(object);
    }

    @Test
    void createShippingDocumentTest() {
        JSONObject object = ShopeeUtil.createShippingDocument("42756d4753794544747a595673537671", 1017169304, "2404208P1PBPG8");
        System.out.println(object);
    }

    @Test
    void createGetTokenById() {
        for (int i = 0; i < 100; i++) {
            new Thread(shopService.getAccessTokenByShopId("1017169304")).start();
        }
    }

    @Test
    void getReturnListByHttpTest() {
        JSONObject object = ShopeeUtil.getReturnListByHttp("694c786c63794a4441457064416c4152", 1017169304);
        System.out.println(object);
    }

    @Autowired
    private ReturnOrderServiceImpl returnOrderService;

    @Test
    void saveOrUpdateReturnOrderTest() {
        returnOrderService.saveOrUpdateReturnOrder();
    }

    @Autowired
    private RedisServiceImpl redisService;
    @Test
    void getCostType() {
        Set<String> keys = redisService.keys(CLOTHES_TYPE + "*");
        List<String> types = new ArrayList<>();
        for (String key : keys) {
            System.out.println(key.substring(key.indexOf(CLOTHES_TYPE) + 1, key.length()));
        }
    }

}

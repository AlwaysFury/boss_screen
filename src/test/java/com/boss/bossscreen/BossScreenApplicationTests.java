package com.boss.bossscreen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.bossscreen.service.impl.*;
import com.boss.bossscreen.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.boss.bossscreen.constant.RedisPrefixConst.CATEGORY;

@SpringBootTest
@Slf4j
@EnableRetry
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

    @Autowired
    private ProductServiceImpl productService;

    @Test
    void saveOrUpdateProduct() {
        productService.saveOrUpdateProduct();
    }

    @Test
    void getOrderList() {
        String startTimeStr = "2024-01-01 00:00:00";
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
            List<String> object = ShopeeUtil.getOrderList("685875684c5a4c576d5a4f7665657143", 874244879, 0, new ArrayList<>(), start, end);
            System.out.println(object.toString());
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
        JSONObject object = ShopeeUtil.getOrderDetail("6a565561557961446d6f697973504843", 1017169304, "240102SQ95FRB7");
        System.out.println(object);
    }

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    void saveOrUpdateOrder() {
        log.info("======开始刷新订单信息");
        long startTime =  System.currentTimeMillis();

        orderService.saveOrUpdateOrder("2024-05-01", "2024-05-31");

        log.info("更新订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    @Autowired
    private ShopServiceImpl shopService;
    @Test
    void refreshShopToken() {
        shopService.refreshShopToken();
    }

    @Test
    void getShopInfoTest() {
        JSONObject object = ShopeeUtil.getShopInfoByHttp("425a566776797646527873684e637643", 1017169304);
        System.out.println(object);
    }

    @Test
    void getCategoryTest() {
        JSONObject object = ShopeeUtil.getCategoryByHttp("425a566776797646527873684e637643", 1017169304);
        JSONArray array = object.getJSONObject("response").getJSONArray("category_list");
        for (int i = 0; i < array.size(); i++) {
            JSONObject categoryObject = array.getJSONObject(i);
            redisService.set(CATEGORY + categoryObject.getLong("category_id"), categoryObject.toJSONString());
        }
        System.out.println(object);
    }

    @Test
    void getItemPromotionTest() {
        JSONObject object = ShopeeUtil.getItemPromotion("7a4e526a4d706679536a69435a4d4e74", 1017169304, "23248487563");
        System.out.println(object);
    }


    @Test
    void getEscrowDetailTest() {
        JSONObject object = ShopeeUtil.getEscrowDetail("655952765848657543524e7947626641", 874244879, "2405015JVKQJ2Y");
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
        String str = "[[\"clothes_type:short\",\"short\"],[\"clothes_type:100%cotton\",\"100%cotton\"],[\"clothes_type:hoodie\",\"hoodie\"],[\"clothes_type:s\",\"s\"],[\"clothes_type:child\",\"child\"]]";
//        Set<String> keys = redisService.keys(CLOTHES_TYPE + "*");
//        JSONArray array = new JSONArray();
//        for (String key : keys) {
//            JSONArray tempArray = new JSONArray();
//            tempArray.add(key);
//            tempArray.add(redisService.get(key));
//            array.add(tempArray);
////            System.out.println(key.substring(key.indexOf(CLOTHES_TYPE) + 1, key.length()));
//        }
//        System.out.println(array.toJSONString());

        JSONArray array = JSONArray.parseArray(str);
        for (int i = 0; i < array.size(); i++) {
            JSONArray jsonArray = array.getJSONArray(i);
            for (int j = 0; j < jsonArray.size(); j++) {
                redisService.set(jsonArray.getString(0), jsonArray.getString(1));
            }
        }
    }

    @Test
    void getCategoryType() {
        //StringBuilder str = new StringBuilder("");
//        Set<String> keys = redisService.keys(CATEGORY + "*");
//        JSONArray array = new JSONArray();
//        for (String key : keys) {
//            JSONArray tempArray = new JSONArray();
//            tempArray.add(key);
//            tempArray.add(redisService.get(key));
//            array.add(tempArray);
//        }
//        System.out.println(array.toJSONString());

        String str = readFile("/Users/fury/workspace/ca.txt");
        JSONArray array = JSONArray.parseArray(str);
        for (int i = 0; i < array.size(); i++) {
            JSONArray jsonArray = array.getJSONArray(i);
            for (int j = 0; j < jsonArray.size(); j++) {
                redisService.set(jsonArray.getString(0), jsonArray.getString(1));
            }
        }

    }

    public String readFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tempContent = content.toString();
        return tempContent;
    }

}

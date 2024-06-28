package com.boss.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.task.service.impl.*;
import com.boss.task.util.ShopeeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
import java.util.Set;

import static com.boss.common.constant.RedisPrefixConst.CATEGORY;
import static com.boss.common.constant.RedisPrefixConst.ESCROW;

@SpringBootTest
@Slf4j
class TaskApplicationTests {

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
        String accessToken = "49444a416f6148506f4a6f7346665177";
        String status = "&item_status=NORMAL&item_status=BANNED&item_status=UNLIST&item_status=REVIEWING";
        List<String> itemList = ShopeeUtil.getProducts(accessToken, 874244879, 0, new ArrayList<String>(), status);
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
        productService.refreshProductByStatus("&item_status=NORMAL&item_status=BANNED&item_status=UNLIST&item_status=REVIEWING");
    }

    @Test
    void getOrderList() {
        String startTimeStr = "2024-05-01 00:00:00";
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
            List<String> object = ShopeeUtil.getOrderList("4762415a576374696571644a6e455750", 874244879, 0, new ArrayList<>(), start, end);
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
        JSONObject object = ShopeeUtil.getOrderDetail("44625a646a6f6778534f57704d5a7a44", 874244879, "240531SAGCUP3N");
        System.out.println(object);
    }

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    void saveOrUpdateOrder() {
        log.info("======开始刷新订单信息");
        long startTime =  System.currentTimeMillis();

//        orderService.refreshOrderByTimeStr("2024-06-01", "2024-06-19");

        log.info("更新订单耗时： {}秒", (System.currentTimeMillis() - startTime) / 1000);
    }

    @Autowired
    private ShopServiceImpl shopService;
    @Test
    void refreshShopToken() {
        shopService.refreshShopToken();
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
        List<String> objects = new ArrayList<>();
        objects.add("2405246N2RX9KH");
        JSONObject object = ShopeeUtil.getEscrowDetail("4762415a576374696571644a6e455750", 874244879, objects);
        System.out.println(object);
    }

    @Test
    void getReturnListByHttpTest() {
        JSONObject object = ShopeeUtil.getReturnListByHttp("475962786d70755862446676417a6e55", 874244879);
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
    void getCategoryType() {

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

    @Test
    void getPayoutDetailTest() throws InterruptedException {
//        JSONArray object = ShopeeUtil.getPayoutInfo("4762415a576374696571644a6e455750", 874244879, 1717171200, 1717948800, 0, new JSONArray());
//        JSONArray array = new JSONArray();
//        array.add("8184308878393187335");
//        JSONArray object = ShopeeUtil.getBillingTransactionInfo("4762415a576374696571644a6e455750", 874244879, array, "", new JSONArray());

//        payoutDetailService.refreshPayoutInfoByTime("2024-06-10", "2024-06-25");

//        System.out.println(object);
    }

    @Autowired
    private ProductExtraInfoServiceImpl productExtraInfoService;
    @Test
    void refreshProductExtraInfoTest() {
        productExtraInfoService.saveOrUpdateProductExtraInfo();
    }

    @Autowired
    private EscrowInfoServiceImpl escrowInfoService;
    @Test
    void refreshEscrowTest() {
//        escrowInfoService.refreshEscrowByTime("2024-06-18", "2024-06-21");
        escrowInfoService.refreshOrderNoOnEscrow();
    }

    @Test
    void refreshUnPaidEscrowTest() {
        escrowInfoService.refreshEscrowByStatus("UNPAID");
    }

    @Test
    void refreshUnCompleteOrder() {
        orderService.refreshOrderByStatus("COMPLETE");
    }

    @Test
    void delRedisKey() {
        delKey(ESCROW);
    }

    @Test
    void getRedisKey() {
        System.out.println(redisService.keys(ESCROW + "*").size());
    }

    void delKey(String preKey) {
        Set<String> keys = redisService.keys(preKey + "*");
        for (String key : keys) {
            redisService.del(key);
        }
    }

    @Autowired
    private PayoutInfoServiceImpl payoutDetailService;

    @Test
    void testRefreshPayoutDetail() {
//        payoutDetailService.refreshPayoutInfoByTime("2024-05-01", "2024-05-31");
    }

    @Test
    void getTrackingInfoTest() {
        JSONObject object = ShopeeUtil.getTrackingInfo("44625a646a6f6778534f57704d5a7a44", 874244879, "240531SAGCUP3N");
        System.out.println(object);
    }

    @Test
    void getEscrowList() {
        String startTimeStr = "2024-06-01 00:00:00";
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
            List<String> object = ShopeeUtil.getEscrowList("4747557957777265626f52664462696f", 874244879, 1, new ArrayList<>(), start, end);
//            System.out.println(object.toString());
            System.out.println("多少件："+object.size());
            orderSnList.addAll(object);
        }
    }

    @Autowired
    private TrackingInfoServiceImpl trackingInfoService;

    @Test
    void testDeleteProduct() {
        productService.refreshDeletedProduct();
    }

}

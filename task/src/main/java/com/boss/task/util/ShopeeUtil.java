package com.boss.task.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.task.dto.MainAccountAuthDTO;
import com.boss.task.dto.ShopAuthDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/8
 */


@Component
@Slf4j
public class ShopeeUtil {

    /**
     * 获得授权链接签名
     * @param partner_id
     * @param path
     * @param timest
     * @param tmp_partner_key
     * @return
     */
    public static String getAuthSign(long partner_id, String path, long timest, String tmp_partner_key) {
        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
        byte[] partner_key;
        byte[] base_string;
        String sign = "";
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = String.format("%064x",new BigInteger(1,mac.doFinal(base_string)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * 获取 token 签名
     * @param partner_id
     * @param path
     * @param timest
     * @param tmp_partner_key
     * @return
     */
    public static BigInteger getTokenSign(long partner_id, String path, long timest, String tmp_partner_key) {
        String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
        byte[] partner_key;
        byte[] base_string;
        BigInteger sign = null;
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = new BigInteger(1,mac.doFinal(base_string));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    public static BigInteger getShopTokenSign(long partner_id, String path, long timest, String accessToken, long shopId, String tmp_partner_key) {
        String tmp_base_string = String.format("%s%s%s%s%s", partner_id, path, timest, accessToken, shopId);
        byte[] partner_key;
        byte[] base_string;
        BigInteger sign = null;
        try {
            base_string = tmp_base_string.getBytes("UTF-8");
            partner_key = tmp_partner_key.getBytes("UTF-8");
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
            mac.init(secret_key);
            sign = new BigInteger(1,mac.doFinal(base_string));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    // 生成授权链接
    public static String getAuthUrl(String type, String userId){
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/shop/auth_partner";
        String redirect_url = "shop".equals(type) ? ShopAuthDTO.getRedirectUrl() : MainAccountAuthDTO.getRedirectUrl();
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        String sign = getAuthSign(partner_id,path,timest,tmp_partner_key);
        return host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&redirect=%s", partner_id,timest, sign, redirect_url + "?userId=" + userId);
    }

    //shop request for access token for the first time
    // 获取店铺账号token
    public static JSONObject getShopAccessToken(String code,long shop_id) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/auth/token/get";
        BigInteger sign = getTokenSign(ShopAuthDTO.getPartnerId(), path,timest,ShopAuthDTO.getTempPartnerKey());
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", ShopAuthDTO.getPartnerId(),timest, String.format("%032x",sign));
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("code",code);
        paramMap.put("shop_id",shop_id);
        paramMap.put("partner_id",ShopAuthDTO.getPartnerId());
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        log.info(result);
        return JSONObject.parseObject(result);
    }

    /**
     * 刷新 token
     * @param refresh_token
     * @param id
     * @return
     * @throws IOException
     */
    public static JSONObject refreshToken(String refresh_token, long id, String type) {
        JSONObject result = refreshTokenByHttp(refresh_token, id, type);

        int retryCount = 0;
        while (true) {
            if ((result == null || result.getString("error").contains("error")) && retryCount < 5) {
                result = refreshTokenByHttp(refresh_token, id, type);
                retryCount++;
            } else {
                break;
            }
        }

        return result;
    }

    public static JSONObject refreshTokenByHttp(String refresh_token, long id, String type) {

        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/auth/access_token/get";
        BigInteger sign = getTokenSign(ShopAuthDTO.getPartnerId(), path,timest,ShopAuthDTO.getTempPartnerKey());
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", ShopAuthDTO.getPartnerId(),timest, String.format("%032x",sign));
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refresh_token",refresh_token);
        paramMap.put("shop".equals(type) ? "shop_id" : "merchant_id", id);
        paramMap.put("partner_id",ShopAuthDTO.getPartnerId());
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        return JSONObject.parseObject(result);
    }

    //main account request for the access token for the first time
    // 获取主账号token
    public static JSONObject getAccountAccessToken(String code, long main_account_id) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/auth/token/get";
        BigInteger sign = getTokenSign(ShopAuthDTO.getPartnerId(), path,timest,ShopAuthDTO.getTempPartnerKey());


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("code",code);
        paramMap.put("main_account_id",main_account_id);
        paramMap.put("partner_id",ShopAuthDTO.getPartnerId());
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", ShopAuthDTO.getPartnerId(),timest, String.format("%032x",sign));
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        log.info(result);
        return JSONObject.parseObject(result);
    }

    /**
     * 获取产品列表
     * @param accessToken
     * @param shopId
     * @param offset
     * @param itemIds
     * @return
     */
    public static List<String> getProducts(String accessToken, long shopId, int offset, List<String> itemIds, String status) {
        JSONObject result = getProductByHttp(accessToken, shopId, offset, status);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getProductByHttp(accessToken, shopId, offset, status);
            retryCount++;
        }

        if ((result == null || result.getString("error").contains("error"))) {
            return itemIds;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("item");
        if (tempArray == null) {
            return itemIds;
        }
        for (int i = 0; i < tempArray.size(); i++) {
            itemIds.add(tempArray.getJSONObject(i).getString("item_id"));
        }

        if (responseObject.getBoolean("has_next_page")) {
            getProducts(accessToken, shopId, responseObject.getInteger("next_offset"), itemIds, status);
        }

        return itemIds;
    }

    private static JSONObject getProductByHttp(String accessToken, long shopId, int offset, String status) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&page_size=100" +
                        "%s&offset=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, status, offset);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取产品的基本信息
     * @param accessToken
     * @param shopId
     * @param itemId
     * @return
     */
    public static JSONObject getProductInfo(String accessToken, long shopId, String itemId) {

        JSONObject result = getProductInfoByHttp(accessToken, shopId, itemId);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getProductInfoByHttp(accessToken, shopId, itemId);
            retryCount++;
        }

        return result;
    }

    private static JSONObject getProductInfoByHttp(String accessToken, long shopId, String itemId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_base_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id_list=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemId);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取产品的模型信息
     * @param accessToken
     * @param shopId
     * @param itemId
     * @return
     */
    public static JSONObject getModelList(String accessToken, long shopId, long itemId) {
        JSONObject result = getModelListByHttp(accessToken, shopId, itemId);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getModelListByHttp(accessToken, shopId, itemId);
            retryCount++;
        }

        return result;
    }

    private static JSONObject getModelListByHttp(String accessToken, long shopId, long itemId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_model_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemId);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }


    public static JSONObject getAttributes(String accessToken, long shopId, long category_id) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_attributes";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&category_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, category_id);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


        return JSONObject.parseObject(result);
    }


    public static JSONObject getCategory(String accessToken, long shopId, long category_id) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_category";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&category_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, category_id);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


        return JSONObject.parseObject(result);
    }

    /**
     * 获取订单列表
     * @param accessToken
     * @param shopId
     * @param offset
     * @param orderSns
     * @param startTime
     * @param endTime
     * @return
     */
    public static List<String> getOrderList(String accessToken, long shopId, int offset, List<String> orderSns, long startTime, long endTime) {
        JSONObject result = getOrderListByHttp(accessToken, shopId, offset, startTime, endTime);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getOrderListByHttp(accessToken, shopId, offset, startTime, endTime);
            retryCount++;
        }

        if ((result == null || result.getString("error").contains("error"))) {
            return orderSns;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("order_list");
        for (int i = 0; i < tempArray.size(); i++) {
            orderSns.add(tempArray.getJSONObject(i).getString("order_sn"));
        }

        if (responseObject.getBoolean("more")) {
            getOrderList(accessToken, shopId, responseObject.getInteger("next_cursor"), orderSns, startTime, endTime);
        }

        return orderSns;
    }

    private static JSONObject getOrderListByHttp(String accessToken, long shopId, int offset, long startTime, long endTime) {

        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/order/get_order_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&time_range_field=create_time&time_from=%s&time_to=%s&page_size=100&cursor=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, startTime, endTime, offset == 0 ? "" : offset);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取订单详情
     * @param accessToken
     * @param shopId
     * @param orderSnList
     * @return
     */
    public static JSONObject getOrderDetail(String accessToken, long shopId, String orderSnList) {
        JSONObject result = getOrderDetailByHttp(accessToken, shopId, orderSnList);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getOrderDetailByHttp(accessToken, shopId, orderSnList);
            retryCount++;
        }

        log.info(result.toJSONString());

        return result;
    }

    private static JSONObject getOrderDetailByHttp(String accessToken, long shopId, String orderSnList) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/order/get_order_detail";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&order_sn_list=%s&&request_order_status_pending=true&response_optional_fields=buyer_user_id,buyer_username,estimated_shipping_fee,recipient_address,actual_shipping_fee,goods_to_declare,note,note_update_time,item_list,pay_time,dropshipper, dropshipper_phone,split_up,buyer_cancel_reason,cancel_by,cancel_reason,actual_shipping_fee_confirmed,buyer_cpf_id,fulfillment_flag,pickup_done_time,package_list,shipping_carrier,payment_method,total_amount,buyer_username,invoice_data,no_plastic_packing,order_chargeable_weight_gram,edt",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, orderSnList);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    public static JSONObject getItemPromotion(String accessToken, long shopId, String itemIds) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_promotion";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id_list=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemIds);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }


        return JSONObject.parseObject(result);
    }

    public static List<String> getEscrowList(String accessToken, long shopId, int pageNo, List<String> orderSns, long startTime, long endTime) {
        JSONObject result = getEscrowListByHttp(accessToken, shopId, pageNo, startTime, endTime);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getEscrowListByHttp(accessToken, shopId, pageNo, startTime, endTime);
            retryCount++;
        }

        if ((result == null || result.getString("error").contains("error"))) {
            return orderSns;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("escrow_list");
        for (int i = 0; i < tempArray.size(); i++) {
            orderSns.add(tempArray.getJSONObject(i).getString("order_sn"));
        }

        if (responseObject.getBoolean("more")) {
            pageNo++;
            getEscrowList(accessToken, shopId, pageNo, orderSns, startTime, endTime);
        }

        return orderSns;
    }

    private static JSONObject getEscrowListByHttp(String accessToken, long shopId, int page_no, long startTime, long endTime) {

        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/payment/get_escrow_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("shop_id", shopId);
        bodyObject.put("release_time_from", startTime);
        bodyObject.put("release_time_to", endTime);
        bodyObject.put("page_size", 100);
        bodyObject.put("page_no", page_no);

        String result;
        try {
            result = HttpUtil.post(tmp_url, bodyObject.toJSONString());
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取订单支付详情
     * @param accessToken
     * @param shopId
     * @param orderSn
     * @return
     */
    public static JSONObject getEscrowDetail(String accessToken, long shopId, List<String> orderSn) {
        JSONObject result = getEscrowDetailByHttp(accessToken, shopId, orderSn);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getEscrowDetailByHttp(accessToken, shopId, orderSn);
            retryCount++;
        }

        return result;
    }

    public static JSONObject getEscrowDetailByHttp(String accessToken, long shopId, List<String> orderSn) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/payment/get_escrow_detail_batch";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);
        JSONObject bodyObject = new JSONObject();
        bodyObject.put("order_sn_list", orderSn);
        String result;
        try {
            result = HttpUtil.post(tmp_url, bodyObject.toJSONString());
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取订单物流号
     * @param accessToken
     * @param shopId
     * @param orderSn
     * @return
     */
    public static JSONObject getTrackingInfo(String accessToken, long shopId, String orderSn) {
        JSONObject result = getTrackingInfoByHttp(accessToken, shopId, orderSn);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || (result == null || result.getString("error").contains("error"))) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }


            try {
                result = getTrackingInfoByHttp(accessToken, shopId, orderSn);
            } catch (Exception e) {

            }
            retryCount++;
        }

        return result;
    }

    public static JSONObject getTrackingInfoByHttp(String accessToken, long shopId, String orderSn) {


        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/logistics/get_tracking_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&order_sn=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, orderSn);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }


    public static JSONObject createShippingDocument(String accessToken, long shopId, String orderSn) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/logistics/create_shipping_document";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, orderSn);

        JSONObject bodyObject = new JSONObject();
        JSONArray orderSnArray = new JSONArray();
        JSONObject orderSnObject = new JSONObject();
        orderSnObject.put("order_sn", orderSn);
        orderSnArray.add(orderSnObject);
        bodyObject.put("order_list", orderSnArray);

        String result;
        try {
            result = HttpUtil.post(tmp_url, bodyObject.toJSONString());
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 获取退货列表
     * @param accessToken
     * @param shopId
     * @return
     */
    public static JSONObject getReturnListByHttp(String accessToken, long shopId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/returns/get_return_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&page_no=0&page_size=100",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    public static JSONObject getShopInfoByHttp(String accessToken, long shopId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/merchant/get_merchant_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    public static JSONObject getCategoryByHttp(String accessToken, long shopId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_category";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&language=zh-hans",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    /**
     * 收入和调整记录
     * @param accessToken
     * @param shopId
     * @param startTime
     * @param endTime
     * @return
     */
    public static JSONArray getPayoutInfo(String accessToken, long shopId, long startTime, long endTime, int cursor, JSONArray resultArray) {
        JSONObject result = getPayoutInfoByHttp(accessToken, shopId, startTime, endTime, cursor);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getPayoutInfoByHttp(accessToken, shopId, startTime, endTime, cursor);
            retryCount++;
        }

        if (result == null || result.getString("error").contains("error") || !result.containsKey("response")) {
            return resultArray;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("payout_list");
        if (tempArray == null || tempArray.isEmpty()) {
            return resultArray;
        }

        for (int i = 0; i < tempArray.size(); i++) {
            JSONObject payoutObject = tempArray.getJSONObject(i);
            JSONObject resultObject = new JSONObject();
            resultObject.put("encrypted_payout_id", payoutObject.getString("encrypted_payout_id"));
            resultObject.put("exchange_rate", payoutObject.getString("exchange_rate"));
            resultObject.put("payout_time", payoutObject.getLong("payout_time"));

            resultArray.add(resultObject);
        }

        if (responseObject.getBoolean("more")) {
            getPayoutInfo(accessToken, shopId, startTime, endTime, responseObject.getInteger("next_cursor"), resultArray);
        }

        return resultArray;
    }


    public static JSONObject getPayoutInfoByHttp(String accessToken, long shopId, long startTime, long endTime, int cursor) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/payment/get_payout_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("shop_id", shopId);
        bodyObject.put("payout_time_from", startTime);
        bodyObject.put("payout_time_to", endTime);
        bodyObject.put("page_size", 100);
        bodyObject.put("cursor", cursor == 0 ? "" : cursor);

        String result;
        try {
            result = HttpUtil.post(tmp_url, bodyObject.toJSONString());
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }


        return JSONObject.parseObject(result);
    }

    public static JSONArray getBillingTransactionInfo(String accessToken, long shopId, JSONArray ids, String cursor, JSONArray resultArray) {

        JSONObject result = getBillingTransactionInfoByHttp(accessToken, shopId, ids, cursor);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || (result == null || result.getString("error").contains("error"))) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            try {
                result = getBillingTransactionInfoByHttp(accessToken, shopId, ids, cursor);
            } catch (Exception e) {

            }
            retryCount++;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("transactions");
        for (int i = 0; i < tempArray.size(); i++) {
            resultArray.add(tempArray.getJSONObject(i));
        }

        if (responseObject.getBoolean("more")) {
            getBillingTransactionInfo(accessToken, shopId, ids, responseObject.getString("next_cursor"), resultArray);
        }

        return resultArray;
    }

    public static JSONObject getBillingTransactionInfoByHttp(String accessToken, long shopId, JSONArray ids, String cursor) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/payment/get_billing_transaction_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        JSONObject bodyObject = new JSONObject();
        bodyObject.put("billing_transaction_info_type", 2);
        bodyObject.put("encrypted_payout_ids", ids);
        bodyObject.put("page_size", 100);
        bodyObject.put("cursor", cursor);

        String result;
        try {
            result = HttpUtil.post(tmp_url, bodyObject.toJSONString());
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }

    /**
     * 取产品额外信息
     * @param accessToken
     * @param shopId
     * @param itemId
     * @return
     */
    public static JSONObject getProductExtraInfo(String accessToken, long shopId, String itemId) {

        JSONObject result = getProductExtraInfoByHttp(accessToken, shopId, itemId);

        int retryCount = 0;
        final int maxRetries = 5;
        final int baseDelayMs = 100; // 初始延迟时间，例如100毫秒

        while ((result == null || result.getString("error").contains("error")) && retryCount < maxRetries) {
            try {
                Thread.sleep(baseDelayMs * (int)Math.pow(2, retryCount)); // 指数级增长的延迟
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 保持中断状态
                throw new RuntimeException("Thread interrupted", e);
            }

            result = getProductExtraInfoByHttp(accessToken, shopId, itemId);
            retryCount++;
        }

        return result;
    }

    private static JSONObject getProductExtraInfoByHttp(String accessToken, long shopId, String itemId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_extra_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id_list=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemId);

        String result;
        try {
            result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);
        } catch (Exception e) {
            JSONObject temp = new JSONObject();
            temp.put("error", "error");
            return temp;
        }

        return JSONObject.parseObject(result);
    }
}

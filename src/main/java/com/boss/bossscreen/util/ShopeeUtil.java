package com.boss.bossscreen.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.boss.bossscreen.dto.MainAccountAuthDTO;
import com.boss.bossscreen.dto.ShopAuthDTO;
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
            if (result.getString("error").contains("error") && retryCount < 5) {
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

    public static List<String> getProducts(String accessToken, long shopId, int offset, List<String> itemIds) {
        JSONObject result = getProductByHttp(accessToken, shopId, offset);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getProductByHttp(accessToken, shopId, offset);
                retryCount++;
            } else {
                break;
            }
        }

        if (result.getString("error").contains("error")) {
            return itemIds;
        }

        JSONObject responseObject = result.getJSONObject("response");
        JSONArray tempArray = responseObject.getJSONArray("item");
        for (int i = 0; i < tempArray.size(); i++) {
            itemIds.add(tempArray.getJSONObject(i).getString("item_id"));
        }

        if (responseObject.getBoolean("has_next_page")) {
            getProducts(accessToken, shopId, responseObject.getInteger("next_offset"), itemIds);
        }

        return itemIds;
    }

    private static JSONObject getProductByHttp(String accessToken, long shopId, int offset) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&page_size=100" +
                        "&item_status=NORMAL&item_status=BANNED&item_status=UNLIST&item_status=REVIEWING&item_status=SHOPEE_DELETE&offset=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, offset);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    public static JSONObject getProductInfo(String accessToken, long shopId, String itemId) {

        JSONObject result = getProductInfoByHttp(accessToken, shopId, itemId);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getProductInfoByHttp(accessToken, shopId, itemId);
                retryCount++;
            } else {
                break;
            }
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

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }


    public static JSONObject getModelList(String accessToken, long shopId, long itemId) {
        JSONObject result = getModelListByHttp(accessToken, shopId, itemId);

        int retryCount = 0;
        while (true) {
            if (result == null || (result.getString("error").contains("error") && retryCount < 5)) {
                result = getModelListByHttp(accessToken, shopId, itemId);
                retryCount++;
            } else {
                break;
            }
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

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
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

    public static List<String> getOrderList(String accessToken, long shopId, int offset, List<String> orderSns, long startTime, long endTime) {
        JSONObject result = getOrderListByHttp(accessToken, shopId, offset, orderSns, startTime, endTime);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getOrderListByHttp(accessToken, shopId, offset, orderSns, startTime, endTime);
                retryCount++;
            } else {
                break;
            }
        }

        if (result.getString("error").contains("error")) {
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

    private static JSONObject getOrderListByHttp(String accessToken, long shopId, int offset, List<String> orderSns, long startTime, long endTime) {

        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/order/get_order_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&time_range_field=create_time&time_from=%s&time_to=%s&page_size=100&cursor=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, startTime, endTime, offset);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    public static JSONObject getOrderDetail(String accessToken, long shopId, String orderSnList) {
        JSONObject result = getOrderDetailByHttp(accessToken, shopId, orderSnList);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getOrderDetailByHttp(accessToken, shopId, orderSnList);
                retryCount++;
            } else {
                break;
            }
        }

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

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
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

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


        return JSONObject.parseObject(result);
    }


    public static JSONObject getEscrowDetail(String accessToken, long shopId, String orderSn) {
        JSONObject result = getEscrowDetailByHttp(accessToken, shopId, orderSn);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getEscrowDetailByHttp(accessToken, shopId, orderSn);
                retryCount++;
            } else {
                break;
            }
        }

        return result;
    }

    public static JSONObject getEscrowDetailByHttp(String accessToken, long shopId, String orderSn) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/payment/get_escrow_detail";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&order_sn=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, orderSn);

        return JSONObject.parseObject(HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8));
    }

    public static JSONObject getTrackingNumber(String accessToken, long shopId, String orderSn) {
        JSONObject result = getTrackingNumberByHttp(accessToken, shopId, orderSn);

        int retryCount = 0;
        while (true) {
            if (result.getString("error").contains("error") && retryCount < 5) {
                result = getTrackingNumberByHttp(accessToken, shopId, orderSn);
                retryCount++;
            } else {
                break;
            }
        }

        return result;
    }

    public static JSONObject getTrackingNumberByHttp(String accessToken, long shopId, String orderSn) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/logistics/get_tracking_number";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
        // "https://partner.shopeemobile.com/api/v2/order/get_order_list?page_size=20&response_optional_fields=order_status&timestamp=timestamp&shop_id=shop_id&order_status=READY_TO_SHIP&partner_id=partner_id&access_token=access_token&cursor=""&time_range_field=create_time&time_from=1607235072&time_to=1608271872&sign=sign"
        String tmp_url = host + path + String.format("?&partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&order_sn=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, orderSn);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


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
        System.out.println(bodyObject);

        String result = HttpUtil.post(tmp_url, bodyObject.toJSONString());

        return JSONObject.parseObject(result);
    }

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
}

package com.boss.bossscreen.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.boss.bossscreen.dto.MainAccountAuthDTO;
import com.boss.bossscreen.dto.ShopAuthDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
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
    public static String getAuthUrl(String type){
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/shop/auth_partner";
        String redirect_url = "shop".equals(type) ? ShopAuthDTO.getRedirectUrl() : MainAccountAuthDTO.getRedirectUrl();
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        String sign = getAuthSign(partner_id,path,timest,tmp_partner_key);
        return host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&redirect=%s", partner_id,timest, sign, redirect_url);
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
        log.info(result);
        return JSONObject.parseObject(result);
    }

//    public static JSONObject refresh(String refresh_token,long partner_id,String tmp_partner_key,long merchant_id) throws ParseException,IOException{
//        String[] res = new String[2];
//        long timest = System.currentTimeMillis() / 1000L;
//        String host = "https://partner.shopeemobile.com";
//        String path = "/api/v2/auth/access_token/get";
//        BigInteger sign = getTokenSign(ShopAuthDTO.getPartnerId(), path,timest,ShopAuthDTO.getTempPartnerKey());
//        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id,timest, String.format("%032x",sign));
//        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("refresh_token",refresh_token);
//        paramMap.put("merchant_id",merchant_id);
//        paramMap.put("partner_id",partner_id);
//        String result = HttpRequest.post(tmp_url)
//                .header(Header.ACCEPT, "application/json")
//                .header(Header.CONTENT_TYPE, "application/json")
//                .body(JSON.toJSONString(paramMap))
//                .execute().body();
//        return JSONObject.parseObject(result);
//    }

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

    public static JSONObject getProducts(String accessToken, long shopId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&page_size=100&item_status=NORMAL&offset=0",
                                                    partner_id, timest, String.format("%032x",sign), accessToken, shopId);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);

        log.info(result);
        return JSONObject.parseObject(result);
    }

    public static JSONObject getProductInfo(String accessToken, int shopId, String itemId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_item_base_info";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id_list=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemId);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


        return JSONObject.parseObject(result);
    }


    public static JSONObject getModelList(String accessToken, int shopId, String itemId) {
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthDTO.getHost();
        String path = "/api/v2/product/get_model_list";
        long partner_id = ShopAuthDTO.getPartnerId();
        String tmp_partner_key = ShopAuthDTO.getTempPartnerKey();
        BigInteger sign = getShopTokenSign(partner_id, path,timest, accessToken, shopId, tmp_partner_key);
//        String tmp_url = host + path + "?partner_id=" + partner_id + "&timestamp=" + timest + "&access_token=" + accessToken + "&shop_id=" + shopId + "&sign=" + String.format("%032x",sign) + "&page_siz=100&item_status=NORMAL&offset=0&update_time_to="+ timest;
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&access_token=%s&shop_id=%s&item_id=%s",
                partner_id, timest, String.format("%032x",sign), accessToken, shopId, itemId);

        String result = HttpUtil.get(tmp_url, CharsetUtil.CHARSET_UTF_8);


        return JSONObject.parseObject(result);
    }

}

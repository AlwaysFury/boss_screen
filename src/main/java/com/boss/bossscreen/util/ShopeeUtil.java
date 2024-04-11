package com.boss.bossscreen.util;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.boss.bossscreen.vo.ShopAuthVO;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/8
 */


@Component
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

    // 生成授权链接
    public static String getAuthUrl(){
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthVO.getHost();
        String path = "/api/v2/shop/auth_partner";
        String redirect_url = ShopAuthVO.getRedirectUrl();
        long partner_id = ShopAuthVO.getPartnerId();
        String tmp_partner_key = ShopAuthVO.getTempPartnerKey();
        String sign = getAuthSign(partner_id,path,timest,tmp_partner_key);
        return host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s&redirect=%s", partner_id,timest, sign, redirect_url);
    }

    //shop request for access token for the first time
    // 获取店铺账号token
    public static JSONObject getShopAccessToken(String code,long partner_id,String tmp_partner_key,long shop_id) throws ParseException,IOException{
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthVO.getHost();
        String path = "/api/v2/auth/token/get";
        BigInteger sign = getTokenSign(partner_id, path,timest,tmp_partner_key);
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id,timest, String.format("%032x",sign));
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("code",code);
        paramMap.put("shop_id",shop_id);
        paramMap.put("partner_id",partner_id);
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        return JSONObject.parseObject(result);
    }

    /**
     * 刷新 token
     * @param refresh_token
     * @param partner_id
     * @param tmp_partner_key
     * @param shop_id
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public static JSONObject refreshToken(String refresh_token,long partner_id,String tmp_partner_key,long shop_id) throws ParseException,IOException{

        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthVO.getHost();
        String path = "/api/v2/auth/access_token/get";
        BigInteger sign = getTokenSign(partner_id, path,timest,tmp_partner_key);
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id,timest, String.format("%032x",sign));
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("refresh_token",refresh_token);
        paramMap.put("shop_id",shop_id);
        paramMap.put("partner_id",partner_id);
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        return JSONObject.parseObject(result);
    }

    //main account request for the access token for the first time
    // 获取主账号token
    public static JSONObject getAccountAccessToken(String code,long partner_id,String tmp_partner_key,long main_account_id) throws ParseException,IOException{
        long timest = System.currentTimeMillis() / 1000L;
        String host = ShopAuthVO.getHost();
        String path = "/api/v2/auth/token/get";
        BigInteger sign = getTokenSign(partner_id, path,timest,tmp_partner_key);


        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("code",code);
        paramMap.put("main_account_id",main_account_id);
        paramMap.put("partner_id",partner_id);
        String tmp_url = host + path + String.format("?partner_id=%s&timestamp=%s&sign=%s", partner_id,timest, String.format("%032x",sign));
        String result = HttpRequest.post(tmp_url)
                .header(Header.ACCEPT, "application/json")
                .header(Header.CONTENT_TYPE, "application/json")
                .body(JSON.toJSONString(paramMap))
                .execute().body();
        return JSONObject.parseObject(result);
    }

}

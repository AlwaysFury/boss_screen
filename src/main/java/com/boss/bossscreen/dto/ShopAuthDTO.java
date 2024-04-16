package com.boss.bossscreen.dto;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/9
 */
@Component
@Data
public class ShopAuthDTO {

    @Value("${shopeeApi.host}")
    private String host;

    @Value("${shopeeApi.partner_id}")
    private long partner_id;

    @Value("${shopeeApi.partner_key}")
    private String tmp_partner_key;

    @Value("${shopeeApi.shop_redirect_url}")
    private String redirect_url;


    private static String HOST;

    private static long PARTNER_ID;

    private static String TEP_PARTNER_KEY;

    private static String REDIRECT_URL;



    @PostConstruct
    public void setParams() {
        HOST = this.host;
        PARTNER_ID = this.partner_id;
        TEP_PARTNER_KEY = this.tmp_partner_key;
        REDIRECT_URL = this.redirect_url;
    }

    public static String getHost() {
        return HOST;
    }

    public static Long getPartnerId() {
        return PARTNER_ID;
    }

    public static String getTempPartnerKey() {
        return TEP_PARTNER_KEY;
    }

    public static String getRedirectUrl() {
        return REDIRECT_URL;
    }


}

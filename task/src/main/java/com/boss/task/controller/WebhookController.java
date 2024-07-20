package com.boss.task.controller;


import com.boss.task.service.impl.WebhookServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/6/19
 */
@RestController
@RequestMapping("/webhook")
@Slf4j
public class WebhookController {

    @Autowired
    private WebhookServiceImpl webhookService;

    @PostMapping("/getPush")
    public Boolean receiveWebhookData(@RequestBody String body) throws NoSuchAlgorithmException, UnsupportedEncodingException, java.security.InvalidKeyException {
        // requestBody就是接收到的JSON字符串或其他格式的数据
        log.info("Received data: {}",  body);
        try {
            webhookService.getPush(body);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}

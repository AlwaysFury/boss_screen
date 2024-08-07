package com.boss.shopee_receiver.controller;

import com.boss.shopee_receiver.service.impl.ReceiverServiceImpl;
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
 * @Date 2024/8/3
 */

@RestController
@RequestMapping("/receive")
@Slf4j
public class ReceiverController {

    @Autowired
    private ReceiverServiceImpl receiverService;

    @PostMapping("/getPush")
    public Boolean receiveWebhookData(@RequestBody String body) throws NoSuchAlgorithmException, UnsupportedEncodingException, java.security.InvalidKeyException {
        // requestBody就是接收到的JSON字符串或其他格式的数据
        log.info("Received data: {}",  body);
        try {
            receiverService.getPush(body);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}

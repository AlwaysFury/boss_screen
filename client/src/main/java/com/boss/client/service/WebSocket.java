package com.boss.client.service;

import com.alibaba.fastjson.JSON;
import com.boss.client.vo.Result;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/4/25
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {

    /**线程安全Map*/
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") String userId) {
        try {
            sessionPool.put(userId, session);
            log.info("【系统 WebSocket】有新的连接，总数为:" + sessionPool.size());
            this.pushMessage(userId, JSON.toJSONString(Result.ok()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        try {
            sessionPool.remove(userId);
            log.info(userId + " 与【系统 WebSocket】连接断开，总数为:" + sessionPool.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ws推送消息
     *
     * @param userId
     * @param message
     */
    public static void pushMessage(String userId, String message) {
        for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
            //userId key值= {用户id + "_"+ 登录token的md5串}
            if (item.getKey().contains(userId)) {
                Session session = item.getValue();
                try {
                    //update-begin-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                    synchronized (session){
                        log.info("【系统 WebSocket】推送单人消息:" + message);
                        session.getBasicRemote().sendText(message);
                    }
                    //update-end-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
    }

    /**
     * ws推送消息
     *
     * @param userId
     * @param message
     */
    public static void pushResult(String userId, Result<?> result) {
        for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
            //userId key值= {用户id + "_"+ 登录token的md5串}
            if (item.getKey().contains(userId)) {
                Session session = item.getValue();
                try {
                    //update-begin-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                    synchronized (session){
                        log.info("【系统 WebSocket】推送单人消息:" + result);
                        session.getBasicRemote().sendObject(result.getFlag());
                    }
                    //update-end-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                } catch (Exception e) {
                    log.error(e.getMessage(),e);
                }
            }
        }
    }


    /**
     * ws遍历群发消息
     */
    public static void pushMessage(String message) {
        try {
            for (Map.Entry<String, Session> item : sessionPool.entrySet()) {
                try {
                    item.getValue().getAsyncRemote().sendText(message);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("【系统 WebSocket】群发消息{}", message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * ws接受客户端消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam(value = "userId") String userId) {

        log.info("【系统 WebSocket】收到客户端 {} 消息:{}" ,userId, message);

        this.pushMessage(userId, "已经收到消息:" + message);
    }

    /**
     * 配置错误信息处理
     *
     * @param session
     * @param t
     */
    @OnError
    public void onError(Session session, Throwable t) {
        log.warn("【系统 WebSocket】消息出现错误");
    }
}
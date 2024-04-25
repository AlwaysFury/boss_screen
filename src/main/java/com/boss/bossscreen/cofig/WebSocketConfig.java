package com.boss.bossscreen.cofig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * websocket配置类
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 注意事项：
     * WebSocket启动的时候优先于spring容器，从而导致在WebSocketServer中调用业务Service会报空指针异常
     * 所以需要在WebSocketServer中将所需要用到的service给静态初始化一下
     * 此处提前注入
     */
//    @Autowired
//    private void setXxx(XxxService xxxService) {
//        WebSocketServer.xxxService = xxxService;
//    }

}
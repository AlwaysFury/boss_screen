package com.boss.canal_client.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetSocketAddress;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */
@Configuration
@EnableScheduling
@EnableAsync
public class CanalConfig {

    @Value("${canal.server.ip}")
    private String canalServerIp;
    @Value("${canal.server.port}")
    private int canalServerPort;
    @Value("${canal.destination}")
    private String destination;

    @Bean("canalConnector")
    public CanalConnector newSingleConnector() {
        return CanalConnectors.newSingleConnector(new InetSocketAddress(canalServerIp,
                canalServerPort), destination, "", "");
    }
}

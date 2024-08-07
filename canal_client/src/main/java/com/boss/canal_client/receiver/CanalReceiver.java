package com.boss.canal_client.receiver;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author 罗宇航
 * @Date 2024/8/3
 */


@Service
@Slf4j
public class CanalReceiver {

    @Autowired
    @Qualifier("canalConnector")
    private CanalConnector canalConnector;

    @Value("${canal.subscribe}")
    private String subscribe;

    @PostConstruct
    public void connect() {
        canalConnector.connect();
        canalConnector.subscribe(subscribe);
        canalConnector.rollback();
    }

    @PreDestroy
    public void disConnect() {
        canalConnector.disconnect();
    }

    @Async
    @Scheduled(initialDelayString = "${canal.initialDelay:3000}", fixedDelayString = "${canal.fixedDelay:3000}")
    public void processData() {
        try {
            if (!canalConnector.checkValid()) {
                log.warn("=====>与Canal服务器的连接失效！！！重连，下个周期再检查数据变更");
                this.connect();
            } else {
                //获取batchSize条数据
                Message message = canalConnector.getWithoutAck(100);
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    log.info("=====>本批次[{}]没有数据需要同步", batchId);
                } else {
                    log.info("=====>本批次[{}]数据同步共有[{}]个更新需要处理", batchId, size);
                    for (CanalEntry.Entry entry : message.getEntries()) {
                        log.info(entry.getEntryType().toString());
                        if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                                || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                            log.info("=====>当前语句为事务开始或者事务结束, 不做处理");
                            continue;
                        }
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                        //获取表名
                        String tableName = entry.getHeader().getTableName();
                        CanalEntry.EventType eventType = rowChange.getEventType();
                        log.info("=====>数据变更详情：来自binglog[{}.{}], 数据源{}.{}, 变更类型{}",
                                entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                                entry.getHeader().getSchemaName(), tableName, eventType);

                        //处理数据
                        handleData(entry);
                    }

                    // 提交确认
                    canalConnector.ack(batchId);
                    log.info("=====>本批次[{}]Canal同步数据完成", batchId);
                }
            }
        } catch (Exception e) {
            log.error("=====>Canal同步数据失效，请检查：", e);
        }
    }

    private void handleData(CanalEntry.Entry entry) {
        //自己的逻辑，比如保存到数据库，推送的mq，保存到redis
    }
}

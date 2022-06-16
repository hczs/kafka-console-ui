package com.xuxd.kafka.console.interceptor;

import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.utils.ConvertUtil;
import com.xuxd.kafka.console.utils.ExecutorServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author houcheng
 * @version V1.0
 * @date 2022/6/16 22:14:07
 */
@Slf4j
@Component
public class ConsumeHandler extends TextWebSocketHandler {

    private static List<WebSocketSession> connnectedSessions = new CopyOnWriteArrayList<>();

    private static ConcurrentHashMap<String, Boolean> sessionConsumeStatus = new ConcurrentHashMap<>();

    @Autowired
    private ClusterInfoMapper clusterInfoMapper;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到消息：{}", message);
        connnectedSessions.add(session);
        String messageContent = message.getPayload();
        // 先检验是否是关闭消费的消息
        if ("close".equals(messageContent)) {
            log.info("收到关闭消费消息，session: {}", session);
            sessionConsumeStatus.put(session.getId(), false);
            return;
        }
        String[] clusterIdAndTopic = messageContent.split(",");
        if (clusterIdAndTopic.length != 2) {
            log.error("websocket 接收消费消息格式错误，消息内容：{}", messageContent);
            return;
        }
        String clusterId = clusterIdAndTopic[0];
        String topicName = clusterIdAndTopic[1];
        log.info("websocket: ConsumeHandler 接收到消息，要消费的 topicName：{}", topicName);
        sessionConsumeStatus.put(session.getId(), true);
        // 提交消费任务
        ExecutorServiceUtil.threadPoolExecutor.submit(() -> {
            ClusterInfoDO infoDO = clusterInfoMapper.selectById(Long.valueOf(clusterId));
            Properties props = new Properties();
            // 必须属性 集群地址 key value 反序列化
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, infoDO.getAddress());
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, ContextConfig.DEFAULT_REQUEST_TIMEOUT_MS);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            // 设置其他属性
            if (StringUtils.isNotBlank(infoDO.getProperties())) {
                props.putAll(ConvertUtil.toProperties(infoDO.getProperties()));
            }
            props.put(ConsumerConfig.GROUP_ID_CONFIG, session.getId());
            try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
                List<String> topics = new ArrayList<>();
                topics.add(topicName);
                consumer.subscribe(topics);
                log.info("开始消费 topic: {}", topicName);
                while (sessionConsumeStatus.get(session.getId())) {
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
                    for (ConsumerRecord<String, String> record : consumerRecords) {
                        session.sendMessage(new TextMessage(record.toString()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("消费异常", e);
            }
            log.info("已停止消费topic: {}", topicName);
        });
        log.info("本次 websocket 请求处理完成");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("websocket: 客户端建立连接，session：{}", session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("websocket: 连接已关闭，session：{}", session);
        connnectedSessions.remove(session);
    }
}

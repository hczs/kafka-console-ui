package com.xuxd.kafka.console.config;

import com.xuxd.kafka.console.interceptor.ConsumeHandler;
import com.xuxd.kafka.console.interceptor.WebSocketHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author houcheng
 * @version V1.0
 * @date 2022/6/16 22:17:34
 */
@Component
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer  {

    @Autowired
    private ConsumeHandler consumeHandler;

    @Autowired
    private WebSocketHandshakeInterceptor interceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(consumeHandler, "/consume")
                .addInterceptors(interceptor)
                .setAllowedOriginPatterns("*");
    }
}

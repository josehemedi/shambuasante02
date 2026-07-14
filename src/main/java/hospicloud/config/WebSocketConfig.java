package hospicloud.config;

import hospicloud.security.ConsultationChannelInterceptor;
import hospicloud.security.StompSecurityChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ConsultationChannelInterceptor channelInterceptor;
    private final StompSecurityChannelInterceptor stompSecurityChannelInterceptor;

    public WebSocketConfig(ConsultationChannelInterceptor channelInterceptor,
                           StompSecurityChannelInterceptor stompSecurityChannelInterceptor) {
        this.channelInterceptor = channelInterceptor;
        this.stompSecurityChannelInterceptor = stompSecurityChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompSecurityChannelInterceptor, channelInterceptor);
    }
}

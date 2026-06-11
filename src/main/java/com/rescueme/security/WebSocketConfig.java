package com.rescueme.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

//clasa responsabila cu activarea si configurarea WebSocket cu STOMP in Spring Boot
@Configuration //spune ca e o clasa de configurare Spring (componenta Spring care contine reguli de configurare)
@EnableWebSocketMessageBroker //activeaza WebSocket si suportul pt protocolul STOMP peste WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//interfata asta oferita de Spring permite sa definesti endpointul de WebSocket, sa setezi
//prefixul pt mesaje, sa configurezi message brokerul
//aici brokerul de mesaje este unul incorporat in Spring Boot, un "simple broker" oferit de framework, nu unul extern

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/topic/chat", "/topic/read"); //activeaza brokerul
        //simplu de mesaje care va livra mesajele inapoi la clienti
        config.setApplicationDestinationPrefixes("/app"); //toate mesajele de la client trebuie sa aibe prefixul
        //app ca serverul sa stie ca sunt pt el, ca sa nu le incurce cu celelalte canale care sunt pt mesaje
        //trimise inapoi catre clienti
        config.setUserDestinationPrefix("/user"); //daca se vrea sa se trimita un mesaj doar pentru un anumit utilizator
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")//defineste endpoint ul prin care frontend ul se conecteaza la WebSocket
                .setAllowedOriginPatterns("*")
                .withSockJS()//activeaza suportul pt biblioteca de fallback SockJs
                .setSessionCookieNeeded(false); //fara sesiune HTTP, foloseșc JWT
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(128 * 1024);
        registration.setSendTimeLimit(20 * 1000);
        registration.setSendBufferSizeLimit(512 * 1024);
    }
}
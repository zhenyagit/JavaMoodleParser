package org.imjs_man.moodleParser.interceptor;

import java.util.ArrayList;
import java.util.Map;

import org.imjs_man.moodleParser.entity.supporting.WebSocketUser;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class WebSocketUserInterceptor implements ChannelInterceptor  {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object raw = message.getHeaders().get(SimpMessageHeaderAccessor.NATIVE_HEADERS);

            if (raw instanceof Map) {
                Object name = ((Map<?, ?>) raw).get("username");

                if (name instanceof ArrayList) {
                    ArrayList<?> temp = (ArrayList<?>) name;
                    String nameString = (String) temp.get(0);
                    accessor.setUser(new WebSocketUser(nameString));
                }
            }
        }
        return message;
    }
}
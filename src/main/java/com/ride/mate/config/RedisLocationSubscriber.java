package com.ride.mate.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
/**
 * Redis Pub/Sub Subscriber
 * Listens to the "driver-location-updates" Redis channel and
 * forwards location updates to WebSocket subscribers via STOMP.
 *
 * This enables horizontal scaling: when multiple server instances run,
 * a location published on instance A is fanned out via Redis pub/sub
 * to all instances, and each instance pushes it to its local WebSocket clients.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Component
public class RedisLocationSubscriber implements MessageListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    public RedisLocationSubscriber(SimpMessagingTemplate messagingTemplate,
                                    ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);
            Object rideIdObj = payload.get("rideId");
            if (rideIdObj == null) {
                log.warn("[Redis Sub] Received location update without rideId");
                return;
            }
            String rideId = rideIdObj.toString();
            String destination = "/topic/ride/" + rideId + "/location";
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("[Redis Sub] Forwarded location to {} ", destination);
        } catch (Exception e) {
            log.error("[Redis Sub] Error processing location message: {}", e.getMessage());
        }
    }
}

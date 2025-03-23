package com.ride.mate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 * Sets up RedisTemplate for location caching and pub/sub
 * for broadcasting driver location updates across server instances.
 *
 * Redis is used ONLY for real-time location tracking, NOT for replacing MySQL.
 *
 * Keys:
 *   ride:{rideId}:driver-location  (Hash) - cached latest driver position
 *
 * Pub/Sub channel:
 *   driver-location-updates - broadcast location events across instances
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 01-04-2026    N/A          N/A          Tishan          Initial Development
 */
@Configuration
public class RedisConfig {

    public static final String LOCATION_CHANNEL = "driver-location-updates";

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public ChannelTopic driverLocationTopic() {
        return new ChannelTopic(LOCATION_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(
            RedisLocationSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic driverLocationTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, driverLocationTopic);
        return container;
    }
}

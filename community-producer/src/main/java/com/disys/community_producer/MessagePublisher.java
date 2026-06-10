package com.disys.community_producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class MessagePublisher {

    public static final String QUEUE = "energy.community";

    private final Channel channel;
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());


    public MessagePublisher() {
       channel = new oke

    }
}

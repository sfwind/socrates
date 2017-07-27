package com.iquanwai.mq;

import lombok.Data;

/**
 * Created by justin on 17/7/26.
 */
@Data
public class RabbitMQDto {
    private String msgId;
    private Object message;
    private String queue;
    private String topic;
}

package com.iquanwai.mq;

import java.net.ConnectException;

/**
 * Created by justin on 17/1/19.
 */
public interface RabbitMQPublisher {

    public <T> void publish(T message) throws ConnectException;
}

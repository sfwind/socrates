package com.iquanwai.mq;

import com.alibaba.fastjson.JSON;
import com.iquanwai.util.CommonUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ConnectException;
import java.util.function.Consumer;

/**
 * Created by justin on 17/1/19.
 */
public class RabbitMQPublisher {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String topic;
    private Connection connection;
    private Channel channel;
    private String ipAddress;
    private int port = 5672;
    @Setter
    private Consumer<MQSendLog> sendCallback;

    public void init(String topic, String ipAddress, Integer port){
        Assert.notNull(topic, "消息主题不能为空");
        Assert.notNull(ipAddress, "rabbit ip不能为空");
        destroy();
        this.topic = topic;
        this.ipAddress = ipAddress;
        if (port != null) {
            this.port = port;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(ipAddress);
        factory.setPort(this.port);

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            //交换机声明,广播形式
            channel.exchangeDeclare(topic, "fanout");
        }catch (IOException e) {
            //ignore
        }
    }

    @PreDestroy
    public void destroy(){
        try {
            if(channel!=null) {
                channel.close();
            }
            if(connection!=null) {
                connection.close();
            }
        }catch (IOException e) {
            //ignore
        }
    }

//    public void publish(String message) throws ConnectException {
//        //重连尝试
//        if(connection==null || channel==null){
//            init(topic, ipAddress, port);
//        }
//        if(channel==null){
//            throw new ConnectException();
//        }
//
//        try {
//            String msgId = CommonUtils.randomString(32);
//
//            RabbitMQDto dto = new RabbitMQDto();
//            dto.setMsgId(msgId);
//            dto.setMessage(message);
//            String json = JSON.toJSONString(dto);
//            channel.basicPublish(topic, "", null, json.getBytes());
//            if (this.sendCallback != null) {
//                MessageQueue messageQueue = new MessageQueue();
//                messageQueue.setMessage(message);
//                messageQueue.setTopic(topic);
//                messageQueue.setMsgId(msgId);
//                this.sendCallback.accept(messageQueue);
//            }
//        }catch (IOException e) {
//            //ignore
//        }
//    }

    public <T> void publish(T message) throws ConnectException {
        //重连尝试
        if(connection==null || channel==null){
            init(topic, ipAddress, port);
        }
        if(channel==null){
            throw new ConnectException();
        }

        String msgId = CommonUtils.randomString(32);

        RabbitMQDto dto = new RabbitMQDto();
        dto.setMsgId(msgId);
        dto.setMessage(message);
        String json = JSON.toJSONString(dto);
        try {
            channel.basicPublish(topic, "", null, json.getBytes());
            if (this.sendCallback != null) {
                MQSendLog mqSendLog = new MQSendLog();
                mqSendLog.setTopic(topic);
                mqSendLog.setMsgId(msgId);
                mqSendLog.setMessage(message instanceof String ? message.toString() : JSON.toJSONString(message));
                this.sendCallback.accept(mqSendLog);
            }
            logger.info("发送mq,topic:{},msgId:{},message:{}", topic, msgId, message);
        }catch (IOException e) {
            //ignore
            logger.error("发送mq失败", e);
        }
    }
}

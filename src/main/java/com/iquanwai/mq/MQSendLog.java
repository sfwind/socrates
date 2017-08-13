package com.iquanwai.mq;

import lombok.Data;

/**
 * Created by nethunder on 2017/7/24.
 */
@Data
public class MQSendLog {
    private Integer id;
    private String msgId;
    private String topic;
    private String publisherIp;
    private String message;
    private Boolean sendError;
}

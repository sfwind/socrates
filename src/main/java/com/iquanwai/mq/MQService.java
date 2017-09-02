package com.iquanwai.mq;

import com.iquanwai.domain.dao.MQDealLogDao;
import com.iquanwai.domain.dao.MQSendLogDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by justin on 17/7/26.
 */
@Service
public class MQService {
    @Autowired
    private MQSendLogDao mqSendLogDao;
    @Autowired
    private MQDealLogDao mqDealLogDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String ipAddress;

    public void saveMQSendOperation(MQSendLog mqSendLog) {
        // 插入mqSendOperation
        if (ipAddress == null) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ipAddress = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        mqSendLog.setPublisherIp(ipAddress);
        mqSendLogDao.insert(mqSendLog);
    }


    public void updateAfterDealOperation(RabbitMQDto dto) {
        String msgId = dto.getMsgId();
        if (ipAddress == null) {
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ipAddress = localHost.getHostAddress();
            } catch (UnknownHostException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        MQDealLog mqDealLog = new MQDealLog();
        mqDealLog.setMsgId(msgId);
        mqDealLog.setTopic(dto.getTopic());
        mqDealLog.setQueue(dto.getQueue());
        mqDealLog.setConsumerIp(ipAddress);
        mqDealLogDao.insert(mqDealLog);
    }
}

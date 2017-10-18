package com.iquanwai.mq;

import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Created by nethunder on 2017/10/10.
 */
@Component
public class ApplicationSearchReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String TOPIC = "business_school_application_search";
    private String QUEUE = "search_queue";
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private BusinessSchoolService businessSchoolService;

    @PostConstruct
    public void init() {
        rabbitMQFactory.initReceiver(QUEUE, TOPIC, queueMessage -> {
            String message = queueMessage.getMessage().toString();
            Date date = DateUtils.parseStringToDate(message);
            logger.info("receive message {}", message);
            businessSchoolService.searchApplications(date);
        });
    }
}

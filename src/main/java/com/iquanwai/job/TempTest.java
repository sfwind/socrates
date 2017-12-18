package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TempTest {

    @Autowired
    private CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "10/* * * * * ?")
    public void main() {
        logger.info("开始获取黑名单列表");
        List<String> openIds = customerService.loadBlackListOpenIds();
        openIds.forEach(openId -> System.out.println(openId));
        logger.info("黑名单列表获取结束");
    }

}

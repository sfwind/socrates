package com.iquanwai.job;

import com.iquanwai.domain.PayService;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 16/9/14.
 */
@Component
public class CloseOrderJob {
    @Autowired
    private PayService payService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron = "0 * * * * ?")
    @CatInspect(name = "closeOrder")
    public void work() {
        logger.info("关闭订单任务开始");
        payService.closeOrder();
        logger.info("关闭订单任务结束");
    }
}

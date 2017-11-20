package com.iquanwai.job;

import com.iquanwai.domain.AuditionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditionCompleteRewardJob {

    @Autowired
    private AuditionService auditionService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // @Scheduled(cron = "0 30 21 * * ?")
    @Scheduled(cron = "*/5 * * * * ?")
    public void sendAuditionCompleteReward() {
        logger.info("发送试听课奖学金 job 开始...");
        auditionService.sendAuditionCompleteReward();
        logger.info("发送试听课奖学金 job 结束...");
    }

}

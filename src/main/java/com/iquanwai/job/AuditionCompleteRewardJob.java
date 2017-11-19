package com.iquanwai.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuditionCompleteRewardJob {

    @Scheduled(cron = "")
    public void sendAuditionCompleteReward() {

    }

}

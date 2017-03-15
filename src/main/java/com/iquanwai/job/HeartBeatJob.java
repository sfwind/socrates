package com.iquanwai.job;

import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.po.ImprovementPlan;
import com.iquanwai.domain.po.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by justin on 17/3/15.
 */
@Component
public class HeartBeatJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    @Scheduled(cron="30 * * * * ?")
    public void work(){
        profileDao.load(Profile.class, 1);
        improvementPlanDao.load(ImprovementPlan.class, 1);
        logger.info("Heartbeat trigger");
    }
}

package com.iquanwai.job.asst;

import com.iquanwai.domain.AsstService;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.po.UserRole;
import com.iquanwai.util.cat.CatInspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsstJob {

    @Autowired
    private AsstService asstService;
    @Autowired
    private CustomerService customerService;

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Scheduled(cron="0 0 0 1 * ?")
    @CatInspect(name = "asstJob")
    public void work(){
        logger.info("顺延助教一个月过期时间开始");
        delayExpiredDate();
        logger.info("顺延助教一个月过期时间结束");
    }

    /**
     * 顺延助教过期时间
     */
    private void delayExpiredDate(){
        List<UserRole> userRoles = asstService.getValidAssists();

        List<Integer> profileIds = userRoles.stream().map(UserRole::getProfileId).collect(Collectors.toList());
        //顺延1个月
        customerService.updateExpiredDate(profileIds,1,"MONTH");
    }
}

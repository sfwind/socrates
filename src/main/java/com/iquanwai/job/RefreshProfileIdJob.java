package com.iquanwai.job;

import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.dao.QuanwaiOrderDao;
import com.iquanwai.domain.dao.RiseUserLandingDao;
import com.iquanwai.domain.dao.RiseUserLoginDao;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.QuanwaiOrder;
import com.iquanwai.domain.po.RiseUserLanding;
import com.iquanwai.domain.po.RiseUserLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by justin on 2018/1/23.
 */
@Component
public class RefreshProfileIdJob {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private RiseUserLandingDao riseUserLandingDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private CustomerService customerService;

    @Scheduled(cron="0 0/15 * * * ?")
    public void work(){
        List<RiseUserLanding> riseUserLandingList = riseUserLandingDao.selectAll();
        riseUserLandingList.forEach(riseUserLanding -> {
            try{
                Profile profile = customerService.getProfile(riseUserLanding.getOpenid());
                riseUserLandingDao.update(profile.getId(), riseUserLanding.getId());
            }catch (Exception e){
                logger.error("riseUserLanding {} is not valid", riseUserLanding);
            }

        });

        List<RiseUserLogin> riseUserLogins = riseUserLoginDao.selectAll();
        riseUserLogins.forEach(riseUserLogin -> {
            try {
                Profile profile = customerService.getProfile(riseUserLogin.getOpenid());
                riseUserLoginDao.update(profile.getId(), riseUserLogin.getId());
            } catch (Exception e) {
                logger.error("riseUserLogin {} is not valid", riseUserLogin);
            }

        });

        List<QuanwaiOrder> quanwaiOrders = quanwaiOrderDao.selectAll();
        quanwaiOrders.forEach(quanwaiOrder -> {
            try {
                Profile profile = customerService.getProfile(quanwaiOrder.getOpenid());
                quanwaiOrderDao.update(profile.getId(), quanwaiOrder.getId());
            } catch (Exception e) {
                logger.error("quanwaiOrder {} is not valid", quanwaiOrder);
            }

        });
    }
}

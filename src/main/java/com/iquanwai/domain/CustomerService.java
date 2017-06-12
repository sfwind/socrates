package com.iquanwai.domain;

import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.RiseUserLanding;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/4/13.
 */
@Service
public class CustomerService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private RiseUserLandingDao riseUserLandingDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;

    public void checkMemberExpired(){
        List<RiseMember> riseMembers = riseMemberDao.loadWillCloseMembers();
        for (RiseMember riseMember : riseMembers) {
            if (!riseMember.getExpireDate().after(new Date())) {
                try {
                    logger.info("user:{} expired ad {}", riseMember.getOpenId(), DateUtils.parseDateTimeToString(riseMember.getExpireDate()));
                    profileDao.riseMemberExpired(riseMember.getProfileId());
                    riseMemberDao.riseMemberExpired(riseMember);
                } catch (Exception e){
                    logger.error("expired: {} error", riseMember.getOpenId());
                }
            }
        }
    }

    public void userLoginLog(Integer days){
        List<String> openIds = operationLogDao.loadThatDayLoginUser(days);
        Date thatDay = DateUtils.beforeDays(new Date(), days);

        openIds.forEach(openId -> {
            RiseUserLanding riseUserLanding = riseUserLandingDao.loadByOpenId(openId);
            Date landingDate = null;
            if(riseUserLanding==null){
                landingDate = DateUtils.beforeDays(new Date(), days);
                boolean insert = riseUserLandingDao.insert(openId, landingDate);
                if(!insert){
                    logger.error("插入用户:{} 注册表失败! 日期:{}", openId, landingDate);
                }
            } else {
                landingDate = riseUserLanding.getLandingDate();
            }

            Integer diffDay = DateUtils.interval(thatDay, landingDate);
            boolean insert = riseUserLoginDao.insert(openId, thatDay, diffDay);
            if (!insert) {
                logger.error("插入用户:{} 登录表失败! 日期:{}", openId, thatDay);
            }
        });

    }

    /**
     * 记录昨天用户的情况
     */
    public void userLoginLog(){
        this.userLoginLog(1);
    }
}

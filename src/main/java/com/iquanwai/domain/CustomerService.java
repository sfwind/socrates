package com.iquanwai.domain;

import com.iquanwai.domain.dao.OperationLogDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.RiseUserLandingDao;
import com.iquanwai.domain.dao.RiseUserLoginDao;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.RiseUserLanding;
import com.iquanwai.mq.RabbitMQFactory;
import com.iquanwai.mq.RabbitMQPublisher;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher rabbitMQPublisher;

    public static final String TOPIC = "login_user_reload";
    //训练营用户
    public static final int MEMBER_TYPE_CAMP = 5;

    @PostConstruct
    public void init() {
        rabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(TOPIC);
    }

    public void checkMemberExpired() {
        List<RiseMember> riseMembers = riseMemberDao.loadWillCloseMembers();
        //发送用户信息修改消息
        riseMembers.stream().filter(riseMember -> !riseMember.getExpireDate().after(new Date()))
                .forEach(riseMember -> {
                    try {
                        logger.info("user:{} expired ad {}", riseMember.getOpenId(),
                                DateUtils.parseDateTimeToString(riseMember.getExpireDate()));
                        //训练营用户的用户profile表保留risemember=3
                        if (riseMember.getMemberTypeId() != MEMBER_TYPE_CAMP) {
                            profileDao.riseMemberExpired(riseMember.getProfileId());
                        }

                        riseMemberDao.riseMemberExpired(riseMember);
                        //发送用户信息修改消息
                        rabbitMQPublisher.publish(riseMember.getOpenId());
                    } catch (Exception e) {
                        logger.error("expired: {} error", riseMember.getOpenId());
                    }
                });
    }

    public void userLoginLog(Integer days) {
        List<String> openIds = operationLogDao.loadThatDayLoginUser(days).stream().
                filter(Objects::nonNull).collect(Collectors.toList());
        Date thatDay = DateUtils.beforeDays(new Date(), days);
        openIds.forEach(openId -> {
            RiseUserLanding riseUserLanding = riseUserLandingDao.loadByOpenId(openId);
            Date landingDate = null;
            if (riseUserLanding == null) {
                landingDate = DateUtils.beforeDays(new Date(), days);
                boolean insert = riseUserLandingDao.insert(openId, landingDate);
                if (!insert) {
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
    public void userLoginLog() {
        this.userLoginLog(1);
    }

    public Profile getProfile(Integer id) {
        return profileDao.load(Profile.class, id);
    }
}

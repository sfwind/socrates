package com.iquanwai.domain;

import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.SMSDto;
import com.iquanwai.domain.message.ShortMessageService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.*;
import com.iquanwai.mq.RabbitMQFactory;
import com.iquanwai.mq.RabbitMQPublisher;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ShortMessageService shortMessageService;

    private RabbitMQPublisher rabbitMQPublisher;

    private static final String TOPIC = "login_user_reload";
    //训练营用户
    private static final int MEMBER_TYPE_CAMP = 5;

    private static final String RISE_PAY_PAGE = "/pay/rise";

    // 会员购买申请 发放优惠券的 Category 和 Description
    private static final String RISE_APPLY_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String RISE_APPLY_COUPON_DESCRIPTION = "商学院奖学金";


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

    public void sendRiseMemberApplyMessageByAddTime(Date addTime, Integer distanceDay) {
        String addTimeStr = DateUtils.parseDateToString(addTime);
        // 全部的数据
        List<CustomerStatus> customerStatuses = customerStatusDao.loadCustomerStatusByAddTime(addTimeStr);
        List<Integer> customerStatusProfileIds = customerStatuses.stream().map(CustomerStatus::getProfileId).collect(Collectors.toList());

        // 在 RiseMember 中存在的数据
        List<RiseMember> customerRiseMembers = riseMemberDao.loadValidRiseMemberByProfileIds(customerStatusProfileIds);
        Map<Integer, RiseMember> existRiseMemberMap = customerRiseMembers.stream().collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        for (Integer profileId : customerStatusProfileIds) {
            RiseMember riseMember = existRiseMemberMap.get(profileId);
            if (riseMember == null
                    || riseMember.getMemberTypeId().equals(RiseMember.HALF)
                    || riseMember.getMemberTypeId().equals(RiseMember.ANNUAL)
                    || riseMember.getMemberTypeId().equals(RiseMember.ELITE)
                    || riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE)) {
                List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId, RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION);

                Profile profile = profileDao.load(Profile.class, profileId);

                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(profile.getOpenid());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_PAGE);

                if (coupons.size() > 0) {
                    // 设置消息 message id
                    templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());

                    // 有优惠券模板消息内容
                    String first = profile.getNickname() + "同学，晚上好！快来学习今天的小课，拿下一个职场新技能！\n";
                    data.put("first", new TemplateMessage.Keyword(first, "#000000"));
                    data.put("keyword1", new TemplateMessage.Keyword(plan.getProblemName(), "#000000"));
                    data.put("keyword2",
                            new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()) + "\n\n不需要提醒？可以点此卡片，进入“我的”去关闭",
                                    "#000000"));
                    data.put("remark", new TemplateMessage.Keyword("\n点此卡片开始学习", "#f57f16"));


                    // 有优惠券短信内容
                    SMSDto smsDto = new SMSDto();
                    if (profile.getMobileNo() != null) {
                        smsDto.setProfileId(profileId);
                        smsDto.setPhone(profile.getMobileNo());
                        smsDto.setType(SMSDto.PROMOTION);
                        smsDto.setContent("");
                    }
                    shortMessageService.sendShorMessage(smsDto);
                } else {
                    // 设置消息 message id
                    templateMessage.setTemplate_id(ConfigUtils.getApplySuccessMsg());

                    // 无优惠券模板消息内容
                    String first = profile.getNickname() + "同学，晚上好！快来学习今天的小课，拿下一个职场新技能！\n";
                    data.put("first", new TemplateMessage.Keyword(first, "#000000"));
                    data.put("keyword1", new TemplateMessage.Keyword(plan.getProblemName(), "#000000"));
                    data.put("keyword2",
                            new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()) + "\n\n不需要提醒？可以点此卡片，进入“我的”去关闭",
                                    "#000000"));
                    data.put("remark", new TemplateMessage.Keyword("\n点此卡片开始学习", "#f57f16"));
                }

                templateMessageService.sendMessage(templateMessage);
            }
        }
    }
}

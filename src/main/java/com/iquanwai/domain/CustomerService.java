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
import org.apache.commons.lang3.StringUtils;
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
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ShortMessageService shortMessageService;

    private RabbitMQPublisher userLoadRabbitMQPublisher;
    private RabbitMQPublisher riseExpireRabbitMQPublisher;

    private static final String LOGIN_USER_RELOAD = "login_user_reload";
    private static final String RISE_EXPIRE_MSG = "rise_expire_msg";

    private static final String TOPIC = "login_user_reload";
    //训练营用户
    private static final int MEMBER_TYPE_CAMP = 5;
    private static final String RISE_PAY_URL = "/pay/rise";

    private static final String RISE_PAY_PAGE = "/pay/rise";

    private static final String PERSON_ACCOUNT_PAGE = "/rise/static/customer/account"; // 个人账户页面

    // 会员购买申请 发放优惠券的 Category 和 Description
    private static final String RISE_APPLY_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String RISE_APPLY_COUPON_DESCRIPTION = "商学院奖学金";

    @PostConstruct
    public void init() {
        userLoadRabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(LOGIN_USER_RELOAD);
        riseExpireRabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(RISE_EXPIRE_MSG);
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
                        userLoadRabbitMQPublisher.publish(riseMember.getOpenId());
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

    public Profile getProfile(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        profile.setRiseMember(getRiseMember(profileId));
        return profile;
    }

    public Profile getProfile(String openId) {
        Profile profile = profileDao.loadByOpenId(openId);
        if (profile != null) {
            profile.setRiseMember(getRiseMember(profile.getId()));
        }
        return profile;
    }

    /**
     * 商学院申请通过模板消息
     */
    public void sendRiseMemberApplyMessageByAddTime(Date addTime, Integer distanceDay) {
        List<BusinessSchoolApplication> applications = businessSchoolApplicationDao.loadDealApplicationsForNotice(addTime);
        // 过滤已经过期的申请,dealtime+48小时内不过期
        applications = applications.stream().filter(application ->
                DateUtils.afterDays(application.getDealTime(), 2).after(new Date()))
                .collect(Collectors.toList());

        List<Integer> applyProfileIds = applications.stream().map(BusinessSchoolApplication::getProfileId)
                .collect(Collectors.toList());

        // 在 RiseMember 中存在的数据
        List<RiseMember> customerRiseMembers = riseMemberDao.loadValidRiseMemberByProfileIds(applyProfileIds);
        Map<Integer, RiseMember> existRiseMemberMap = customerRiseMembers.stream()
                .collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        for (BusinessSchoolApplication businessSchoolApplication : applications) {
            Integer profileId = businessSchoolApplication.getProfileId();
            RiseMember riseMember = existRiseMemberMap.get(profileId);
            if (riseMember == null ||
                    (!riseMember.getMemberTypeId().equals(RiseMember.ELITE) && !riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {

                List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId, RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION);
                Profile profile = profileDao.load(Profile.class, profileId);

                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(profile.getOpenid());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_PAGE);

                if (coupons.size() > 0) {
                    Coupon coupon = coupons.get(0);
                    // 设置消息 message id
                    templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());
                    String expiredHourStr = DateUtils.parseDateToString6(
                            DateUtils.afterDays(businessSchoolApplication.getDealTime(), 2));
                    String expiredDateStr = DateUtils.parseDateToString(
                            DateUtils.afterDays(businessSchoolApplication.getDealTime(), 2));
                    // 有优惠券模板消息内容
                    String first = "Hi " + profile.getNickname() + "，您的圈外商学院录取资格及奖学金即将到期，请尽快办理入学！\n";
                    data.put("first", new TemplateMessage.Keyword(first, "#000000"));

                    if (distanceDay == 1) {
                        data.put("keyword1", new TemplateMessage.Keyword("明天" + expiredHourStr + "（" + expiredDateStr + "）", "#000000"));
                    } else if (distanceDay == 0) {
                        data.put("keyword1", new TemplateMessage.Keyword("今天" + expiredHourStr + "（" + expiredDateStr + "）到期", "#000000"));
                    }
                    data.put("keyword2", new TemplateMessage.Keyword("商学院入学奖学金", "#000000"));
                    data.put("keyword3", new TemplateMessage.Keyword(coupon.getAmount().intValue() + "元", "#000000"));
                    data.put("remark", new TemplateMessage.Keyword("\n点此卡片，立即办理入学", "#f57f16"));
                } else {
                    String expiredDateStr = DateUtils.parseDateToString5(
                            DateUtils.afterDays(businessSchoolApplication.getDealTime(), 2));
                    // 设置消息 message id
                    templateMessage.setTemplate_id(ConfigUtils.getApplySuccessMsg());
                    // 无优惠券模板消息内容
                    String first = "我们很荣幸地通知您被商学院录取，录取有效期48小时，请尽快办理入学，及时开始学习并结识优秀的校友吧！\n";
                    data.put("first", new TemplateMessage.Keyword(first, "#000000"));
                    data.put("keyword1", new TemplateMessage.Keyword("已录取", "#000000"));
                    BusinessSchoolApplication application = businessSchoolApplicationDao.loadLastApproveApplication(profileId);
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(application.getCheckTime()), "#000000"));
                    data.put("remark", new TemplateMessage.Keyword("过期时间 :  " + expiredDateStr + "\n\n点击卡片，立即办理入学", "#f57f16"));
                }

                templateMessageService.sendMessage(templateMessage);
            }
        }
    }

    /**
     * 商学院申请通过短信
     */
    public void sendRiseMemberApplyShortMessageByAddTime(Date addTime) {
        String addTimeStr = DateUtils.parseDateToString(addTime);
        // 全部的数据
        List<CustomerStatus> customerStatuses = customerStatusDao.loadCustomerStatusByAddTime(addTimeStr, CustomerStatus.APPLY_BUSINESS_SCHOOL_SUCCESS);
        List<Integer> customerStatusProfileIds = customerStatuses.stream().map(CustomerStatus::getProfileId).collect(Collectors.toList());

        // 在 RiseMember 中存在的数据
        List<RiseMember> customerRiseMembers = riseMemberDao.loadValidRiseMemberByProfileIds(customerStatusProfileIds);
        Map<Integer, RiseMember> existRiseMemberMap = customerRiseMembers.stream().collect(Collectors.toMap(RiseMember::getProfileId, riseMember -> riseMember));

        for (Integer profileId : customerStatusProfileIds) {
            RiseMember riseMember = existRiseMemberMap.get(profileId);
            if (riseMember == null || (!riseMember.getMemberTypeId().equals(RiseMember.ELITE) && !riseMember.getMemberTypeId().equals(RiseMember.HALF_ELITE))) {
                List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId, RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION);

                Profile profile = getProfile(profileId);

                if (coupons.size() > 0) {
                    // 有优惠券短信内容
                    SMSDto smsDto = new SMSDto();
                    if (profile.getMobileNo() != null) {
                        smsDto.setProfileId(profileId);
                        smsDto.setPhone(profile.getMobileNo());
                        smsDto.setType(SMSDto.PROMOTION);
                        String content = "Hi " + profile.getNickname() +
                                "，你申请的商学院入学奖学金即将到期，请至「圈外同学」公众号，办理入学并使用吧！如有疑问请联系圈外小黑(微信号：quanwaizhushou2) 回复TD退订";
                        smsDto.setContent(content);
                    }
                    shortMessageService.sendShorMessage(smsDto);
                }
            }
        }
    }

    /**
     * 在特定日期内，即将过期的会员人数
     */
    public List<RiseMember> loadRiseMembersByExpireDate(Date expireDate) {
        String dateStr = DateUtils.parseDateToString(expireDate);
        return riseMemberDao.loadRiseMembersByExpireDate(dateStr);
    }

    /**
     * 给即将过期会员发送模板消息
     */
    public void sendWillExpireMessage(List<RiseMember> riseMembers, Integer distanceDay) {
        List<Integer> profileIds = riseMembers.stream().map(RiseMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = profileDao.loadByProfileIds(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        TemplateMessage templateMessage = new TemplateMessage();
        for (RiseMember riseMember : riseMembers) {
            Profile profile = profileMap.get(riseMember.getProfileId());
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setTemplate_id(ConfigUtils.getRiseMemberExpireMsg());
            templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_URL);
            String first;
            if (distanceDay != 0) {
                first = "Hi " + profile.getNickname() + "，小哥哥例行维护信息时，发现您的会员" + distanceDay + "天后到期哦：";
            } else {
                first = "Hi " + profile.getNickname() + "，小哥哥例行维护信息时，发现您的会员今天到期哦：";
            }
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            data.put("name", new TemplateMessage.Keyword(convertMemberTypeStr(riseMember.getMemberTypeId()), "#000000"));
            data.put("expDate", new TemplateMessage.Keyword(DateUtils.parseDateToString(DateUtils.beforeDays(riseMember.getExpireDate(), 1)) + "\n\n到期前加入商学院，可以免申请入学哦！到期后可以复习，但不能选新课啦", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("\n点击卡片，立即加入商学院，加速你的职业发展吧！", "#f57f16"));
            templateMessageService.sendMessage(templateMessage);
        }
    }

    /**
     * 给即将过期的会员发送短信
     */
    public void sendWillExpireShortMessage(List<RiseMember> riseMembers, Integer distanceDay) {
        List<Integer> profileIds = riseMembers.stream().map(RiseMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = profileDao.loadByProfileIds(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        // Hi xxxx，您的半年版会员/一年版会员/商学院会员N天后/今天到期哦！有疑问请联系圈外小黑（微信ID：quanwaizhushou2）
        for (RiseMember riseMember : riseMembers) {
            Profile profile = profileMap.get(riseMember.getProfileId());

            SMSDto smsDto = new SMSDto();
            smsDto.setProfileId(profile.getId());
            String mobileNo = profile.getMobileNo();
            if (!StringUtils.isEmpty(mobileNo)) {
                smsDto.setPhone(profile.getMobileNo());
            }
            String content;
            if (distanceDay != 0) {
                content = "Hi " + profile.getNickname() + "，您的" + convertMemberTypeStr(riseMember.getMemberTypeId()) + distanceDay
                        + "天后到期哦！有疑问请联系圈外小黑（微信号：quanwaizhushou2）回复TD退订";
            } else {
                content = "Hi " + profile.getNickname() + "，您的" + convertMemberTypeStr(riseMember.getMemberTypeId()) +
                        "今天到期哦！有疑问请联系圈外小黑（微信号：quanwaizhushou2）回复TD退订";
            }
            smsDto.setContent(content);
            smsDto.setType(SMSDto.PROMOTION);
            shortMessageService.sendShorMessage(smsDto);
        }
    }

    /**
     * 给优惠券即将过期的人发送模板消息
     */
    public void sendWillExpireCouponMessage(Date distanceDay) {
        String distanceDayStr = DateUtils.parseDateToString(distanceDay);
        List<Coupon> coupons = couponDao.loadCouponsByExpireDate(distanceDayStr);
        List<Integer> profileIds = coupons.stream().map(Coupon::getProfileId).collect(Collectors.toList());
        Map<Integer, Coupon> couponMap = coupons.stream().collect(Collectors.toMap(Coupon::getProfileId, coupon -> coupon));

        List<Profile> profiles = profileDao.loadByProfileIds(profileIds);

        for (Profile profile : profiles) {
            Coupon coupon = couponMap.get(profile.getId());

            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());
            templateMessage.setUrl(ConfigUtils.getAppDomain() + PERSON_ACCOUNT_PAGE);
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);

            String description = coupon.getDescription().contains("奖学金") ? "奖学金" : "优惠券";
            String first = "Hi " + profile.getNickname() + "，你的" + description + "即将到期，请尽快使用吧！\n";
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword("1天后（" + DateUtils.parseDateToString(DateUtils.beforeDays(coupon.getExpiredDate(), 1)) + "）", "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword(description, "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword(coupon.getAmount().intValue() + "元", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("\n点击卡片查看详情。", "#f57f16"));

            templateMessageService.sendMessage(templateMessage);
        }

    }

    private Integer getRiseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember == null) {
            return 0;
        }
        Integer memberTypeId = riseMember.getMemberTypeId();
        if (memberTypeId == null) {
            return 0;
        }
        // 精英或者专业版用户
        if (memberTypeId == RiseMember.HALF || memberTypeId == RiseMember.ANNUAL
                || memberTypeId == RiseMember.ELITE || memberTypeId == RiseMember.HALF_ELITE) {
            return 1;
        } else if (memberTypeId == RiseMember.CAMP) {
            return 3;
        } else if (memberTypeId == RiseMember.COURSE) {
            return 2;
        } else {
            return 0;
        }
    }

    private String convertMemberTypeStr(Integer memberTypeId) {
        String memberTypeStr = "";
        switch (memberTypeId) {
            case RiseMember.HALF:
                memberTypeStr = "半年版会员";
                break;
            case RiseMember.ANNUAL:
                memberTypeStr = "一年版会员";
                break;
            case RiseMember.ELITE:
                memberTypeStr = "商学院会员";
                break;
            case RiseMember.HALF_ELITE:
                memberTypeStr = "商学院会员";
                break;
            default:
                break;
        }
        return memberTypeStr;
    }
}

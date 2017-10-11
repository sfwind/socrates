package com.iquanwai.domain;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.SMSDto;
import com.iquanwai.domain.message.ShortMessageService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.RiseUserLanding;
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
    private TemplateMessageService templateMessageService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private RabbitMQPublisher userLoadRabbitMQPublisher;
    private RabbitMQPublisher riseExpireRabbitMQPublisher;

    private static final String LOGIN_USER_RELOAD = "login_user_reload";
    private static final String RISE_EXPIRE_MSG = "rise_expire_msg";

    //训练营用户
    private static final int MEMBER_TYPE_CAMP = 5;
    private static final String RISE_PAY_URL = "/pay/rise";

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

    public Profile getProfile(Integer id) {
        return profileDao.load(Profile.class, id);
    }

    public List<RiseMember> loadRiseMembersByExpireDate(Date expireDate) {
        String dateStr = DateUtils.parseDateToString(expireDate);
        return riseMemberDao.loadRiseMembersByExpireDate(dateStr);
    }

    public void sendWillExpireMessage(List<RiseMember> riseMembers, Integer distanceDay) {
        List<Integer> profileIds = riseMembers.stream().map(RiseMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = profileDao.loadByProfileIds(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        TemplateMessage templateMessage = new TemplateMessage();
        for (RiseMember riseMember : riseMembers) {
            Integer profileId = riseMember.getProfileId();
            Profile profile = profileMap.get(profileId);
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setTemplate_id(ConfigUtils.getRiseMemberExpireMsg());
            templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_URL);
            StringBuilder first = new StringBuilder();
            if (distanceDay != 0) {
                first.append("Hi " + profile.getNickname() + "小哥哥例行维护信息时，发现您的会员" + distanceDay + "天后到期哦：");
            } else {
                first.append("Hi " + profile.getNickname() + "小哥哥例行维护信息时，发现您的会员今天到期哦：");
            }
            data.put("first", new TemplateMessage.Keyword(first.toString(), "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword(convertMemberTypeStr(riseMember.getMemberTypeId()), "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword(riseMember.getExpired() + "\n\n到期前加入商学院，可以免申请入学哦！到期后可以复习，但不能选新课啦！", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("\n点击卡片，立即加入商学院，加速你的职业发展吧！", "#f57f16"));
            templateMessageService.sendMessage(templateMessage);
        }
    }

    public void sendWillExpireShortMessage(List<RiseMember> riseMembers, Integer distanceDay) {
        List<Integer> profileIds = riseMembers.stream().map(RiseMember::getProfileId).collect(Collectors.toList());
        List<Profile> profiles = profileDao.loadByProfileIds(profileIds);
        Map<Integer, Profile> profileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        // Hi xxxx，您的半年版会员/一年版会员/商学院会员N天后/今天到期哦！有疑问请联系圈外小黑（微信ID：quanwaizhushou2）
        for (RiseMember riseMember : riseMembers) {
            Profile profile = profileMap.get(riseMember.getId());

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("profileId", riseMember.getProfileId());
            jsonObject.put("content", "Hi " + profile.getNickname() + "，您的" + convertMemberTypeStr(riseMember.getMemberTypeId()) + distanceDay
                    + "天后到期哦！有疑问请联系圈外小黑（微信ID：quanwaizhushou2）");

            SMSDto smsDto = new SMSDto();
            smsDto.setProfileId(profile.getId());
            String mobileNo = profile.getMobileNo();
            if (!StringUtils.isEmpty(mobileNo)) {
                smsDto.setPhone(profile.getMobileNo());
            }
            String content = "Hi " + profile.getNickname() + "，您的" + convertMemberTypeStr(riseMember.getMemberTypeId()) + distanceDay
                    + "天后到期哦！有疑问请联系圈外小黑（微信ID：quanwaizhushou2）";
            smsDto.setContent(content);
            smsDto.setType(SMSDto.PROMOTION);
            shortMessageService.sendShorMessage(smsDto);
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
        }
        return memberTypeStr;
    }
}

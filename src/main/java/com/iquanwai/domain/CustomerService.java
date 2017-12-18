package com.iquanwai.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.*;
import com.iquanwai.domain.po.Coupon;
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
import java.util.*;
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
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private RestfulHelper restfulHelper;


    private RabbitMQPublisher userLoadRabbitMQPublisher;

    private static final String LOGIN_USER_RELOAD = "login_user_reload";
    //训练营用户
    private static final int MEMBER_TYPE_CAMP = 5;
    private static final String RISE_PAY_URL = "/pay/rise";

    private static final String PERSON_ACCOUNT_PAGE = "/rise/static/customer/account"; // 个人账户页面

    // 会员购买申请 发放优惠券的 Category 和 Description
    private static final String RISE_APPLY_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String RISE_APPLY_COUPON_DESCRIPTION = "商学院奖学金";


    String LIST_BLACKLIST_URL = "https://api.weixin.qq.com/cgi-bin/tags/members/getblacklist?access_token={access_token}";
    private final int WX_BLACKLIST_DEFAULT_PAGE_SIZE = 10000;

    @PostConstruct
    public void init() {
        userLoadRabbitMQPublisher = rabbitMQFactory.initFanoutPublisher(LOGIN_USER_RELOAD);
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

    public List<String> loadBlackListOpenIds() {
        String url = LIST_BLACKLIST_URL;
        int count = 0;
        List<String> blackList = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("begin_openid", "");
        String body = restfulHelper.post(url, jsonObject.toJSONString());
        String data = JSON.parseObject(body).getString("data");
        //获取data中的openidList
        if (data != null) {
            JSONObject dataJSON = JSON.parseObject(data);
            String openidList = dataJSON.getString("openid");
            blackList.addAll(Arrays.asList(openidList.substring(1, openidList.length() - 1).split(",")));
            String nextOpenid = JSON.parseObject(body).getString("next_openid");

            int total = Integer.valueOf(JSON.parseObject(body).getString("total"));
            //取出所有的openid
            while ((total - 1) / WX_BLACKLIST_DEFAULT_PAGE_SIZE > count) {
                jsonObject = new JSONObject();
                jsonObject.put("begin_openid", nextOpenid);
                body = restfulHelper.post(url, jsonObject.toJSONString());
                data = JSON.parseObject(body).getString("data");

                dataJSON = JSON.parseObject(data);
                openidList = dataJSON.getString("openid");
                blackList.addAll(Arrays.asList(openidList.substring(1, openidList.length() - 1).split(",")));
                nextOpenid = JSON.parseObject(body).getString("next_openid");

                count++;
            }
        }
        return blackList;
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
            data.put("expDate", new TemplateMessage.Keyword(DateUtils.parseDateToString(
                    DateUtils.beforeDays(riseMember.getExpireDate(), 1)) + "\n\n到期前加入商学院，可以免申请入学哦！到期后可以复习，但不能选新课啦", "#000000"));
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
            shortMessageService.sendShortMessage(smsDto);
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

        // 过滤出黑名单人员，对于在黑名单中的人员，不发送提醒消息
        List<String> blackListOpenIds = loadBlackListOpenIds();
        profiles = profiles.stream().filter(profile -> !blackListOpenIds.contains(profile.getOpenid()))
                .collect(Collectors.toList());

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
            data.put("keyword1", new TemplateMessage.Keyword("1天后（" +
                    DateUtils.parseDateToString(DateUtils.beforeDays(coupon.getExpiredDate(), 1)) + "）", "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword(description, "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword(coupon.getAmount() + "元", "#000000"));
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

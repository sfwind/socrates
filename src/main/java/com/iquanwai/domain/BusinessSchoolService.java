package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.SMSDto;
import com.iquanwai.domain.message.ShortMessageService;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/10/4.
 */
@Service
public class BusinessSchoolService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    public Integer BS_APPLICATION;
    public static String PAY_URL = "https://www.iquanwai.com/pay/apply";
    public static String PAY_CAMP_URL = "https://www.iquanwai.com/pay/camp";

    @PostConstruct
    public void init() {
        BS_APPLICATION = ConfigUtils.getBsApplicationActivity();
    }

    @Autowired
    private SurveySubmitDao surveySubmitDao;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private CouponDao couponDao;

    private static final String RISE_PAY_PAGE = "/pay/rise";

    // 会员购买申请 发放优惠券的 Category 和 Description
    private static final String RISE_APPLY_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String RISE_APPLY_COUPON_DESCRIPTION = "商学院奖学金";

    public void searchApplications(Date date) {
        List<SurveySubmit> surveySubmits = surveySubmitDao.loadSubmitGroup(BS_APPLICATION, date).stream().filter(item -> {
            // 过滤已经入库的申请
            return businessSchoolApplicationDao.loadBySubmitId(item.getId()) == null;
        }).collect(Collectors.toList());
        logger.info("查找:{} ，共{}条", DateUtils.parseDateToString(date), surveySubmits.size());
        /*
          处理步骤：
          1.判断 status
          2.固化当前会员类型
          3.是否重复提交（老数据,这个批次）
         */
        Map<String, List<SurveySubmit>> surveyGroup = surveySubmits.stream().collect(Collectors.groupingBy(SurveySubmit::getOpenId));
        surveyGroup.forEach((openId, thisBatch) -> {
            Profile profile = profileDao.loadByOpenId(openId);
            List<BusinessSchoolApplication> otherBatch = businessSchoolApplicationDao.getUserApplications(profile.getId(), date, 60);
            Integer minSubmitId = thisBatch.stream().mapToInt(SurveySubmit::getId).min().getAsInt();
            List<BusinessSchoolApplication> waitDeal = thisBatch.stream().map(survey -> {
                BusinessSchoolApplication application = new BusinessSchoolApplication();
                // 是否重复提交 !(老批次没有，并且这个批次是最小的id)
                application.setIsDuplicate(!(CollectionUtils.isEmpty(otherBatch) && survey.getId().equals(minSubmitId)));
                // 固化当前会员类型
                RiseMember riseMember = riseMemberDao.loadValidRiseMember(profile.getId());
                application.setOriginMemberType(riseMember != null ? riseMember.getMemberTypeId() : null);
                // 判断status,不是申请中和自动关闭
                boolean findOld = otherBatch.stream().anyMatch(item -> item.getStatus() != BusinessSchoolApplication.APPLYING
                        && item.getStatus() != BusinessSchoolApplication.AUTO_CLOSE);
                Integer status;
                if (!findOld) {
                    // 之前没有处理过,一个月之内,并且当前不是精英版
                    if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE
                            || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
                        logger.info("精英版自动关闭:{}", profile.getId());
                        application.setComment("精英版自动关闭");
                        status = BusinessSchoolApplication.AUTO_CLOSE;
                    } else {
                        status = BusinessSchoolApplication.APPLYING;
                    }
                    if (CollectionUtils.isNotEmpty(otherBatch)) {
                        // 之前没处理过，并且有老的申请，将老的申请关掉,注意，只会关闭未处理(status=0)的订单
                        otherBatch.forEach(item -> {
                            logger.info("关掉老的申请:{}", profile.getId());
                            businessSchoolApplicationDao.autoCloseApplication(item.getId());
                        });
                    }
                } else {
                    logger.info("已经处理过:{}", profile.getId());
                    status = BusinessSchoolApplication.AUTO_CLOSE;
                    application.setComment("近期已经处理过");
                }
                application.setStatus(status);
                // 常规数据初始化
                application.setSubmitId(survey.getId());
                application.setProfileId(profile.getId());
                application.setOpenid(profile.getOpenid());
                application.setCheckTime(status != BusinessSchoolApplication.APPLYING ? new Date() : null);
                application.setDeal(status != BusinessSchoolApplication.APPLYING);
                application.setDealTime(status != BusinessSchoolApplication.APPLYING ? new Date() : null);
                application.setSubmitTime(survey.getSubmitTime());
                return application;
            }).collect(Collectors.toList());
            for (BusinessSchoolApplication application : waitDeal) {
                Integer id = businessSchoolApplicationDao.insert(application);
                logger.info("插入商学院申请:{}", id);
            }
        });
    }

    public void noticeApplication(Date date) {
        List<BusinessSchoolApplication> applications = businessSchoolApplicationDao.loadCheckApplicationsForNotice(date);
        logger.info("待发送通知:{} 条", applications.size());
        Map<Integer, List<BusinessSchoolApplication>> waitNoticeMap = applications.stream()
                .collect(Collectors.groupingBy(BusinessSchoolApplication::getStatus));
        // 通知 通过的
        List<BusinessSchoolApplication> approveGroup = waitNoticeMap.getOrDefault(BusinessSchoolApplication.APPROVE, Lists.newArrayList());
        this.noticeApplicationForApprove(approveGroup);
        // 通知 拒信
        List<BusinessSchoolApplication> rejectGroup = waitNoticeMap.getOrDefault(BusinessSchoolApplication.REJECT, Lists.newArrayList());
        this.noticeApplicationForReject(rejectGroup);
    }

    public void noticeApplicationForReject(List<BusinessSchoolApplication> applications) {
        logger.info("审核拒绝:{}条", applications.size());
        // 发送有优惠券的
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.getRejectApplyMsgId());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(PAY_CAMP_URL);
        templateMessage.setComment("发送拒信");
        data.put("keyword1", new TemplateMessage.Keyword("【圈外商学院】"));
        data.put("keyword2", new TemplateMessage.Keyword("未通过"));
        data.put("remark", new TemplateMessage.Keyword(
                "\n本期商学院的申请者都异常优秀，我们无法为每位申请者提供学习机会，但是很高兴你有一颗追求卓越的心！\n\n点击下方“详情”，了解商学院预科班--圈外训练营。"));
        // 同样的对象不需要定义两次
        data.put("first", new TemplateMessage.Keyword(
                "认真审核过你的入学申请后，我们很遗憾地通知，你本次未被商学院录取。\n\n" +
                        "在此之前，我们推荐你进入【训练营】进行学习。训练营能够帮你快速提高专项能力，为你进入商学院做好准备。点击了解训练营。\n"));
        applications.forEach(app -> this.sendMsg(templateMessage, data, app, "keyword3"));
    }

    public void noticeApplicationForApprove(List<BusinessSchoolApplication> applications) {
        Integer count = applications != null ? applications.size() : 0;
        logger.info("审核通过:{} 条", count);
        if (count == 0) {
            return;
        }
        Map<Double, List<BusinessSchoolApplication>> coupons = applications.stream().collect(Collectors.groupingBy(BusinessSchoolApplication::getCoupon));
        // 没有优惠券
        List<BusinessSchoolApplication> noCouponGroup = coupons.remove(0d);
        coupons.forEach((amount, group) -> logger.info("{}元优惠券,{}条", amount, group.size()));
        logger.info("无优惠券,{}条", noCouponGroup == null ? 0 : noCouponGroup.size());
        // 发送有优惠券的
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(PAY_URL);
        templateMessage.setComment("商学院审核通过");
        data.put("keyword1", new TemplateMessage.Keyword("通过"));
        data.put("remark", new TemplateMessage.Keyword("奖学金和录取通知2天内有效，请及时点击本通知书，办理入学。", "#f57f16"));
        // 同样的对象不需要定义两次
        coupons.forEach((amount, applicationGroup) -> {
            data.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！" +
                    "\n\n根据你的申请，入学委员会决定发放给你" + amount.intValue()
                    + "元奖学金，付款时自动抵扣学费。希望你在商学院内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
            applicationGroup.forEach(app -> this.sendMsg(templateMessage, data, app, "keyword2"));
        });


        // 发送没有优惠券的模版
        TemplateMessage noCouponMsg = new TemplateMessage();
        noCouponMsg.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
        noCouponMsg.setUrl(PAY_URL);
        noCouponMsg.setComment("商学院审核通过,无优惠券");
        Map<String, TemplateMessage.Keyword> noCouponData = Maps.newHashMap();
        noCouponMsg.setData(noCouponData);
        noCouponData.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！希望你在商学院内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
        noCouponData.put("keyword1", new TemplateMessage.Keyword("通过"));
        noCouponData.put("remark", new TemplateMessage.Keyword("\n本录取通知书2天内有效，过期后需重新申请。请及时点击本通知书，办理入学。", "#f57f16"));
        // 发送没有优惠券的
        if (noCouponGroup != null) {
            noCouponGroup.forEach(app -> this.sendMsg(noCouponMsg, noCouponData, app, "keyword2"));
        }
    }


    private void sendMsg(TemplateMessage templateMessage, Map<String, TemplateMessage.Keyword> data,
                         BusinessSchoolApplication application, String checkKey) {
        templateMessage.setTouser(application.getOpenid());
        data.put(checkKey, new TemplateMessage.Keyword(DateUtils.parseDateToString(application.getCheckTime())));
        logger.info("发送模版消息id ：{}", templateMessage.getTemplate_id());
        // 录取通知强制发送
        templateMessageService.sendMessage(templateMessage, false);
        // 有优惠券短信内容
        Integer profileId = application.getProfileId();
        Profile profile = profileDao.load(Profile.class, profileId);
        SMSDto smsDto = new SMSDto();
        if (profile != null && profile.getMobileNo() != null) {
            smsDto.setProfileId(profileId);
            smsDto.setPhone(profile.getMobileNo());
            smsDto.setType(SMSDto.PROMOTION);
            String content = "Hi " + profile.getNickname() +
                    "，感谢申请圈外商学院，您的申请结果已公布，现在就去「圈外同学」微信公众号查收吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
            smsDto.setContent(content);
            shortMessageService.sendShortMessage(smsDto);
        }

        // 更新提醒状态
        businessSchoolApplicationDao.updateNoticeAction(application.getId());
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

                // 只查看未过期的
                List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId,
                        RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION)
                        .stream()
                        .filter(coupon -> new DateTime(coupon.getExpiredDate()).isAfterNow())
                        .collect(Collectors.toList());

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
                    data.put("keyword3", new TemplateMessage.Keyword(coupon.getAmount() + "元", "#000000"));
                    data.put("remark", new TemplateMessage.Keyword("\n点此卡片，立即办理入学", "#f57f16"));

                    // 有优惠券短信内容
                    SMSDto smsDto = new SMSDto();
                    if (profile.getMobileNo() != null) {
                        smsDto.setProfileId(profileId);
                        smsDto.setPhone(profile.getMobileNo());
                        smsDto.setType(SMSDto.PROMOTION);
                        String content = "Hi " + profile.getNickname() +
                                "，你申请的商学院入学奖学金即将到期，请至「圈外同学」公众号，办理入学并使用吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
                        smsDto.setContent(content);
                    }
                    shortMessageService.sendShortMessage(smsDto);
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

                    // 有优惠券短信内容
                    SMSDto smsDto = new SMSDto();
                    if (profile.getMobileNo() != null) {
                        smsDto.setProfileId(profileId);
                        smsDto.setPhone(profile.getMobileNo());
                        smsDto.setType(SMSDto.PROMOTION);
                        String content = "Hi " + profile.getNickname() +
                                "，你申请的商学院入学资格即将到期，请至「圈外同学」公众号，办理入学并使用吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
                        smsDto.setContent(content);
                        shortMessageService.sendShortMessage(smsDto);
                    }

                }

                templateMessageService.sendMessage(templateMessage);
            }
        }
    }
}

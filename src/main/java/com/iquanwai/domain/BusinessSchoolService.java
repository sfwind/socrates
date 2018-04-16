package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.dao.member.MemberTypeDao;
import com.iquanwai.domain.message.*;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.Constants;
import com.iquanwai.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public final static String PAY_URL = ConfigUtils.getAppDomain() + "/pay/apply?goodsId=";
    public final static String PAY_CAMP_URL = ConfigUtils.getAppDomain() + "/pay/camp";
    public final static Integer ELITE_GOODS = 3;
    public final static Integer THOUGHT_GOODS = 8;

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
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MemberTypeDao memberTypeDao;

    // 会员购买申请 发放优惠券的 Category 和 Description
    private static final String RISE_APPLY_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String RISE_APPLY_COUPON_DESCRIPTION = "商学院奖学金";
    private static final String APPLY_COUPON_DESCRIPTION = "商学院申请费返还";


    /**
     * 发送申请审核结果
     *
     * @param date 当天以及当天之前
     */
    public void noticeApplication(Date date) {
        List<MemberType> memberTypes = memberTypeDao.loadAll(MemberType.class).stream().filter(memberType -> memberType.getDel() == 0).collect(Collectors.toList());

        List<BusinessSchoolApplication> applications = businessSchoolApplicationDao.loadCheckApplicationsForNotice(date);
        logger.info("待发送通知:{} 条", applications.size());
        Map<Integer, List<BusinessSchoolApplication>> waitNoticeMap = applications.stream()
                .collect(Collectors.groupingBy(BusinessSchoolApplication::getStatus));
        // 通知 通过的
        List<BusinessSchoolApplication> approveGroup = waitNoticeMap.getOrDefault(BusinessSchoolApplication.APPROVE, Lists.newArrayList());
        this.noticeApplicationForApprove(approveGroup, memberTypes);
        // 通知 拒信
        List<BusinessSchoolApplication> rejectGroup = waitNoticeMap.getOrDefault(BusinessSchoolApplication.REJECT, Lists.newArrayList());

        this.noticeApplicationForReject(rejectGroup, memberTypes);
    }

    public void noticeApplicationForReject(List<BusinessSchoolApplication> applications, List<MemberType> memberTypes) {

        logger.info("审核拒绝:{}条", applications.size());
        // 发送有优惠券的
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.getRejectApplyMsgId());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        data.put("keyword2", new TemplateMessage.Keyword("未通过"));
        templateMessage.setUrl(PAY_CAMP_URL);
        templateMessage.setComment("发送拒信");
        applications.forEach(app -> {

                    String remark = "\n点击扫描二维码，添加【圈外招生委员会】微信，实时关注和咨询招生信息\n\n" +
                            "即可获得：{}课程知识礼包";
                    String first = null;
                    //添加项目判断（2018-04-10）
                    int memberTypeId = app.getMemberTypeId();
                    MemberType existMemberType = memberTypes.stream().filter(memberType -> memberType.getId() == memberTypeId).findAny().orElse(null);
                    if(existMemberType!=null){
                        remark = remark.replace("{}", existMemberType.getDescription());
                        data.put("keyword1", new TemplateMessage.Keyword("【" + existMemberType.getDescription() + "】"));
                        first = "我们认真评估了你的入学申请，认为你的需求和圈外商学院" + existMemberType.getDescription() + "暂时不匹配\n\n" +
                                "本期圈外商学院" + existMemberType.getDescription() + "的申请者都异常优秀，我们无法为每位申请者提供学习机会，" +
                                "但是很高兴你有一颗追求卓越的心！\n" +
                                "\n欢迎继续关注后续的课程与体验活动\n";
                    }

                    data.put("remark", new TemplateMessage.Keyword(
                            remark, "#f57f16"));
                    // 同样的对象不需要定义两次
                    data.put("first", new TemplateMessage.Keyword(
                            first));
                    this.sendMsg(templateMessage, data, app, "keyword3", memberTypes);
                }

        );
    }


    /**
     * 发送申请通过的通知
     *
     * @param applications 申请记录
     */
    public void noticeApplicationForApprove(List<BusinessSchoolApplication> applications, List<MemberType> memberTypes) {
        Integer count = applications != null ? applications.size() : 0;
        logger.info("审核通过:{} 条", count);
        if (count == 0) {
            return;
        }


        applications.forEach(application -> {
            // 发放优惠券，开白名单
            try {
                int profileId = application.getProfileId();
                //2   8
                int memberTypeId = application.getMemberTypeId();
                //判断是否已经买过对应的课程（2018-04-10）
                if (checkIsBuy(profileId, memberTypeId)) {
                    return;
                }

                // 是否有优惠券
                List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId,
                        RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION);
                if (CollectionUtils.isEmpty(coupons)) {
                    if (application.getCoupon() != null && application.getCoupon() > 0) {
                        Coupon couponBean = new Coupon();
                        couponBean.setAmount(application.getCoupon().intValue());
                        couponBean.setProfileId(profileId);
                        couponBean.setUsed(Coupon.UNUSED);
                        couponBean.setExpiredDate(DateUtils.afterDays(new Date(), 2));
                        couponBean.setCategory(RISE_APPLY_COUPON_CATEGORY);
                        couponBean.setDescription(RISE_APPLY_COUPON_DESCRIPTION);
                        couponDao.insert(couponBean);
                    }
                }

                // 如果收了申请费,则添加一张等价优惠券
//                String orderId = application.getOrderId();
//                if (orderId != null) {
//                    QuanwaiOrder order = quanwaiOrderDao.loadOrder(orderId);
//                    if (order != null) {
//                        // 是否有优惠券
//                        coupons = couponDao.loadCouponsByProfileId(profileId,
//                                RISE_APPLY_COUPON_CATEGORY, APPLY_COUPON_DESCRIPTION);
//                        if (CollectionUtils.isEmpty(coupons)) {
//                            if (order.getStatus().equals(QuanwaiOrder.PAID)) {
//                                // 添加优惠券
//                                Coupon couponBean = new Coupon();
//                                couponBean.setAmount(order.getPrice().intValue());
//                                couponBean.setProfileId(profileId);
//                                couponBean.setUsed(Coupon.UNUSED);
//                                couponBean.setExpiredDate(DateUtils.afterDays(new Date(), 2));
//                                couponBean.setCategory(RISE_APPLY_COUPON_CATEGORY);
//                                couponBean.setDescription(APPLY_COUPON_DESCRIPTION);
//                                couponDao.insert(couponBean);
//                            }
//                        }
//                    }
//                }
                //判断项目类型（2018-04-10）
                MemberType existMember = memberTypes.stream().filter(memberType -> memberType.getId() == memberTypeId).findAny().orElse(null);
                if (existMember != null) {
                    messageService.sendMessage("恭喜！我们很荣幸地通知你被【圈外商学院" + existMember.getDescription() + "】录取！请及时点击本通知书，办理入学。",
                            String.valueOf(profileId), MessageService.SYSTEM_MESSAGE, PAY_URL + existMember.getId());
                }
            } catch (Exception e) {
                logger.error("插入优惠券失败", e);
            }
        });

        Map<Double, List<BusinessSchoolApplication>> coupons = applications.stream().filter(businessSchoolApplication -> businessSchoolApplication.getMemberTypeId().equals(Constants.MemberType.ELITE)).collect(Collectors.groupingBy(BusinessSchoolApplication::getCoupon));
        Map<Double, List<BusinessSchoolApplication>> mbaCoupons = applications.stream().filter(businessSchoolApplication -> businessSchoolApplication.getMemberTypeId().equals(Constants.MemberType.THOUGHT)).collect(Collectors.groupingBy(BusinessSchoolApplication::getCoupon));
        // 没有优惠券
        List<BusinessSchoolApplication> noCouponGroup = coupons.remove(0d);
        List<BusinessSchoolApplication> mbaNoCouponGroup = mbaCoupons.remove(0d);
        coupons.forEach((amount, group) -> logger.info("{}元优惠券:{}条", amount, group.size()));
        logger.info("无优惠券:{}条", noCouponGroup == null ? 0 : noCouponGroup.size());

        mbaCoupons.forEach((amount, group) -> logger.info("{}元优惠券:{}条", amount, group.size()));
        logger.info("无优惠券:{}条", mbaNoCouponGroup == null ? 0 : mbaNoCouponGroup.size());

        //发送奖学金消息（）
        sendCouponMsgs(coupons, noCouponGroup, mbaCoupons, mbaNoCouponGroup, memberTypes);
    }


    private void sendMsg(TemplateMessage templateMessage, Map<String, TemplateMessage.Keyword> data,
                         BusinessSchoolApplication application, String checkKey, List<MemberType> memberTypes) {

        int profileId = application.getProfileId();
        int memberTypeId = application.getMemberTypeId();
        //判断是否已经购买过
        if (checkIsBuy(profileId, memberTypeId)) {
            return;
        }

        Profile profile = profileDao.load(Profile.class, profileId);
        templateMessage.setTouser(profile.getOpenid());
        data.put(checkKey, new TemplateMessage.Keyword(DateUtils.parseDateToString(application.getCheckTime())));
        logger.info("发送模版消息id ：{}", templateMessage.getTemplate_id());
        // 录取通知强制发送
        templateMessageService.sendMessage(templateMessage, false);
        // 有优惠券短信内容
        if (profile.getMobileNo() != null) {
            SMSDto smsDto = new SMSDto();
            smsDto.setProfileId(profileId);
            smsDto.setPhone(profile.getMobileNo());
            smsDto.setType(SMSDto.PROMOTION);
            String content = "Hi，感谢申请圈外商学院{}，您的申请结果已公布，现在就去「圈外同学」微信公众号查收吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
            //添加项目类型判断（2018-04-10）
            MemberType existMember = memberTypes.stream().filter(memberType -> memberType.getId() == memberTypeId).findAny().orElse(null);
            if(existMember!=null){
                content = content.replace("{}", existMember.getDescription());
            }
            smsDto.setContent(content);
            shortMessageService.sendShortMessage(smsDto);
        }

        // 更新提醒状态
        businessSchoolApplicationDao.updateNoticeAction(application.getId());
    }

    /**
     * 商学院申请通过模板消息
     */
    public void sendRiseMemberApplyMessageByDealTime(Date dealTime, Integer distanceDay) {
        List<MemberType> memberTypes = memberTypeDao.loadAll(MemberType.class).stream().filter(memberType -> memberType.getDel() == 0).collect(Collectors.toList());
        //MemberType eliteMemberType = memberTypes.stream().filter(memberType -> memberType.getId() == RiseMember.ELITE).findAny().orElse(null);
      //  MemberType mbaMemberType = memberTypes.stream().filter(memberType -> memberType.getId() == RiseMember.BUSINESS_THOUGHT).findAny().orElse(null);

        List<BusinessSchoolApplication> applications = businessSchoolApplicationDao.loadDealApplicationsForNotice(dealTime);
        // 过滤已经过期的申请,dealtime+24小时内不过期
        applications = applications.stream().filter(application ->
                DateUtils.afterDays(application.getDealTime(), 1).after(new Date()))
                .collect(Collectors.toList());

        List<Integer> applyProfileIds = applications.stream().map(BusinessSchoolApplication::getProfileId)
                .collect(Collectors.toList());

        //判断是否已经付费过（2018-04-10）
        List<RiseMember> customerRiseMembers = riseMemberDao.loadValidRiseMemberByProfileIds(applyProfileIds);
        Map<Integer, List<RiseMember>> existRiseMemberMap = customerRiseMembers.stream()
                .collect(Collectors.groupingBy(RiseMember::getProfileId));

        applications.forEach((businessSchoolApplication) -> {
            try {
                Integer profileId = businessSchoolApplication.getProfileId();
                //添加判断（2018-04-10）
                List<RiseMember> riseMembers = existRiseMemberMap.get(profileId);
                List<Integer> riseMemberIds = Lists.newArrayList();
                if (riseMembers != null) {
                    riseMemberIds = riseMembers.stream().map(RiseMember::getMemberTypeId).collect(Collectors.toList());
                }
                //排除已经付费的用户
                int memberTypeId = businessSchoolApplication.getMemberTypeId();
                MemberType existMemberType =memberTypes.stream().filter(memberType -> memberType.getId() == memberTypeId).findAny().orElse(null);
                if(CollectionUtils.isEmpty(riseMembers)||!riseMemberIds.contains(memberTypeId)){
                    sendExpiredMessage(distanceDay,businessSchoolApplication,profileId,existMemberType);
                }
            } catch (Exception e) {
                logger.error("发送过期通知失败", e);
            }
        });

    }

    private void sendExpiredMessage(Integer distanceDay, BusinessSchoolApplication businessSchoolApplication, Integer profileId, MemberType memberType) {
        // 只查看未过期的奖学金
        List<Coupon> coupons = couponDao.loadCouponsByProfileId(profileId,
                RISE_APPLY_COUPON_CATEGORY, RISE_APPLY_COUPON_DESCRIPTION);

        Profile profile = profileDao.load(Profile.class, profileId);

        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(profile.getOpenid());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(PAY_URL + memberType.getId());

        if (CollectionUtils.isNotEmpty(coupons)) {
            Coupon coupon = coupons.get(0);
            // 设置消息 message id
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());
            String expiredHourStr = DateUtils.parseDateToString6(
                    DateUtils.afterDays(businessSchoolApplication.getDealTime(), 1));
            String expiredDateStr = DateUtils.parseDateToString(
                    DateUtils.afterDays(businessSchoolApplication.getDealTime(), 1));
            // 有优惠券模板消息内容
            String first = "Hi " + profile.getNickname() + "，您的圈外商学院" + memberType.getDescription() + "录取资格及奖学金即将到期，请尽快办理入学！\n";
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));

            data.put("keyword1", new TemplateMessage.Keyword("今天" + expiredHourStr + "（" + expiredDateStr + "）到期", "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword(memberType.getDescription() + "入学奖学金", "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword(coupon.getAmount() + "元", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("\n点此卡片，立即办理入学", "#f57f16"));

            // 有优惠券短信内容
            SMSDto smsDto = new SMSDto();
            if (profile.getMobileNo() != null) {
                smsDto.setProfileId(profileId);
                smsDto.setPhone(profile.getMobileNo());
                smsDto.setType(SMSDto.PROMOTION);
                String content = "Hi，你申请的圈外商学院" + memberType.getDescription() + "入学奖学金即将到期，请至「圈外同学」公众号，办理入学并使用吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
                smsDto.setContent(content);
                shortMessageService.sendShortMessage(smsDto);
            }

        } else {
            String expiredDateStr = DateUtils.parseDateToString5(
                    DateUtils.afterDays(businessSchoolApplication.getDealTime(), 1));
            // 设置消息 message id
            templateMessage.setTemplate_id(ConfigUtils.getApplySuccessMsg());
            // 无优惠券模板消息内容
            String first = "我们很荣幸地通知您被圈外商学院" + memberType.getDescription() + "录取，录取有效期24小时，请尽快办理入学，及时开始学习并结识优秀的校友吧！\n";
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
                String content = "Hi，你申请的圈外商学院" + memberType.getDescription() + "入学资格即将到期，请至「圈外同学」公众号，办理入学并使用吧！如有疑问请联系圈外小Y(微信号：quanwai666) 回复TD退订";
                smsDto.setContent(content);
                shortMessageService.sendShortMessage(smsDto);
            }
        }
        templateMessageService.sendMessage(templateMessage);
    }

    /**
     * 发送奖学金消息
     *
     * @param coupons
     * @param noCouponGroup
     * @param mbaCoupons
     * @param mbaNoCouponGroup
     */
    private void sendCouponMsgs(Map<Double, List<BusinessSchoolApplication>> coupons, List<BusinessSchoolApplication> noCouponGroup, Map<Double, List<BusinessSchoolApplication>> mbaCoupons, List<BusinessSchoolApplication> mbaNoCouponGroup, List<MemberType> memberTypes) {
        MemberType eliteMemberType = memberTypes.stream().filter(memberType -> memberType.getId() == RiseMember.ELITE).findAny().orElse(null);
        MemberType mbaMemberType = memberTypes.stream().filter(memberType -> memberType.getId() == RiseMember.BUSINESS_THOUGHT).findAny().orElse(null);

        // 同样的对象不需要定义两次
        //发送商学院奖学金消息
        coupons.forEach((amount, applicationGroup) -> {
            // 发送有优惠券的
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setUrl(PAY_URL + ELITE_GOODS);

            if (eliteMemberType != null) {
                templateMessage.setComment(eliteMemberType.getDescription() + "审核通过");
            }
            data.put("keyword1", new TemplateMessage.Keyword("通过"));
            data.put("remark", new TemplateMessage.Keyword("\n奖学金和录取通知24小时内有效，请及时点击本通知书，办理入学。", "#f57f16"));
            if (eliteMemberType != null) {
                data.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院" + eliteMemberType.getDescription() + "】录取！" +
                        "\n\n根据你的申请，入学委员会决定发放给你" + amount.intValue()
                        + "元奖学金，付款时自动抵扣学费。希望你在圈外商学院" + eliteMemberType.getDescription() + "内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
            }
            applicationGroup.forEach(app -> this.sendMsg(templateMessage, data, app, "keyword2", memberTypes));
        });

        // 发送没有优惠券的
        if (noCouponGroup != null) {
            noCouponGroup.forEach(app -> {
                // 发送没有优惠券的模版
                TemplateMessage noCouponMsg = new TemplateMessage();
                noCouponMsg.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
                noCouponMsg.setUrl(PAY_URL + ELITE_GOODS);
                if (eliteMemberType != null) {
                    noCouponMsg.setComment(eliteMemberType.getDescription() + "审核通过,无优惠券");
                }
                Map<String, TemplateMessage.Keyword> noCouponData = Maps.newHashMap();
                noCouponMsg.setData(noCouponData);
                if (eliteMemberType != null) {
                    noCouponData.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院" + eliteMemberType.getDescription() + "】录取！希望你在圈外商学院" + eliteMemberType.getDescription() + "内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
                }
                noCouponData.put("keyword1", new TemplateMessage.Keyword("通过"));
                noCouponData.put("remark", new TemplateMessage.Keyword("\n本录取通知24小时内有效，过期后需重新申请。请及时点击本通知书，办理入学。", "#f57f16"));
                this.sendMsg(noCouponMsg, noCouponData, app, "keyword2", memberTypes);
            });
        }

        //发送商业进阶项目优惠券消息
        mbaCoupons.forEach((amount, applicationGroup) -> {
            // 发送有优惠券的
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setUrl(PAY_URL + THOUGHT_GOODS);
            if (mbaMemberType != null) {
                templateMessage.setComment(mbaMemberType.getDescription() + "审核通过");
            }
            data.put("keyword1", new TemplateMessage.Keyword("通过"));
            data.put("remark", new TemplateMessage.Keyword("\n奖学金和录取通知24小时内有效，请及时点击本通知书，办理入学。", "#f57f16"));
            data.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院" + mbaMemberType.getDescription() + "】录取！" +
                    "\n\n根据你的申请，入学委员会决定发放给你" + amount.intValue()
                    + "元奖学金，付款时自动抵扣学费。希望你在圈外商学院" + mbaMemberType.getDescription() + "内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
            applicationGroup.forEach(app -> this.sendMsg(templateMessage, data, app, "keyword2", memberTypes));
        });

        // 发送没有优惠券的
        if (mbaNoCouponGroup != null) {
            mbaNoCouponGroup.forEach(app -> {
                // 发送没有优惠券的模版
                TemplateMessage noCouponMsg = new TemplateMessage();
                noCouponMsg.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
                noCouponMsg.setUrl(PAY_URL + THOUGHT_GOODS);
                if (mbaMemberType != null) {
                    noCouponMsg.setComment(mbaMemberType.getDescription() + "审核通过,无优惠券");
                }
                Map<String, TemplateMessage.Keyword> noCouponData = Maps.newHashMap();
                noCouponMsg.setData(noCouponData);
                if (mbaMemberType != null) {
                    noCouponData.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院" + mbaMemberType.getDescription() + "】录取！希望你在圈外商学院" + mbaMemberType.getDescription() + "内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
                }
                noCouponData.put("keyword1", new TemplateMessage.Keyword("通过"));
                noCouponData.put("remark", new TemplateMessage.Keyword("\n本录取通知24小时内有效，过期后需重新申请。请及时点击本通知书，办理入学。", "#f57f16"));
                this.sendMsg(noCouponMsg, noCouponData, app, "keyword2", memberTypes);
            });
        }

    }

    /**
     * 判断是否已经购买
     *
     * @param profileId
     * @return
     */
    private boolean checkIsBuy(Integer profileId, Integer memberTypeId) {
        List<RiseMember> riseMembers = riseMemberDao.loadAllValidRiseMembers(profileId);
        List<Integer> riseMemberIds = riseMembers.stream().map(RiseMember::getMemberTypeId).collect(Collectors.toList());

        if (riseMemberIds.contains(memberTypeId)) {
            return true;
        }
        return false;
    }

    public void expiredApply() {
        businessSchoolApplicationDao.loadNoExpiredDealApplication()
                .stream()
                .filter(item -> item.getDealTime() != null)
                .filter(item -> DateUtils.intervalMinute(DateUtils.afterHours(item.getDealTime(), 24)) <= 0)
                .forEach(item -> businessSchoolApplicationDao.expiredApply(item.getId()));
    }
}

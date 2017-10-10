package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.BusinessSchoolApplicationDao;
import com.iquanwai.domain.dao.CustomerMessageLogDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.SurveySubmitDao;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.BusinessSchoolApplication;
import com.iquanwai.domain.po.CustomerMessageLog;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.SurveySubmit;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
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
    public static String PAY_URL = "https://www.iquanwai.com/pay/rise";

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
    private CustomerMessageLogDao customerMessageLogDao;

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
                // 判断status
                boolean findOld = otherBatch.stream().anyMatch(item -> item.getStatus() != BusinessSchoolApplication.APPLYING);
                Integer status;
                if (!findOld) {
                    // 之前没有处理过,一个月之内,并且当前不是精英版
                    if (riseMember != null && (riseMember.getMemberTypeId() == RiseMember.ELITE || riseMember.getMemberTypeId() == RiseMember.HALF_ELITE)) {
                        logger.info("精英版自动关闭:{}", profile.getId());
                        status = BusinessSchoolApplication.AUTO_CLOSE;
                    } else {
                        status = BusinessSchoolApplication.APPLYING;
                    }
                    if (CollectionUtils.isNotEmpty(otherBatch)) {
                        // 之前没处理过，并且有老的申请，将老的申请关掉
                        otherBatch.forEach(item -> {
                            logger.info("关掉老的申请:{}", profile.getId());
                            businessSchoolApplicationDao.autoCloseApplication(item.getId());
                        });
                    }
                } else {
                    logger.info("已经处理过:{}", profile.getId());
                    status = BusinessSchoolApplication.AUTO_CLOSE;
                }
                application.setStatus(status);
                // 常规数据初始化
                application.setSubmitId(survey.getId());
                application.setProfileId(profile.getId());
                application.setOpenid(profile.getOpenid());
                application.setCheckTime(status != BusinessSchoolApplication.APPLYING ? new Date() : null);
                application.setDeal(status != BusinessSchoolApplication.APPLYING);
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
        Map<Integer, List<BusinessSchoolApplication>> waitNoticeMap = applications.stream().collect(Collectors.groupingBy(BusinessSchoolApplication::getStatus));
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
        templateMessage.setUrl(PAY_URL);
        data.put("keyword1", new TemplateMessage.Keyword("【圈外商学院】"));
        data.put("keyword2", new TemplateMessage.Keyword("未通过"));

        data.put("remark", new TemplateMessage.Keyword("本期商学院的申请者都异常优秀，我们无法为每位申请者提供学习机会，但是很高兴你有一颗追求卓越的心！点击下方“详情”，了解预科班--圈外训练营。"));
        // 同样的对象不需要定义两次
        CustomerMessageLog log = new CustomerMessageLog();
        log.setComment("发送拒信");
        log.setPublishTime(new Date());
        data.put("first", new TemplateMessage.Keyword("在认真审核过你的入学申请后，我们很遗憾地通知你不能加入我们的【圈外同学商学院】。" +
                "\n在此之前，我们推荐你先加入我们的【训练营】进行学习。训练营能够帮你快速集中地提高专项能力，为你下次申请商学院提高录取通率。\n点击下方“详情”即可了解训练营。\n"));
        applications.forEach(app -> this.sendMsg(templateMessage, data, log, app, "keyword3"));
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
        coupons.forEach((amount, group) -> {
            logger.info("{}元优惠券,{}条", amount, group.size());
        });
        logger.info("无优惠券,{}条", noCouponGroup == null ? 0 : noCouponGroup.size());
        // 发送有优惠券的
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(PAY_URL);
        data.put("keyword1", new TemplateMessage.Keyword("通过"));
        data.put("remark", new TemplateMessage.Keyword("报名方式：点击下方“详情”\n\n在未来的日子里，希望你在商学院内取得傲人的成绩，和顶尖的校友们一同前进！"));
        // 同样的对象不需要定义两次
        CustomerMessageLog log = new CustomerMessageLog();
        log.setComment("商学院审核通过");
        log.setPublishTime(new Date());
        coupons.forEach((amount, applicationGroup) -> {
            data.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！" +
                    "\n根据你的申请，圈外入学委员会决定为你提供" + amount.intValue() + "元的奖学金。奖学金已放入你的商学院个人帐户，付款操作时可使用奖学金抵扣。" +
                    "\n点击本通知书下方的“详情”即可办理入学。\n));"));
            applicationGroup.forEach(app -> this.sendMsg(templateMessage, data, log, app, "keyword2"));
        });


        // 发送没有优惠券的模版
        TemplateMessage noCouponMsg = new TemplateMessage();
        noCouponMsg.setTemplate_id(ConfigUtils.getUnderCloseMsg());
        noCouponMsg.setUrl(PAY_URL);
        Map<String, TemplateMessage.Keyword> noCouponData = Maps.newHashMap();
        noCouponMsg.setData(data);
        noCouponData.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！\n本期商学院的申请者都异常优秀，能够占有一席是很值得自豪的。\n点击本通知书下方的“详情”即可办理入学。\n"));
        noCouponData.put("keyword1", new TemplateMessage.Keyword("通过"));
        noCouponData.put("keyword3", new TemplateMessage.Keyword("点击下方“详情”"));
        noCouponData.put("remark", new TemplateMessage.Keyword("\n在未来的日子里，希望你在商学院内取得傲人的成绩，和顶尖的校友们一同前进！"));
        CustomerMessageLog noCouponLog = new CustomerMessageLog();
        noCouponLog.setComment("商学院审核通过");
        noCouponLog.setPublishTime(new Date());
        // 发送没有优惠券的
        if (noCouponGroup != null) {
            noCouponGroup.forEach(app -> this.sendMsg(noCouponMsg, noCouponData, noCouponLog, app, "keyword2"));
        }
    }


    public void sendMsg(TemplateMessage templateMessage, Map<String, TemplateMessage.Keyword> data, CustomerMessageLog log, BusinessSchoolApplication application, String checkKey) {
        templateMessage.setTouser(application.getOpenid());
        data.put(checkKey, new TemplateMessage.Keyword(DateUtils.parseDateToString(application.getCheckTime())));
        logger.info("发送模版消息id ：{}", templateMessage.getTemplate_id());
        templateMessageService.sendMessage(templateMessage);
        log.setOpenid(application.getOpenid());
        customerMessageLogDao.insert(log);
    }


}

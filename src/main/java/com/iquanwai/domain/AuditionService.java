package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 试听课相关
 */
@Service
public class AuditionService {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;
    @Autowired
    private AuditionRewardDao auditionRewardDao;
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String RISE_PAY_PAGE = "/pay/rise";

    private void preUpdateProfileId() {
        List<AuditionClassMember> auditionClassMembers = auditionClassMemberDao.loadNullProfileId();
        auditionClassMembers.forEach(auditionClassMember -> {
            String openId = auditionClassMember.getOpenId();
            if (openId != null) {
                Profile profile = customerService.getProfile(openId);
                if (profile != null) {
                    auditionClassMemberDao.updateProfileId(auditionClassMember.getId(), profile.getId());
                }
            }
        });
    }

    public void sendAuditionCompleteReward() {
        preUpdateProfileId();

        // 获取当前时间所在周的周一的前一天
        Date startDate = DateUtils.beforeDays(DateUtils.getMonday(new Date()), 1);
        List<AuditionClassMember> auditionClassMembers = auditionClassMemberDao.loadByStartDate(DateUtils.parseDateToString(startDate));
        // 过滤出还没有校验过的人员
        auditionClassMembers = auditionClassMembers.stream().filter(auditionClassMember -> !auditionClassMember.getChecked()).collect(Collectors.toList());

        // 获取过滤出人员的 ProfileId 的集合
        List<Integer> auditionProfileIds = auditionClassMembers.stream().map(AuditionClassMember::getProfileId).collect(Collectors.toList());

        // 获取过滤出人员的 Id 集合
        List<Integer> auditionIds = auditionClassMembers.stream().map(AuditionClassMember::getId).collect(Collectors.toList());
        // 获取所有人员 Id 的 AuditionIdentity 身份信息
        List<AuditionReward> auditionIdentities = auditionRewardDao.loadByAuditionIds(auditionIds);
        // 根据 AuditionId 分组
        Map<Integer, List<AuditionReward>> auditionMap = auditionIdentities.stream().collect(Collectors.groupingBy(AuditionReward::getAuditionId));

        // 根据 ProfileId 集合，获取 ImprovementPlan 集合（ProblemId 是 试听课 Id）
        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadPlansByProfileIds(auditionProfileIds, ConfigUtils.getTrialProblemId());
        // ProfileId => ImprovementPlan
        Map<Integer, ImprovementPlan> improvementPlanMap = improvementPlans.stream().collect(Collectors.toMap(ImprovementPlan::getProfileId, improvementPlan -> improvementPlan));

        auditionClassMembers.forEach(classMember -> {
            logger.info("正在处理：" + classMember.getId());

            // 处理到某条记录，将该条记录 checked 状态改为 1
            Integer profileId = classMember.getProfileId();
            auditionClassMemberDao.updateChecked(classMember.getId(), true);

            List<AuditionReward> personalAuditionRewards = auditionMap.get(classMember.getId());
            if (personalAuditionRewards == null) {
                personalAuditionRewards = Lists.newArrayList();
            }

            // 如果某个人是优胜团队中，发送优秀奖
            boolean isWinningGroup = personalAuditionRewards.stream().filter(identity -> AuditionReward.Identity.WINNINGGROUP == identity.getIdentity()).count() > 0;
            if (isWinningGroup) {
                sendWinningGroupMessage(profileId);
            }

            ImprovementPlan improvementPlan = improvementPlanMap.get(profileId);
            if (improvementPlan != null) {
                boolean sendAuditionReward = checkPracticeCompleteStatus(improvementPlan.getId());
                if (sendAuditionReward) {
                    boolean isCommittee = personalAuditionRewards.stream().filter(identity -> AuditionReward.Identity.COMMITTEE == identity.getIdentity()).count() > 0;
                    if (isCommittee) {
                        sendCommitteeMessage(profileId);
                    } else {
                        sendNormalMessage(profileId);
                    }
                }
            }
        });
    }

    // 发送优秀团队奖学金
    private void sendWinningGroupMessage(Integer profileId) {
        Profile profile = customerService.getProfile(profileId);

        Coupon coupon = new Coupon();
        coupon.setProfileId(profileId);
        coupon.setAmount(100);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterDays(new Date(), 3));
        coupon.setCategory(Coupon.Category.ELITE_RISE_MEMBER);
        coupon.setDescription("试听课奖学金");
        int result = couponDao.insert(coupon);

        if (result > 0) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_PAGE);

            // 设置消息 message id
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());

            String first = "嗨，恭喜你的小组在PK中获胜，成为圈外商学院试听课优秀团队。作为小组得力干将，你已获得￥100元商学院奖学金，希望你在商学院成长过程中再接再厉，更优秀！\n";
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()), "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword("优秀团队", "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword("100\n有效期：48小时\n", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("点击详情，立即领取奖学金", "#f57f16"));
            templateMessageService.sendMessage(templateMessage, false);
        }
    }

    // 发送优秀学员奖学金
    private void sendNormalMessage(Integer profileId) {
        Profile profile = customerService.getProfile(profileId);

        Coupon coupon = new Coupon();
        coupon.setProfileId(profileId);
        coupon.setAmount(200);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterDays(new Date(), 3));
        coupon.setCategory(Coupon.Category.ELITE_RISE_MEMBER);
        coupon.setDescription("试听课奖学金");
        int result = couponDao.insert(coupon);

        if (result > 0) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_PAGE);

            // 设置消息 message id
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());

            String first = "嗨，恭喜你通过努力完成圈外商学院试听课程学习，成为试听课优秀学员，额外获得￥200元商学院奖学金\n";
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()), "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword("优秀学员", "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword("200\n有效期：48小时\n\n如本周内申请商学院被录取并获得入学奖学金，可以叠加使用哦~\n", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("点击详情，立即领取奖学金", "#f57f16"));
            templateMessageService.sendMessage(templateMessage, false);
        }
    }

    // 发送优秀学委奖学金
    private void sendCommitteeMessage(Integer profileId) {
        Profile profile = customerService.getProfile(profileId);

        Coupon coupon = new Coupon();
        coupon.setProfileId(profileId);
        coupon.setAmount(200);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.afterDays(new Date(), 3));
        coupon.setCategory(Coupon.Category.ELITE_RISE_MEMBER);
        coupon.setDescription("试听课奖学金");
        int result = couponDao.insert(coupon);

        if (result > 0) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            templateMessage.setUrl(ConfigUtils.getAppDomain() + RISE_PAY_PAGE);

            // 设置消息 message id
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());

            String first = "嗨，恭喜你在圈外商学院试听课程中认真负责努力学习，成为试听课优秀学委，额外获得￥200元商学院奖学金\n";
            data.put("first", new TemplateMessage.Keyword(first, "#000000"));
            data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date()), "#000000"));
            data.put("keyword2", new TemplateMessage.Keyword("优秀学委", "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword("200\n有效期：48小时\n\n如本周内申请商学院被录取并获得入学奖学金，可以叠加使用哦~\n", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("点击详情，立即领取奖学金", "#f57f16"));
            templateMessageService.sendMessage(templateMessage, false);
        }
    }

    /**
     * 查看某个学习计划是否完成<br/>
     * 1. 完成所有的知识点和选择题<br/>
     * 2. 每节应用题至少完成一道
     */
    private boolean checkPracticeCompleteStatus(Integer planId) {
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        boolean completeStatus = true;

        if (practicePlans.size() != 0) {
            // 应该完成的知识点、选择题中未完成的题数
            Long unCompleteNecessaryCountLong = practicePlans.stream()
                    .filter(practicePlan ->
                            PracticePlan.WARM_UP == practicePlan.getType() || PracticePlan.WARM_UP_REVIEW == practicePlan.getType()
                                    || PracticePlan.KNOWLEDGE == practicePlan.getType() || PracticePlan.KNOWLEDGE_REVIEW == practicePlan.getType())
                    .filter(practicePlan -> practicePlan.getStatus() == 0)
                    .count();
            if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                // 必须完成知识点、选择题的题数大于 0，不发奖学金
                completeStatus = false;
            } else {
                // 所有必须完成的知识点、选择题都已经完成
                // 对应用题完成情况进行复查
                List<PracticePlan> applicationPracticePlans = practicePlans.stream()
                        .filter(practicePlan -> PracticePlan.APPLICATION == practicePlan.getType() || PracticePlan.APPLICATION_REVIEW == practicePlan.getType())
                        .collect(Collectors.toList());
                List<Integer> applicationIds = applicationPracticePlans.stream().map(PracticePlan::getPracticeId).map(Integer::parseInt).collect(Collectors.toList());
                List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadApplicationSubmitsByApplicationIds(applicationIds, planId);
                Map<Integer, ApplicationSubmit> applicationSubmitMap = applicationSubmits.stream().collect(Collectors.toMap(ApplicationSubmit::getApplicationId, applicationSubmit -> applicationSubmit));

                // 根据 planId 和 practicePlan 中的 PracticeId 来获取应用题完成数据
                Set<Integer> seriesSet = applicationPracticePlans.stream().map(PracticePlan::getSeries).collect(Collectors.toSet());

                // Plan 中每节至少优质完成一道应用题的小节数
                Long planApplicationCheckLong = seriesSet.stream().filter(series -> {
                    List<Integer> practiceIds = applicationPracticePlans.stream()
                            .filter(practicePlan -> practicePlan.getSeries().equals(series))
                            .map(PracticePlan::getPracticeId)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    // 每个 Series 中至少存在一节内容完成，无内容长度限制
                    Long seriesApplicationCheckLong = practiceIds.stream().filter(practiceId -> {
                        ApplicationSubmit applicationSubmit = applicationSubmitMap.get(practiceId);
                        return applicationSubmit != null;
                    }).count();
                    return seriesApplicationCheckLong.intValue() > 0;
                }).count();

                if (planApplicationCheckLong.intValue() != seriesSet.size()) {
                    completeStatus = false;
                }
            }
        } else {
            completeStatus = false;
        }
        return completeStatus;
    }

}

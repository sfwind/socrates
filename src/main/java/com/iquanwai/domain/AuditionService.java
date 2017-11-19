package com.iquanwai.domain;

import com.iquanwai.domain.dao.ApplicationSubmitDao;
import com.iquanwai.domain.dao.AuditionClassMemberDao;
import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.PracticePlanDao;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.DateUtils;
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
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;


    private static final int AUDITION_PROBLEM_ID = 9;

    public void sendAuditionCompleteReward() {
        // TODO 确认金额
        List<AuditionClassMember> auditionClassMembers = auditionClassMemberDao.loadByStartDate(DateUtils.getMonday(new Date()));

        List<Integer> auditionProfileIds = auditionClassMembers.stream().map(AuditionClassMember::getProfileId).collect(Collectors.toList());

        List<ImprovementPlan> improvementPlans = improvementPlanDao.loadPlansByProfileIds(auditionProfileIds, AUDITION_PROBLEM_ID);
        Map<Integer, ImprovementPlan> improvementPlanMap = improvementPlans.stream().collect(Collectors.toMap(ImprovementPlan::getId, improvementPlan -> improvementPlan));
        List<Integer> riseClassMemberPlanIds = improvementPlans.stream().map(ImprovementPlan::getId).collect(Collectors.toList());

        riseClassMemberPlanIds.forEach(planId -> {
            boolean sendAuditionReward = true;
            List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);

            if (practicePlans.size() != 0) {
                // 应该完成的知识点、选择题中未完成的题数
                Long unCompleteNecessaryCountLong = practicePlans.stream()
                        .filter(practicePlan ->
                                PracticePlan.WARM_UP == practicePlan.getType() || PracticePlan.WARM_UP_REVIEW == practicePlan.getType()
                                        || PracticePlan.KNOWLEDGE == practicePlan.getType() || PracticePlan.KNOWLEDGE_REVIEW == practicePlan.getType())
                        .filter(practicePlan ->
                                practicePlan.getStatus() == 0)
                        .count();
                if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                    // 必须完成知识点、选择题的题数大于 0，不发结课证书
                    sendAuditionReward = false;
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
                        // 每个 Series 中至少存在一节内容完成
                        Long seriesApplicationCheckLong = practiceIds.stream().filter(practiceId -> {
                            ApplicationSubmit applicationSubmit = applicationSubmitMap.get(practiceId);
                            return applicationSubmit != null && (applicationSubmit.getContent().contains("img") || applicationSubmit.getLength() > 10);
                        }).count();
                        return seriesApplicationCheckLong.intValue() > 0;
                    }).count();

                    if (planApplicationCheckLong.intValue() != seriesSet.size()) {
                        sendAuditionReward = false;
                    }
                }

                if (sendAuditionReward) {
                    ImprovementPlan improvementPlan = improvementPlanMap.get(planId);
                    Coupon coupon = new Coupon();
                    coupon.setOpenid(improvementPlan.getOpenid());
                    coupon.setProfileId(improvementPlan.getProfileId());
                    // TODO 确定金额
                    coupon.setAmount(200.00);
                    coupon.setUsed(0);
                    coupon.setExpiredDate(DateUtils.afterDays(new Date(), 7));
                    coupon.setDescription("试听课奖学金");
                }
            }
        });


    }

}

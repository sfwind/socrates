package com.iquanwai.domain;

import com.iquanwai.domain.dao.BusinessSchoolApplicationDao;
import com.iquanwai.domain.dao.ProfileDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.SurveySubmitDao;
import com.iquanwai.domain.po.BusinessSchoolApplication;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.SurveySubmit;
import com.iquanwai.util.ConfigUtils;
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

    public void searchApplications(Date date) {
        List<SurveySubmit> surveySubmits = surveySubmitDao.loadSubmitGroup(BS_APPLICATION, date).stream().filter(item -> {
            // 过滤已经入库的申请
            return businessSchoolApplicationDao.loadBySubmitId(item.getId()) == null;
        }).collect(Collectors.toList());
        /*
          处理步骤：
          1.判断 status
          2.固化当前会员类型
          3.是否重复提交（老数据,这个批次）
         */
        Map<String, List<SurveySubmit>> surveyGroup = surveySubmits.stream().collect(Collectors.groupingBy(SurveySubmit::getOpenId));
        surveyGroup.forEach((openId, thisBatch) -> {
            Profile profile = profileDao.loadByOpenId(openId);
            List<BusinessSchoolApplication> otherBatch = businessSchoolApplicationDao.getUserApplications(profile.getId(), new Date());
            Integer minSubmitId = thisBatch.stream().mapToInt(SurveySubmit::getId).min().getAsInt();
            List<BusinessSchoolApplication> waitDeal = thisBatch.stream().map(survey -> {
                BusinessSchoolApplication application = new BusinessSchoolApplication();
                // 是否重复提交 !(老批次没有，并且这个批次是最小的id)
                application.setIsDuplicate(!(CollectionUtils.isEmpty(otherBatch) && survey.getId().equals(minSubmitId)));
                // 固化当前会员类型
                RiseMember riseMember = riseMemberDao.loadValidRiseMember(profile.getId());
                application.setOriginMemberType(riseMember != null ? riseMember.getMemberTypeId() : null);
                // 判断status
                Integer status = otherBatch.stream().anyMatch(item -> item.getStatus() != BusinessSchoolApplication.APPLYING) ? BusinessSchoolApplication.AUTO_CLOSE : BusinessSchoolApplication.APPLYING;
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

}

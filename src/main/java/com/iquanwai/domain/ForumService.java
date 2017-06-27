package com.iquanwai.domain;

import com.iquanwai.domain.dao.AnswerApprovalDao;
import com.iquanwai.domain.po.AnswerApproval;
import com.iquanwai.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/6/27.
 */
@Service
public class ForumService {
    @Autowired
    private AnswerApprovalDao answerApprovalDao;

    private static final int FOUR_HOUR = 4;

    public List<AnswerApproval> getAnswerApprovals(){
        Date end = DateUtils.startOfHour(new Date());
        Date start = DateUtils.beforeHours(end, FOUR_HOUR);
        //查询过去4小时的点赞
        return answerApprovalDao.getLastPeriodApprovals(start, end);
    }
}

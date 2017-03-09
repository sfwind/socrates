package com.iquanwai.domain;

import com.iquanwai.domain.dao.HomeworkVoteDao;
import com.iquanwai.domain.po.HomeworkVote;
import com.iquanwai.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/11.
 */
@Service
public class PracticeService {
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;

    public List<HomeworkVote> loadVoteYesterday(){
        return homeworkVoteDao.loadVoteByDate(DateUtils.beforeDays(new Date(), 1));
    }

}

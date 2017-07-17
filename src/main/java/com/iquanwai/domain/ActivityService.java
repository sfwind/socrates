package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.iquanwai.domain.dao.EventWallDao;
import com.iquanwai.domain.dao.ProblemDao;
import com.iquanwai.domain.po.EventWall;
import com.iquanwai.domain.po.Problem;
import com.iquanwai.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/7/4.
 */
@Service
public class ActivityService {
    @Autowired
    private EventWallDao eventWallDao;
    @Autowired
    private ProblemDao problemDao;

    public List<Problem> loadDiaoDaProblem() {
        List<EventWall> eventWallList = eventWallDao.loadDiaoDa(DateUtils.startDay(new Date()));

        List<Problem> problems =  eventWallList.stream().map(eventWall -> {
            Problem problem = problemDao.load(Problem.class, eventWall.getProblemId());
            problem.setActivityUrl(eventWall.getDestUrl());
            problem.setPassword(eventWall.getSubHead());
            return problem;
        }).collect(Collectors.toList());
        //小课去重
        List<Integer> problemIds = Lists.newArrayList();
        return problems.stream().filter(problem -> {
            if (problemIds.contains(problem.getId())) {
                return false;
            } else {
                problemIds.add(problem.getId());
                return true;
            }
        }).collect(Collectors.toList());
    }
}
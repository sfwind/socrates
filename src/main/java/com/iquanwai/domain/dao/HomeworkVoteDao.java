package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.HomeworkVote;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/2.
 */
@Repository
public class HomeworkVoteDao extends PracticeDBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<HomeworkVote> loadVoteByDate(Date date){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkVote>> h = new BeanListHandler(HomeworkVote.class);
        String sql = "select * from HomeworkVote where AddTime>=? and Del=0";
        try{
            return runner.query(sql, h, date);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }
    /**
     * 根据类型和依赖id查询被点赞的次数
     *
     * @param type         1:挑战任务,2：体系化大作业
     * @param referencedId 被依赖的id
     */
    public List<HomeworkVote> allVoteList(Integer type, Integer referencedId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkVote>> h = new BeanListHandler<HomeworkVote>(HomeworkVote.class);

        try {
            List<HomeworkVote> list = run.query("select * from HomeworkVote where Type=? and referencedId=?"
                    , h, type, referencedId);
            return list;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 根据类型和依赖id查询有效的被点赞的次数
     *
     * @param type         1:挑战任务,2：体系化大作业
     * @param referencedId 被依赖的id
     */
    public List<HomeworkVote> voteAbleList(Integer type, Integer referencedId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<HomeworkVote>> h = new BeanListHandler<HomeworkVote>(HomeworkVote.class);
        try {
            List<HomeworkVote> list = run.query("select * from HomeworkVote where Type=? and referencedId=? and Del=0"
                    , h, type, referencedId);
            return list;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }


    /**
     * 被点赞的次数
     */
    public int votedCount(Integer type, Integer referencedId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ScalarHandler<Long> h = new ScalarHandler<Long>();
        try {
            Long number = run.query("select count(1) from HomeworkVote where Type=? and referencedId=? and Del=0", h, type, referencedId);
            return number.intValue();
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

    /**
     * 进行点赞
     *
     * @return 插入结果
     */
    public void vote(HomeworkVote homeworkVote) {
        QueryRunner run = new QueryRunner(getDataSource());
        String insertSql = "INSERT INTO HomeworkVote(Type,ReferencedId,VoteOpenId,VotedOpenId,Device) " +
                "VALUES(?, ?, ?, ?, ?)";
        try {
            run.insert(insertSql, new ScalarHandler<>(), homeworkVote.getType(), homeworkVote.getReferencedId(),
                    homeworkVote.getVoteOpenId(), homeworkVote.getVotedOpenid(),
                    homeworkVote.getDevice());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * voteOpenid对某条记录的点赞记录
     *
     * @param type         1:挑战任务，2：体系化大作业
     * @param referencedId 被依赖的id
     * @param voteOpenId   点赞的人
     * @return
     */
    public HomeworkVote loadVoteRecord(Integer type, Integer referencedId, String voteOpenId) {
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<HomeworkVote> h = new BeanHandler(HomeworkVote.class);
        try {
            HomeworkVote vote = run.query("SELECT * FROM HomeworkVote where ReferencedId=? and VoteOpenId=? and Type=? ",
                    h, referencedId, voteOpenId, type);
            return vote;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * 重新点赞
     *
     * @param id 点赞的id
     */
    public void reVote(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "update HomeworkVote set Del=0 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * 取消点赞
     *
     * @param id 点赞的id
     */
    public void disVote(Integer id) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE HomeworkVote set Del=1 where Id=?";
        try {
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}

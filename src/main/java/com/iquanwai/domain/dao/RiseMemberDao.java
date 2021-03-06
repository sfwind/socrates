package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.RiseMember;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<RiseMember> loadAllValidRiseMembers(Integer profileId){
        QueryRunner runner = new QueryRunner(getDataSource());
        ResultSetHandler<List<RiseMember>> h  = new BeanListHandler<>(RiseMember.class);
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND Expired = 0 AND Del = 0";

        try {
           return runner.query(sql,h,profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public boolean riseMemberExpired(RiseMember riseMember) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update RiseMember set Expired = 1 where Id = ? AND Del = 0";

        try {
            runner.update(sql, riseMember.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    public RiseMember loadValidRiseMember(Integer profileId) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId = ? AND Expired = 0 AND Del = 0";
        ResultSetHandler<RiseMember> h = new BeanHandler<RiseMember>(RiseMember.class);
        try {
            return runner.query(sql, h, profileId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseMember> loadWillCloseMembers() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where Expired = 0 and ExpireDate <= CURRENT_TIMESTAMP AND Del = 0";
        ResultSetHandler<List<RiseMember>> handler = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> validRiseMember() {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where Expired = 0 AND Del = 0";

        try {
            ResultSetHandler<List<RiseMember>> handler = new BeanListHandler<>(RiseMember.class);
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    public List<RiseMember> loadRiseMembersByExpireDate(String expireDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ExpireDate = ? AND Expired = 0 AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, expireDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadValidRiseMemberByProfileIds(List<Integer> profileIds) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return Lists.newArrayList();
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Expired = 0 AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    /**
     * 更新过期日期
     * @param category :顺延类型（day,month,year）
     */
    public void updateExpiredDate(List<Integer> profileIds, Integer delay, String category) {
        if (CollectionUtils.isEmpty(profileIds)) {
            return;
        }
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update RiseMember set ExpireDate = DATE_ADD(ExpireDate,INTERVAL " + delay + " " + category + ") WHERE ProfileId in (" + produceQuestionMark(profileIds.size()) + ") AND Expired = 0 AND Del = 0";
        try {
            runner.update(sql, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public List<RiseMember> loadValidElite(String startTime, String endTime) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE MemberTypeId = 3 AND  Expired = 0 AND VIP =0 AND AddTime>=? AND AddTime<=? AND DEL = 0 ";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<>(RiseMember.class);
        try {
            return runner.query(sql, h, startTime, endTime);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> loadAllByProfileIds(List<Integer> profileIds) {
        if (profileIds.size() == 0) {
            return Lists.newArrayList();
        }

        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM RiseMember WHERE ProfileId IN (" + produceQuestionMark(profileIds.size()) + ") AND Del = 0";
        ResultSetHandler<List<RiseMember>> h = new BeanListHandler<RiseMember>(RiseMember.class);

        try {
            return runner.query(sql, h, profileIds.toArray());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }
}

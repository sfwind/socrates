package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.AuditionClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Repository
public class AuditionClassMemberDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public List<AuditionClassMember> loadByStartDate(Date startDate) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM AuditionClassMember WHERE StartDate = ?";
        ResultSetHandler<List<AuditionClassMember>> h = new BeanListHandler<>(AuditionClassMember.class);
        try {
            return runner.query(sql, h, startDate);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int updateChecked(Integer auditionClassMemberId, Boolean checked) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "UPDATE AuditionClassMember SET Checked = ? WHERE Id = ?";
        try {
            return runner.update(sql, checked, auditionClassMemberId);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }

}
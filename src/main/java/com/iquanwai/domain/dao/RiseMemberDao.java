package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.RiseMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/4/13.
 */
@Repository
public class RiseMemberDao extends DBUtil {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    public boolean riseMemberExpired(RiseMember riseMember){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update RiseMember set Expired = 1 where Id = ?";

        try{
            runner.update(sql,riseMember.getId());
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
            return false;
        }
        return true;
    }

    public List<RiseMember> loadWillCloseMembers(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where Expired = 0 and ExpireDate <= CURRENT_TIMESTAMP";
        ResultSetHandler<List<RiseMember>> handler = new BeanListHandler<>(RiseMember.class);
        try{
           return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public List<RiseMember> validRiseMember(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "select * from RiseMember where Expired = 0";

        try{
            ResultSetHandler<List<RiseMember>> handler = new BeanListHandler<>(RiseMember.class);
            return runner.query(sql, handler);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}

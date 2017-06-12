package com.iquanwai.domain.dao;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/2/8.
 */
@Repository
public class ProfileDao extends DBUtil{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public Boolean riseMemberExpired(Integer id){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update Profile set RiseMember = 0 where Id = ?";
        try{
            runner.update(sql, id);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }


}

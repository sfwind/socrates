package com.iquanwai.domain.dao;

import com.google.common.collect.Lists;
import com.iquanwai.domain.po.MaterialPrint;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public class MaterialPrintDao extends DBUtil{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<MaterialPrint> loadPostedPrint(){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "SELECT * FROM MaterialPrint Where Posted != 0 AND DEL = 0";
        ResultSetHandler<List<MaterialPrint>> h = new BeanListHandler<>(MaterialPrint.class);

        try {
            return runner.query(sql,h);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return Lists.newArrayList();
    }

    public void batchInsertPrint(List<MaterialPrint> materialPrints){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO MaterialPrint(ProfileId,Type,Posted,CheckBatch,SendBatch) Values(?,?,?,?,?)";

        Object[][] param = new Object[materialPrints.size()][];

        for(int i =0;i<materialPrints.size();i++){
            MaterialPrint materialPrint = materialPrints.get(i);
            param[i] = new Object[5];
            param[i][0] = materialPrint.getProfileId();
            param[i][1] = materialPrint.getType();
            param[i][2] = materialPrint.getPosted();
            param[i][3] = materialPrint.getCheckBatch();
            param[i][4] = materialPrint.getSendBatch();
        }
        try {
            runner.batch(sql,param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
    }

    /**
     * 临时方案check 模板消息人员是否正确
     * @param profileId
     * @param checkBatch
     */
    public Integer  updatePrint(Integer profileId,String checkBatch){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "Update MaterialPrint set posted = 10 Where ProfileId = ? And CheckBatch = ? And DEL = 0 ";
        try {
            return runner.update(sql,profileId,checkBatch);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(),e);
        }
        return -1;
    }

}

package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.iquanwai.domain.dao.EmployeeDao;
import com.iquanwai.domain.dao.MaterialPrintDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.dao.UserRoleDao;
import com.iquanwai.domain.po.MaterialPrint;
import com.iquanwai.domain.po.QuanwaiEmployee;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.domain.po.UserRole;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class GiftService {

    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private MaterialPrintDao materialPrintDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 生成寄送名单
     */
    public void generateList() {
        //商学院用户
        String endTime = DateUtils.parseDateToString(new Date()) + " 00:00:00";
        String startTime = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), 7)) + " 00:00:00";
        System.out.println("startTime:"+startTime);
        System.out.println("endTime:"+endTime);
        //获得需要寄送礼包的人的名单
        List<RiseMember> riseMemberList = riseMemberDao.loadValidElite(startTime, endTime);
        List<MaterialPrint> sendList = Lists.newArrayList();
        List<UserRole> userRoles = userRoleDao.loadValidAssists();
        List<QuanwaiEmployee> employees = employeeDao.loadEmployees();
        List<MaterialPrint> materialPrints = materialPrintDao.loadPostedPrint();
        //去除助教
        riseMemberList.stream().forEach(riseMember -> {
            Integer profileId = riseMember.getProfileId();

            //去除助教
           if(userRoles.stream().filter(userRole -> profileId.equals(userRole.getProfileId())).count()>0){
                return;
           }
            //去除员工
           if(employees.stream().filter(employee -> profileId.equals(employee.getProfileId())).count()>0){
               return;
           }
           if(materialPrints.stream().filter(materialPrint -> profileId.equals(materialPrint.getProfileId())).count()>0){
               return;
           }

           MaterialPrint postMaterialPrint = new MaterialPrint();
           postMaterialPrint.setProfileId(riseMember.getProfileId());
           postMaterialPrint.setType("quanquan_mail");
           postMaterialPrint.setPosted(0);
           postMaterialPrint.setCheckBatch(new Date());
           sendList.add(postMaterialPrint);

        });
        materialPrintDao.batchInsertPrint(sendList);
        //TODO:发送模板消息


    }
}

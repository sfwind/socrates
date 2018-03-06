package com.iquanwai.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.*;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.domain.po.*;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private TemplateMessageService templateMessageService;


    private String url = "https://www.iquanwai.com/rise/static/customer/profile?goRise=true";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 生成寄送名单
     */
    public void generateList() {
        //商学院用户
        String endTime = DateUtils.parseDateToString(new Date()) + " 00:00:00";
        String startTime = DateUtils.parseDateToString(DateUtils.beforeDays(new Date(), 7)) + " 00:00:00";
        System.out.println("startTime:" + startTime);
        System.out.println("endTime:" + endTime);
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
            if (userRoles.stream().filter(userRole -> profileId.equals(userRole.getProfileId())).count() > 0) {
                return;
            }
            //去除员工
            if (employees.stream().filter(employee -> profileId.equals(employee.getProfileId())).count() > 0) {
                return;
            }
            if (materialPrints.stream().filter(materialPrint -> profileId.equals(materialPrint.getProfileId())).count() > 0) {
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

        List<Integer> profiles = sendList.stream().map(MaterialPrint::getProfileId).collect(Collectors.toList());
        //统计需要发送模板消息的人
        List<Profile> sendProfiles = countSendList(profiles);
        System.out.println("sendList:"+sendProfiles.toString());

        //TODO:临时方案：修改Posted记录，手动check发送，未来发送模板消息
        sendProfiles.forEach(profile -> materialPrintDao.updatePrint(profile.getId(),DateUtils.parseDateToString(new Date())));

        //TODO:发送模板消息
       // sendNotify(sendProfiles);
    }


    private List<Profile> countSendList(List<Integer> profiles) {
        List<Profile> profileList = profileDao.loadByProfileIds(profiles);

        return profileList.stream().filter(profile -> (profile.getRealName() == null
                || profile.getReceiver() == null
                || profile.getMobileNo() == null
                || profile.getAddress() == null
        )).collect(Collectors.toList());
    }

    private void sendNotify(List<Profile> sendList){
         sendList.stream().forEach(profile -> {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            Map<String,TemplateMessage.Keyword> data = Maps.newHashMap();
             templateMessage.setData(data);
             templateMessage.setUrl(url);

             templateMessage.setTemplate_id(ConfigUtils.getInCompleteTask());
             String first = "Hi " +profile.getNickname() + "我们将于下周为你寄送商学院入学礼包，请点击下方详情，" +
                     "在本周日晚20：00之前确定你的收件信息无误，包括收件人姓名、地址和电话。\\n\\n" +
                     "礼包内含圈圈亲笔信，所以请填写真实姓名。如果你需要找人代收，" +
                     "请在代收人姓名后填写其电话。\\n\\n礼包送出后，我会通过圈外同学公众号通知你。\\n";

            data.put("first",new TemplateMessage.Keyword(first,"#000000"));
            data.put("keyword1", new TemplateMessage.Keyword("商学院入学礼包","#000000"));
            data.put("keyword2", new TemplateMessage.Keyword("完善收件信息", "#000000"));
            data.put("keyword3", new TemplateMessage.Keyword("本周日晚20：00\\n", "#000000"));
            data.put("remark", new TemplateMessage.Keyword("点击详情，完善收件人姓名、地址和电话", "##FFA500"));
            data.put("comment",new TemplateMessage.Keyword(new Date().toString()+"寄送礼包完善信息提醒"));
            templateMessageService.sendMessage(templateMessage);
         });
    }



}

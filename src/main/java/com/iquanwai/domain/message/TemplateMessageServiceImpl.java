package com.iquanwai.domain.message;

import com.google.gson.Gson;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.dao.AuditionClassMemberDao;
import com.iquanwai.domain.dao.CustomerMessageLogDao;
import com.iquanwai.domain.dao.RiseMemberDao;
import com.iquanwai.domain.log.OperationLogService;
import com.iquanwai.domain.po.AuditionClassMember;
import com.iquanwai.domain.po.CustomerMessageLog;
import com.iquanwai.domain.po.Profile;
import com.iquanwai.domain.po.RiseMember;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class TemplateMessageServiceImpl implements TemplateMessageService {

    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private CustomerMessageLogDao customerMessageLogDao;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AuditionClassMemberDao auditionClassMemberDao;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private RiseMemberDao riseMemberDao;

    @Override
    public boolean sendMessage(TemplateMessage templateMessage) {
        return sendMessage(templateMessage, true);
    }

    @Override
    public boolean sendMessage(TemplateMessage templateMessage, boolean validation) {
        addHook(templateMessage);
        boolean sendTag = true;
        if (validation) {
            // 发送权限校验
            boolean validPush = checkTemplateMessageAuthority(templateMessage, true);
            // 模板消息发送记录
            saveTemplateMessageSendLog(templateMessage, true, validPush);
            if (!validPush) {
                sendTag = false;
            }
        }
        String body = "";
        if (sendTag) {
            String json = new Gson().toJson(templateMessage);
            body = restfulHelper.post(SEND_MESSAGE_URL, json);
        }

        boolean success = StringUtils.isNoneEmpty(body);
        operationLogService.trace(() -> {
            Profile profile = customerService.getProfile(templateMessage.getTouser());
            return profile.getId();
        }, "sendWechatMessage", () -> OperationLogService.props().add("success", success));
        return success;
    }

    /**
     * 模板消息发送频率控制
     * 1. 此规则只适用于用户没有在产品中做任何操作时被动收到的模版消息
     * 2. 会员用户每周最多收到7条消息
     * 3. 非会员用户每周最多收到2条消息
     * 4. 手动发送的消息 同一个用户最多只能收到一次
     * 5. 用户每天最多收到2条消息
     * 6. 用户三小时内最多收到1条消息
     * 7. 活动提醒通知，文字尽量简洁，不要用推销的口吻
     *
     * @return 是否允许发送模板消息
     */
    private boolean checkTemplateMessageAuthority(TemplateMessage templateMessage, boolean forwardlyPush) {
        // forwardlyPush 为 true 指的是用户在没有任何操作时推送模板消息的情况
        String openId = templateMessage.getTouser();
        Profile profile = customerService.getProfile(openId);

        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profile.getId());

        // 如果不是主动推送或者发送对象是开发人员，不进行任何限制
        List<String> devOpenIds = ConfigUtils.getDevelopOpenIds();
        if (!forwardlyPush || devOpenIds.contains(profile.getOpenid())) {
            return true;
        }

        List<CustomerMessageLog> customerMessageLogs = customerMessageLogDao.loadByOpenId(openId);

        boolean authority;
        // 1. 会员用户每周最多收到 7 条消息
        if (riseMember != null || checkOtherAuthority(profile.getId())) {
            Date distanceDate = DateUtils.beforeDays(new Date(), 7);
            Long result = customerMessageLogs.stream().filter(messageLog -> messageLog.getPublishTime().compareTo(distanceDate) > 0).count();
            authority = result.intValue() < 7;
            if (!authority) {
                return false;
            }
        }

        // 2. 非会员用户每周最多收到 2 条消息
        if (riseMember == null) {
            Date distanceDate = DateUtils.beforeDays(new Date(), 7);
            Long result = customerMessageLogs.stream().filter(messageLog -> messageLog.getPublishTime().compareTo(distanceDate) > 0).count();
            authority = result.intValue() < 2;
            if (!authority) {
                return false;
            }
        }

        // 3. 手动发送内容一样的消息，同一个用户最多只能收到一次
        {
            Long result = customerMessageLogs.stream().filter(messageLog -> messageLog.getContentHash().equals(Integer.toString(templateMessage.getContent().hashCode()))).count();
            authority = result.intValue() < 1;
            if (!authority) {
                return false;
            }
        }

        // 4. 用户每天最多收到2条消息
        {
            Long result = customerMessageLogs.stream().filter(messageLog -> DateUtils.isToday(messageLog.getPublishTime())).count();
            authority = result.intValue() < 2;
            if (!authority) {
                return false;
            }
        }

        // 5. 用户三小时内最多收到1条消息
        {
            Date distanceTime = DateUtils.afterHours(new Date(), -3);
            Long result = customerMessageLogs.stream().filter(messageLog -> messageLog.getPublishTime().compareTo(distanceTime) > 0).count();
            authority = result.intValue() < 1;
            if (!authority) {
                return false;
            }
        }
        return true;
    }

    private void saveTemplateMessageSendLog(TemplateMessage templateMessage, boolean forwardlyPush, boolean validPush) {
        CustomerMessageLog customerMessageLog = new CustomerMessageLog();
        customerMessageLog.setOpenId(templateMessage.getTouser());
        customerMessageLog.setPublishTime(new Date());
        customerMessageLog.setContentHash(Integer.toString(templateMessage.getContent().hashCode()));
        customerMessageLog.setForwardlyPush(forwardlyPush ? 1 : 0);
        customerMessageLog.setValidPush(validPush ? 1 : 0);
        customerMessageLog.setComment(templateMessage.getComment());
        customerMessageLogDao.insert(customerMessageLog);
    }

    private boolean checkOtherAuthority(Integer profileId) {
        AuditionClassMember auditionClassMember = auditionClassMemberDao.loadByProfileId(profileId);
        return auditionClassMember != null;
    }

    private void addHook(TemplateMessage templateMessage) {
        if (templateMessage.getUrl() != null) {
            String url = templateMessage.getUrl();
            if (url.contains("_tm")) {
                return;
            }
            if (url.contains("?")) {
                url = url + "&_tm=template_message";
            } else {
                url = url + "?_tm=template_message";
            }
            templateMessage.setUrl(url);
        }
    }

}

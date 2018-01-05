package com.iquanwai.util;

import com.google.common.collect.Lists;
import com.iquanwai.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ConfigUtils {
    private static Config config;
    private static Config fileconfig;
    private static ZKConfigUtils zkConfigUtils;

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    static {
        loadLocalConfig();
        zkConfigUtils = new ZKConfigUtils();
    }

    private static void loadLocalConfig() {
        logger.info("load local config");
        config = ConfigFactory.load("localconfig");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
        config = fileconfig.withFallback(config);
    }

    public static String getValue(String key) {
        if (config.hasPath(key)) {
            return config.getString(key);
        } else {
            String value = zkConfigUtils.getValue(key);
            if (value == null) {
                value = zkConfigUtils.getArchValue(key);
            }
            return value;
        }
    }

    public static Integer getIntValue(String key) {
        if (config.hasPath(key)) {
            return config.getInt(key);
        } else {
            return zkConfigUtils.getIntValue(key);
        }
    }

    public static Boolean getBooleanValue(String key) {
        if (config.hasPath(key)) {
            return config.getBoolean(key);
        } else {
            return zkConfigUtils.getBooleanValue(key);
        }
    }

    public static String getJdbcUrl() {
        return getValue("db.url");
    }

    public static String getUsername() {
        return getValue("db.name");
    }

    public static String getPassword() {
        return getValue("db.password");
    }

    public static String getFragmentJdbcUrl() {
        return getValue("db.fragment.url");
    }

    public static String getForumJdbcUrl() {
        return getValue("db.forum.url");
    }

    public static String getAppid() {
        return getValue("appid");
    }

    public static String getSecret() {
        return getValue("secret");
    }

    public static String getUnderCloseMsg() {
        return getValue("will.close.task.msg");
    }

    public static String getLearningNotifyMsg() {
        return getValue("learning.notify.msg");
    }

    public static String getAppDomain() {
        return getValue("app.domain");
    }

    public static String getAPIKey() {
        return getValue("api.key");
    }

    public static String getMch_id() {
        return getValue("mch_id");
    }

    public static String getRabbitMQIp() {
        return getValue("rabbitmq.ip");
    }

    public static int getRabbitMQPort() {
        return getIntValue("rabbitmq.port");
    }

    public static String getRabbitMQUser() {
        return getValue("rabbitmq.user");
    }

    public static String getRabbitMQPasswd() {
        return getValue("rabbitmq.password");
    }

    /**
     * 会员到期提醒
     */
    public static String getRiseMemberExpireMsg() {
        return getValue("risemember.expire.msg");
    }

    public static String sendShortMessageUrl() {
        return getValue("send.sms.url");
    }

    /**
     * 申请成功通知
     */
    public static String getApplySuccessMsg() {
        return getValue("application.approve.msg");
    }

    /**
     * 账户变动提醒
     */
    public static String getAccountChangeMsg() {
        return getValue("account.change.message");
    }

    public static List<String> getDevelopOpenIds() {
        String openIdsStr = getValue("sms.alarm.openids");
        return Lists.newArrayList(openIdsStr.split(","));
    }

    public static String getRejectApplyMsgId() {
        return getValue("application.reject.msg");
    }

    public static String getApproveApplyMsgId() {
        return getValue("application.approve.msg");
    }

    public static Integer getTrialProblemId() {
        return getIntValue("rise.trial.problem.id");
    }

}

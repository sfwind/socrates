package com.iquanwai.util;

import com.iquanwai.util.zk.ZKConfigUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigUtils {
    private static Config config;
    private static Config localconfig;
    private static Config fileconfig;
    private static ZKConfigUtils zkConfigUtils;

    private static Timer timer;

    static {
        loadConfig();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadConfig();
            }
        }, 0, 1000 * 60);
        zkConfigUtils = new ZKConfigUtils();
    }


    private static void loadConfig() {
        config = ConfigFactory.load("localconfig");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
        config = fileconfig.withFallback(config);
    }

    public static String getValue(String key) {
        if(config.hasPath(key)) {
            return config.getString(key);
        } else {
            String value = zkConfigUtils.getValue(key);
            if(value == null) {
                value = zkConfigUtils.getArchValue(key);
            }
            return value;
        }
    }

    public static Integer getIntValue(String key) {
        if(config.hasPath(key)) {
            return config.getInt(key);
        } else {
            return zkConfigUtils.getIntValue(key);
        }
    }

    public static Boolean getBooleanValue(String key) {
        if(config.hasPath(key)) {
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

    public static String getFragmentUsername() {
        return getValue("db.fragment.name");
    }

    public static String getFragmentPassword() {
        return getValue("db.fragment.password");
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

    public static String getActivityStartMsg() {
        return getValue("activity.start.msg");
    }

	public static String getAppDomain() {
		return getValue("app.domain");
	}

}

package com.iquanwai.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigUtils {
	private static Config config;
	private static Config localconfig;
	private static Config fileconfig;

	private static Timer timer;
	static{
		loadConfig();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				loadConfig();
			}
		}, 0, 1000*60);
	}

	private static void loadConfig() {
		localconfig = ConfigFactory.load("localconfig");
		config = ConfigFactory.load("socrates");
		fileconfig = ConfigFactory.parseFile(new File("/data/config/localconfig"));
		config = localconfig.withFallback(config);
		config = fileconfig.withFallback(config);
	}

	public static String getJdbcUrl() {
		return config.getString("db.url");
	}

	public static String getUsername() {
		return config.getString("db.name");
	}

	public static String getPassword() {
		return config.getString("db.password");
	}


	public static String getFragmentJdbcUrl() {
		return config.getString("db.fragment.url");
	}

	public static String getFragmentUsername() {
		return config.getString("db.fragment.name");
	}

	public static String getFragmentPassword() {
		return config.getString("db.fragment.password");
	}

	public static String getAPIKey() {
		return config.getString("api.key");
	}

	public static String getAppid() {
		return config.getString("appid");
	}

	public static String getSecret() {
		return config.getString("secret");
	}

	public static String getUnderCloseMsg() {
		return config.getString("will.close.task.msg");
	}
}

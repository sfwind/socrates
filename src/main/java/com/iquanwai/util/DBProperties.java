package com.iquanwai.util;

/**
 * Created by justin on 17/7/29.
 */
public interface DBProperties {
    // 连接最大空闲时间
    int MAX_IDLE_TIME = 600;
    // 连接池中保留的最大连接数
    int MAX_POOL_SIZE = 15;
    // 连接池中保留的最小连接数
    int MIN_POOL_SIZE = 5;
    // 获取新连接的时间
    int CHECKOUT_TIMEOUT = 3000;
    // 数据源内加载的PreparedStatements数量
    int MAX_STATEMENTS = 1000;
    // 空闲连接测试周期
    int IDLE_CONNECTION_TEST_PERIOD = 60;

}

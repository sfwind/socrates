package com.iquanwai.util;

/**
 * Created by justin on 17/7/29.
 */
public interface DBProperties {

    //<!--连接池建立时创建的初始化连接数-->
    int INITIAL_SIZE = 5;
    // <!--连接池中最小的活跃连接数-->
    int MIN_IDLE = 5;
    // <!--连接池中最大的活跃连接数-->
    int MAX_ACTIVE = 15;
    // <!--配置获取连接等待超时时间-->
    long MAX_WAIT = 60000;
    // <!--配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒-->
    long TIME_BETWEEN_EVICTION_RUNS_MILLIS = 60000;
    // <!-- 配置一个连接在池中最小生存的时间，单位是毫秒 -->
    long MIN_EVICTABLE_IDLE_TIME_MILLIS = 300000;
    // <!--校验查询 sql-->
    String VALIDATION_QUERY = "SELECT 'x'";
    // <!--是否在连接空闲一段时间后检测其可用性-->
    boolean TEST_WHILE_IDLE = true;
    // <!--是否在获得连接后检测其可用性-->
    boolean TEST_ON_BORROW = true;
    // <!--是否在连接放回连接池后检测其可用性-->
    boolean TEST_ON_RETURN = false;
    // <!--查询超时时间-->
    int QUERY_TIMEOUT = 60000;
    // <!--事务查询超时时间-->
    int TRANSACTION_QUERY_TIMEOUT = 60000;
    // <!--登录超时时间	-->
    int LOGIN_TIMEOUNT = 60000;
    // <!--打开 PSCache，并且制定每个连接上 PSCache 的大小-->
    boolean POOL_PREPARED_STATEMENTS = false;

}

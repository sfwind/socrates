package com.iquanwai;

import com.iquanwai.util.ConfigUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {


    @Bean(name = "redissonClient")
    RedissonClient injectClient(){
        Config config = new Config();
        config.useSingleServer().setAddress(ConfigUtils.getValue("redis.single.address")).setPassword(ConfigUtils.getValue("redis.single.password"));
        return Redisson.create(config);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Object[] { Application.class }, args);
    }
}
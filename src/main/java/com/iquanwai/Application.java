package com.iquanwai;

import com.iquanwai.mq.RabbitMQConverter;
import com.iquanwai.util.ConfigUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    @Bean(name = "connectionFactory")
    public ConnectionFactory injectConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(ConfigUtils.getRabbitMQIp());
        factory.setPort(ConfigUtils.getRabbitMQPort());
        factory.setUsername(ConfigUtils.getRabbitMQUser());
        factory.setPassword(ConfigUtils.getRabbitMQPasswd());
        factory.start();
        return factory;
    }


    @Bean(name = "amqpTemplate")
    public AmqpTemplate injectRabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(injectConnectionFactory());
        template.setMessageConverter(injectConverter());
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);
        return template;
    }

    @Bean(name = "amqpAdmin")
    public AmqpAdmin injectAmqpAdmin() {
        return new RabbitAdmin(injectConnectionFactory());
    }

    @Bean(name = "mqConverter")
    public MessageConverter injectConverter(){
        return new RabbitMQConverter();
    }

    @Bean(name = "redissonClient")
    public RedissonClient injectClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(ConfigUtils.getValue("redis.single.address")).setPassword(ConfigUtils.getValue("redis.single.password"));
        return Redisson.create(config);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(new Object[]{Application.class}, args);
    }
}
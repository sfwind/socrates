package com.iquanwai.domain;

import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.CouponDao;
import com.iquanwai.domain.dao.QuanwaiOrderDao;
import com.iquanwai.domain.message.RestfulHelper;
import com.iquanwai.domain.po.Coupon;
import com.iquanwai.domain.po.PayClose;
import com.iquanwai.domain.po.PayCloseReply;
import com.iquanwai.domain.po.QuanwaiOrder;
import com.iquanwai.mq.RabbitMQPublisher;
import com.iquanwai.util.CommonUtils;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import com.iquanwai.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/7/20.
 */
@Service
public class PayService {
    @Autowired
    private RestfulHelper restfulHelper;
    @Autowired
    private QuanwaiOrderDao quanwaiOrderDao;
    @Autowired
    private CouponDao couponDao;

    private RabbitMQPublisher rabbitMQPublisher;

    private static final String TOPIC ="close_quanwai_order";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CLOSE_ORDER_URL ="https://api.mch.weixin.qq.com/pay/closeorder";

    private static final String ERROR_CODE = "FAIL";

    private static final String SUCCESS_CODE = "SUCCESS";

    @PostConstruct
    public void init(){
        rabbitMQPublisher = new RabbitMQPublisher();
        rabbitMQPublisher.init(PayService.TOPIC, ConfigUtils.getRabbitMQIp(),
                ConfigUtils.getRabbitMQPort());
    }

    public void closeOrder() {
        //点开付费的保留5分钟
        Date date = DateUtils.afterMinutes(new Date(), -5);
        List<QuanwaiOrder> underCloseOrders = quanwaiOrderDao.queryUnderCloseOrders(date);
        for (QuanwaiOrder courseOrder : underCloseOrders) {
            String orderId = courseOrder.getOrderId();
            try {
                if (courseOrder.getPrepayId() != null) {
                    PayClose payClose = buildPayClose(orderId);
                    String response = restfulHelper.postXML(CLOSE_ORDER_URL, XMLHelper.createXML(payClose));
                    PayCloseReply payCloseReply = XMLHelper.parseXml(PayCloseReply.class, response);
                    if (payCloseReply != null) {
                        if (SUCCESS_CODE.equals(payCloseReply.getReturn_code())) {
                            if (ERROR_CODE.equals(payCloseReply.getErr_code()) && payCloseReply.getErr_code_des() != null) {
                                logger.error(payCloseReply.getErr_code_des() + ", orderId=" + orderId);
                            }
                            logger.info("orderId: {} closed automatically", orderId);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("orderId: {} close failed", orderId);
            }

            //如果有使用优惠券,还原优惠券状态
            if (courseOrder.getDiscount() != 0.0) {
                couponDao.updateCouponByOrderId(Coupon.UNUSED, orderId);
            }
            //发mq消息
            try {
                rabbitMQPublisher.publish(orderId);
            } catch (ConnectException e) {
                logger.error("mq connection failed", e);
            }
        }
    }

    private PayClose buildPayClose(String orderId) {
        PayClose payClose = new PayClose();
        Map<String, String> map = Maps.newHashMap();
        map.put("out_trade_no", orderId);
        String appid = ConfigUtils.getAppid();
        map.put("appid", appid);
        String mch_id = ConfigUtils.getMch_id();
        map.put("mch_id", mch_id);
        String nonce_str = CommonUtils.randomString(16);
        map.put("nonce_str", nonce_str);
        String sign = CommonUtils.sign(map);

        payClose.setNonce_str(nonce_str);
        payClose.setMch_id(mch_id);
        payClose.setAppid(appid);
        payClose.setOut_trade_no(orderId);
        payClose.setSign(sign);

        return payClose;
    }
}

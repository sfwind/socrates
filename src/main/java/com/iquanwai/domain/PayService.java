package com.iquanwai.domain;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.google.common.collect.Maps;
import com.iquanwai.domain.dao.BusinessSchoolApplicationOrderDao;
import com.iquanwai.domain.dao.CouponDao;
import com.iquanwai.domain.dao.CourseOrderDao;
import com.iquanwai.domain.dao.MonthlyCampOrderDao;
import com.iquanwai.domain.dao.QuanwaiOrderDao;
import com.iquanwai.domain.dao.RiseCourseOrderDao;
import com.iquanwai.domain.dao.RiseOrderDao;
import com.iquanwai.domain.message.RestfulHelper;
import com.iquanwai.domain.po.Coupon;
import com.iquanwai.domain.po.PayClose;
import com.iquanwai.domain.po.PayCloseReply;
import com.iquanwai.domain.po.QuanwaiOrder;
import com.iquanwai.util.CommonUtils;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import com.iquanwai.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Autowired
    private RiseCourseOrderDao riseCourseOrderDao;
    @Autowired
    private RiseOrderDao riseOrderDao;
    @Autowired
    private CourseOrderDao courseOrderDao;
    @Autowired
    private MonthlyCampOrderDao monthlyCampOrderDao;
    @Autowired
    private BusinessSchoolApplicationOrderDao businessSchoolApplicationOrderDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CLOSE_ORDER_URL = "https://api.mch.weixin.qq.com/pay/closeorder";

    private static final String ERROR_CODE = "FAIL";

    private static final String SUCCESS_CODE = "SUCCESS";

    /**
     * 关闭微信订单
     *
     * @param courseOrder 课程订单
     */
    private void closeWechatOrder(QuanwaiOrder courseOrder) {
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
    }

    /**
     * 关闭阿里订单
     *
     * @param courseOrder 课程订单
     */
    private void closeAliOrder(QuanwaiOrder courseOrder) {
        String orderId = courseOrder.getOrderId();
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(ConfigUtils.getValue("alipay.gateway"),
                    ConfigUtils.getValue("alipay.appid"),
                    ConfigUtils.getValue("alipay.private.key"),
                    "json",
                    "UTF-8",
                    ConfigUtils.getValue("alipay.public.key"),
                    "RSA2");
            AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();

            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(orderId);
            alipayRequest.setBizModel(model);

            AlipayTradeCloseResponse alipayResponse = alipayClient.execute(alipayRequest);
            if (alipayResponse.isSuccess()) {
                logger.info("关闭成功");
            } else {
                logger.error("关闭失败：{}", alipayResponse.getBody());
            }
            logger.info("{}", alipayResponse);
        } catch (Exception e) {
            logger.error("orderId: " + orderId + " close failed", e);
        }
    }

    public void closeOrder() {
        //点开付费的保留5分钟
        Date date = DateUtils.afterMinutes(new Date(), -5);
        List<QuanwaiOrder> underCloseOrders = quanwaiOrderDao.queryUnderCloseOrders(date);
        for (QuanwaiOrder courseOrder : underCloseOrders) {
            String orderId = courseOrder.getOrderId();

            if (QuanwaiOrder.PAY_WECHAT == courseOrder.getPayType()) {
                closeWechatOrder(courseOrder);
            } else if (QuanwaiOrder.PAY_ALI == courseOrder.getPayType()) {
                closeAliOrder(courseOrder);
            } else {
                logger.error("订单信息错误:{}", orderId);
            }

            //如果有使用优惠券,还原优惠券状态
            if (courseOrder.getDiscount() != 0.0) {
                couponDao.updateCouponByOrderId(Coupon.UNUSED, orderId);
            }

            QuanwaiOrder quanwaiOrder = quanwaiOrderDao.loadOrder(orderId);
            if (quanwaiOrder == null) {
                logger.error("订单 {} 不存在", orderId);
                return;
            }
            if (QuanwaiOrder.SYSTEMATISM.equals(quanwaiOrder.getGoodsType())) {
                courseOrderDao.closeOrder(orderId);
                quanwaiOrderDao.closeOrder(orderId);
            }
            if (QuanwaiOrder.FRAGMENT_MEMBER.equals(quanwaiOrder.getGoodsType())) {
                riseOrderDao.closeOrder(orderId);
                quanwaiOrderDao.closeOrder(orderId);
            }
            if (QuanwaiOrder.FRAGMENT_RISE_COURSE.equals(quanwaiOrder.getGoodsType())) {
                riseCourseOrderDao.closeOrder(orderId);
                quanwaiOrderDao.closeOrder(orderId);
            }
            if (QuanwaiOrder.FRAGMENT_CAMP.equals(quanwaiOrder.getGoodsType())) {
                monthlyCampOrderDao.closeOrder(orderId);
                quanwaiOrderDao.closeOrder(orderId);
            }
            if (QuanwaiOrder.BS_APPLICATION.equals(quanwaiOrder.getGoodsType())) {
                businessSchoolApplicationOrderDao.closeOrder(orderId);
                quanwaiOrderDao.closeOrder(orderId);
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

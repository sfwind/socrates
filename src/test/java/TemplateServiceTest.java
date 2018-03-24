import com.google.common.collect.Maps;
import com.iquanwai.domain.message.TemplateMessage;
import com.iquanwai.domain.message.TemplateMessageService;
import com.iquanwai.util.ConfigUtils;
import com.iquanwai.util.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Created by justin on 2018/3/24.
 */
public class TemplateServiceTest extends TestBase {
    @Autowired
    private TemplateMessageService templateMessageService;
    public final static String PAY_URL = "https://www.iquanwai.com/pay/apply";


    @Test
    public void customerTest() {
        String expiredDateStr = DateUtils.parseDateToString5(
                DateUtils.afterDays(new Date(), 1));

        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser("o-Es21RVF3WCFQMOtl07Di_O9NVo");
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl(PAY_URL);
        // 设置消息 message id
        templateMessage.setTemplate_id(ConfigUtils.getApproveApplyMsgId());
        // 无优惠券模板消息内容
//            String first = "我们很荣幸地通知您被商学院录取，录取有效期24小时，请尽快办理入学，及时开始学习并结识优秀的校友吧！\n";
        String first = "我们很荣幸地通知您被商学院录取，录取有效期24小时。\n";
        data.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！" +
                "\n\n根据你的申请，入学委员会决定发放给你" + 1000
                + "元奖学金，付款时自动抵扣学费。\n"));
//        noCouponData.put("first", new TemplateMessage.Keyword("恭喜！我们很荣幸地通知你被【圈外商学院】录取！希望你在商学院内取得傲人的成绩，和顶尖的校友们一同前进！\n"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword1", new TemplateMessage.Keyword("通过"));
        data.put("remark", new TemplateMessage.Keyword("\n本录取通知24小时内有效，过期后需重新申请。价值799的《战略管理》课程仍有少量免费内测名额，名校EMBA教授授课，现在入学即可联系班主任申领。\n\n请及时点击本通知书，办理入学。", "#f57f16"));

        templateMessageService.sendMessage(templateMessage);
    }
}

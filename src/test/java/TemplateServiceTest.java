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
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser("o-Es21RVF3WCFQMOtl07Di_O9NVo");
        templateMessage.setTemplate_id(ConfigUtils.getRejectApplyMsgId());
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setUrl("https://static.iqycamp.com/images/qrcode_qwzswyh.jpeg?imageslim");
        templateMessage.setComment("发送拒信");
        data.put("keyword1", new TemplateMessage.Keyword("【圈外商学院】"));
        data.put("keyword2", new TemplateMessage.Keyword("未通过"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("remark", new TemplateMessage.Keyword(
                "\n本期商学院的申请者都异常优秀，我们无法为每位申请者提供学习机会，但是很高兴你有一颗追求卓越的心！\n" +
                        "\n" +
                        "扫描二维码，添加【圈外招生委员会】微信"));
        // 同样的对象不需要定义两次
        data.put("first", new TemplateMessage.Keyword(
                "我们认真评估了你的入学申请，认为你的需求和商学院核心能力项目暂时不匹配。\n" +
                        "\n" +
                        "建议关注后续的课程与体验活动。\n" +
                        "\n" +
                        "添加【圈外招生委员会】微信，可实时关注并咨询招生信息。\n"));

        templateMessageService.sendMessage(templateMessage);
    }
}

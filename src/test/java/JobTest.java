import com.iquanwai.domain.AuditionService;
import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.job.*;
import com.iquanwai.util.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by justin on 17/3/21.
 */
public class JobTest extends TestBase {

    @Autowired
    private RiseMemberJob riseMemberJob;
    @Autowired
    private RiseUserJob riseUserJob;
    @Autowired
    private NotifyForumJob forumNotifyJob;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ClosePlanJob closePlanJob;
    @Autowired
    private NotifyRunningLogin notifyRunningLogin;
    @Autowired
    private BusinessSchoolService businessSchoolService;
    @Autowired
    private NotifyBusinessApplicationExpireJob notifyRiseMemberApplyJob;

    @Test
    public void expiredTest() {
        forumNotifyJob.work();
    }

    @Test
    public void customerTest() {
        customerService.userLoginLog(2);

    }
    @Test
    public void testNotify(){
        Date oneDay = DateUtils.beforeDays(new Date(), 1);
        businessSchoolService.sendRiseMemberApplyMessageByDealTime(oneDay, 0);
    }

//    @Test
//    public void searchTest(){
//        businessSchoolService.searchApplications(DateUtils.parseStringToDate("2017-09-22"));
//    }

    @Autowired
    private AuditionService auditionService;

    @Test
    public void test() {
        auditionService.sendAuditionCompleteReward();
    }

}

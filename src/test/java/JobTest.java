import com.iquanwai.domain.BusinessSchoolService;
import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.dao.RiseUserLandingDao;
import com.iquanwai.domain.po.RiseUserLanding;
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
    private ForumNotifyJob forumNotifyJob;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private RiseUserLandingDao riseUserLandingDao;
    @Autowired
    private ClosePlanJob closePlanJob;
    @Autowired
    private NotifyRunningLogin notifyRunningLogin;
    @Autowired
    private BusinessSchoolService businessSchoolService;

    @Test
    public void expiredTest() {
        forumNotifyJob.work();
    }

    @Test
    public void riseJobTest() {
        riseUserJob.work();
    }

    @Test
    public void customerTest() {
        customerService.userLoginLog(2);

    }
    @Test
    public void testNotify(){
        // notifyRunningLogin.notifyHasRunningPlansLogin();;
    }

    @Test
    public void logDaoTest() {
//        List<String> strings = operationLogDao.loadThatDayLoginUser(2);
//
//        strings.forEach(System.out::println);
        RiseUserLanding login = riseUserLandingDao.loadByOpenId("o5h6ywl3-k7FGio94tHPHlw7Eusc");
        Date landingDate = login.getLandingDate();
        System.out.println(DateUtils.parseDateToString(landingDate));
        Date thatDate = DateUtils.beforeDays(new Date(), 2);
        System.out.println(DateUtils.parseDateToString(thatDate));
        Integer diff = DateUtils.interval(thatDate, landingDate);
        System.out.println(diff);
    }

    @Test
    public void searchTest(){
        businessSchoolService.searchApplications(DateUtils.parseStringToDate("2017-09-22"));
    }

    @Test
    public void noticeTest(){
        businessSchoolService.noticeApplication(new Date());
    }

}

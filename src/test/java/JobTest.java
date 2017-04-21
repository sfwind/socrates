import com.iquanwai.domain.CustomerService;
import com.iquanwai.domain.dao.OperationLogDao;
import com.iquanwai.domain.dao.RiseUserLandingDao;
import com.iquanwai.domain.po.RiseUserLanding;
import com.iquanwai.job.NotifyJob;
import com.iquanwai.job.RiseMemberJob;
import com.iquanwai.job.RiseUserJob;
import com.iquanwai.util.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by justin on 17/3/21.
 */
public class JobTest extends TestBase{
    @Autowired
    private NotifyJob notifyJob;
    @Autowired
    private RiseMemberJob riseMemberJob;
    @Autowired
    private RiseUserJob riseUserJob;
    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private RiseUserLandingDao riseUserLandingDao;

    @Test
    public void test(){
        notifyJob.work();
    }

    @Test
    public void expiredTest(){
        riseMemberJob.work();;
    }

    @Test
    public void riseJobTest(){
        riseUserJob.work();
    }

    @Test
    public void customerTest(){
        customerService.userLoginLog(2);

    }

    @Test
    public void logDaoTest(){
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








}

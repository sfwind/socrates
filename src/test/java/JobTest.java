import com.iquanwai.job.NotifyJob;
import com.iquanwai.job.RiseMemberJob;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/3/21.
 */
public class JobTest extends TestBase{
    @Autowired
    private NotifyJob notifyJob;
    @Autowired
    private RiseMemberJob riseMemberJob;

    @Test
    public void test(){
        notifyJob.work();
    }

    @Test
    public void expiredTest(){
        riseMemberJob.work();;
    }
}

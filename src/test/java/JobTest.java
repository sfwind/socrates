import com.iquanwai.job.NotifyJob;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/3/21.
 */
public class JobTest extends TestBase{
    @Autowired
    private NotifyJob notifyJob;

    @Test
    public void test(){
        notifyJob.work();
    }
}

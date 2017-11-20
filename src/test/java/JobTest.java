import com.iquanwai.domain.AuditionService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/3/21.
 */
public class JobTest extends TestBase {

    @Autowired
    private AuditionService auditionService;

    @Test
    public void test() {
        auditionService.sendAuditionCompleteReward();
    }

}

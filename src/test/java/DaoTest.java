import com.iquanwai.domain.accesstoken.AccessTokenService;
import com.iquanwai.domain.dao.HomeworkVoteDao;
import com.iquanwai.domain.dao.ImprovementPlanDao;
import com.iquanwai.domain.dao.RedisUtil;
import com.iquanwai.domain.po.HomeworkVote;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/3/9.
 */
public class DaoTest extends TestBase {
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;

    @Test
    public void test(){
        homeworkVoteDao.loadAll(HomeworkVote.class);
    }

    @Test
    public void testRedis(){
        System.out.println(accessTokenService.getAccessToken());
    }

    @Test
    public void testClosePlan(){
        improvementPlanDao.updateCloseTime(6929);
    }
}

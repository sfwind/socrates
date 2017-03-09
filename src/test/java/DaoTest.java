import com.iquanwai.domain.dao.HomeworkVoteDao;
import com.iquanwai.domain.po.HomeworkVote;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/3/9.
 */
public class DaoTest extends TestBase {
    @Autowired
    private HomeworkVoteDao homeworkVoteDao;

    @Test
    public void test(){
        homeworkVoteDao.loadAll(HomeworkVote.class);
    }
}

import com.iquanwai.domain.GiftService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class GiftServiceTest extends TestBase {
   @Autowired
   private GiftService giftService;
    @Test
    public void generateList() throws Exception {
        giftService.generateList();
    }

}
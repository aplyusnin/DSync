import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.nsu.fit.dsync.server.storage.UserMetaData;

import java.util.HashSet;

public class UserMetaDataTest {
	@Test
	@Ignore
	public void test1(){
		try {
			HashSet<String> shared = UserMetaData.getInstance().getSharedFiles("John");
			Assert.assertEquals(1, shared.size());
		}
		catch (Exception e){
			Assert.fail();
		}
	}

}

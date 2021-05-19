package functional;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.RepoHandler;

public class RepoHandlerTest {

	@Test
	public void lastVersion(){
		try {
			RepoHandler handler = FileManager.getInstance().getHandler("1", "repo1");
			Assert.assertEquals("3c35bf778a7a6c61a150da8705559da4a4ec58aa4c50fd5ee68586c816deaa8d", handler.getLastVersion("File1"));
		}
		catch (Exception e){
			Assert.fail();
		}
	}
	
}

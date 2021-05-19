package functional;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.dsync.server.storage.FileManager;

public class FileManagerTest {

	@Test
	public void getHandler1(){
		try
		{
			FileManager.getInstance().getHandler("1", "repo1");
		}
		catch (Exception e){
			Assert.fail();
		}
	}

	@Test
	public void getHandler2(){
		try
		{
			FileManager.getInstance().getHandler("1", "not_existing_handler");
			Assert.fail();
		}
		catch (Exception e){
		}
	}
}

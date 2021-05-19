package functional;

import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.dsync.server.storage.FileManager;
import ru.nsu.fit.dsync.server.storage.UserMetaData;

public class UserMetaDataTest {

	@Test
	public void hasUser1() {
		Assert.assertTrue(UserMetaData.getInstance().isUserExists("1"));
	}

	@Test
	public void hasUser2() {
		Assert.assertFalse(UserMetaData.getInstance().isUserExists("3"));
	}

	@Test
	public void hasAccess(){
		try {
			Assert.assertTrue(UserMetaData.getInstance().hasAccess("1", FileManager.getInstance().getHandler("1", "repo1")));
		}
		catch (Exception e){
			Assert.fail();
		}
	}

	@Test
	public void validateUserData(){
		try{
			UserMetaData.getInstance().validateUserData("1", "12345");
		}
		catch (Exception e){
			Assert.fail();
		}
	}

}

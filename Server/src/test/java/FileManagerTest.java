import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.nsu.fit.dsync.server.storage.UserMetaData;


import java.io.File;
import java.util.HashSet;



public class FileManagerTest {

	@Test
	@Ignore
	public void hashTest() {
		try
		{
			HashSet<String> shared = UserMetaData.getInstance().getSharedFiles("John");
			String name = shared.iterator().next();
			File file = new File("src/test/resources/Users/" + name);

			//System.out.println(FileManager.getInstance().sha256Hash(new FileInputStream(file)));

		}
		catch (Exception e)
		{
			Assert.fail();
		}
	}

	@Test
	@Ignore
	public void copyTest() {
		/*try {
			DirHandler handler = FileManager.getInstance().getHandler("src/test/resources/Users/Alex/Files/Lab1_Source/");
			File root = handler.getFile();
			File file1 = new File("src/test/resources/Users/Alex/Files/Lab1_Source/data/source.cpp/v2/source.cpp");
			String version = FileManager.getInstance().createVersion(file1, new FileInfo("Alex", "Lab1_Source", "", "source.cpp"), "source.cpp");
			FileManager.getInstance().updateLatestVersion(handler, new FileInfo("Alex", "Lab1_Source", "", "source.cpp"), version);
			handler.releaseFile();
		}
		catch (Exception e){
			Assert.fail();
		}*/
	}
}

package requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Test;
import ru.nsu.fit.dsync.TestClient;
import ru.nsu.fit.dsync.server.DSyncServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SocketTest {
	@Test
	public void test(){
		DSyncServer server = new DSyncServer(8090);

		var t = new Thread(() -> {
			try{
				server.run();
			}
			catch (Exception e){
			}});
		t.start();

		try {
			TestClient.run();
		}
		catch (Exception e){
			System.err.println(e.getMessage());
			Assert.fail();
		}
		t.interrupt();
		try {
			server.shutdown();
		}
		catch (Exception e){

		}
	}
}

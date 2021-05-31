package requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.nsu.fit.dsync.server.DSyncServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RepoCreationTest {
	@Test
	@Ignore
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
			//Thread.sleep(100000);
			URL log = new URL("http://localhost:8090/LOGIN?login=1&password=12345");
			HttpURLConnection logconnection = (HttpURLConnection)log.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(logconnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}

			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode root = objectMapper.readValue(response.toString(), ObjectNode.class);

			String token = root.get("token").asText();


			URL url = new URL("http://localhost:8090/DATA/NEWREPO?repo=testrepo");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();


			connection.setRequestMethod("GET");
			connection.addRequestProperty("X-Access-Token", token);
				/*connection.addRequestProperty("login", "1");
				connection.addRequestProperty("password", "12345");
				connection.addRequestProperty("owner", "1");
				connection.addRequestProperty("repo", "repo1");*/


			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			inputLine = "";
			response = new StringBuffer();
			while((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}

			root = objectMapper.readValue(response.toString(), ObjectNode.class);

			Assert.assertNull(root.get("error"));
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
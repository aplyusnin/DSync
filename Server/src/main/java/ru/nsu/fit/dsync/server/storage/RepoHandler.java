package ru.nsu.fit.dsync.server.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import ru.nsu.fit.dsync.utils.InvalidRequestDataException;
import ru.nsu.fit.dsync.utils.Misc;
import ru.nsu.fit.dsync.utils.Pair;

/**
 * Handle of commit.
 */
public class  RepoHandler {

	public static final int LIMIT = 3;

	private String owner;
	private String repoName;
	private File versions;

	private ObjectMapper objectMapper = new ObjectMapper();
	private ObjectNode root;
	private int changes = 0;

	private String repoRoot;


	public RepoHandler(String owner, String repoName) throws Exception {
		File rRoot = new File("Users/" + owner + "/Files/" + repoName);
		if (!rRoot.exists() || !rRoot.isDirectory()) throw new InvalidRequestDataException("Repo doesn't exist");
		this.owner = owner;
		this.repoName = repoName;
		this.versions = new File("Users/" + owner + "/Files/" + repoName + "/versions.json");
		this.root = objectMapper.readValue(versions, ObjectNode.class);
		repoRoot = "Users/" + owner + "/Files/" + repoName + "/";
	}

	public String getLastVersion(String filename) throws Exception{
		synchronized (this) {
			try {
				return root.get(filename).asText();
			}
			catch (Exception e){
				throw new InvalidRequestDataException("File doesn't exist");
			}
		}
	}

	public void rewriteVersionEntry(String filename, String version){
		synchronized (this){
			TextNode node = new TextNode(version);
			root.set(filename, node);
			changes ++;
			if (changes > LIMIT){
				try {
					changes = 0;
					PrintStream printStream = new PrintStream(new FileOutputStream(versions));
					printStream.println(root.toString());
				}
				catch (Exception e){
					System.err.println("Can't rewrite new version");
				}
			}
		}
	}

	public File findFile(String filename, String version) throws Exception{
		try {
			File[] files = new File(repoRoot + filename + "/" + version).listFiles();
			if (files.length != 1) throw new Exception("Invalid storage state: " + repoRoot + filename + "/" + version);
			return files[0];
		}
		catch (Exception e){
			throw new InvalidRequestDataException("Version of file doesn't exists");
		}
	}


	public File getTemp() throws Exception{
		File file1 = new File(repoRoot + "temp.tmp");
		if (file1.exists()){
			PrintWriter writer = new PrintWriter(file1);
			writer.close();
		}
		else{
			file1.createNewFile();
			file1.deleteOnExit();
		}
		return file1;
	}

	public List<Pair<String, String>> getFiles() {
		File[] flist = new File(repoRoot).listFiles();
		List<Pair<String, String>> res = new LinkedList<>();
		for (var x : flist){
			if (x.isDirectory())
				res.add(new Pair<>(x.getName(), root.get(x.getName()).asText()));
		}
		return res;
	}

	/**
	 * Creates new file version and fills it with pre-stored data from temp
	 * @param temp  pre-stored file
	 * @param name1 - file-name
	 * @param name name of initial file
	 * @return version name
	 * @throws Exception - couldn't create copy
	 */
	public String createVersion(File temp, String name1, String name) throws Exception {
		String hash = Misc.bytesToHex(Misc.getSHA256Hash(new FileInputStream(temp)));
		String path = repoRoot + name1 + "/" + hash;
		File newDir = new File(path);
		if (!newDir.mkdirs()) return hash;//throw new Exception("Can't create new version");
		File file = new File(newDir.getPath() + "/" + name);
		if (!file.createNewFile()) return hash;//throw new Exception("Can't create new version");
		int size = 8196;
		byte[] buffer = new byte[size];
		int count = 0;

		InputStream inputStream = new FileInputStream(temp);
		OutputStream outputStream = new FileOutputStream(file);

		while ((count = inputStream.read(buffer)) > 0)
		{
			outputStream.write(buffer, 0, count);
		}
		return hash;
	}


	public String getOwner() {
		return owner;
	}

	public String getRepoName() {
		return repoName;
	}

}

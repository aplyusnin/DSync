package ru.nsu.fit.dsync.server.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ru.nsu.fit.dsync.utils.InvalidRequestDataException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.concurrent.Semaphore;


/**
 * Handle of commit.
 */
public class DirHandler {

	private String name;
	private File file;

	private Semaphore awaiting;
	private File versions;
	private ObjectMapper objectMapper = new ObjectMapper();
	private ObjectNode root;
	private int changes = 0;

	/**
	 * Creates handler of commit directory
	 * @param filename - name of directory
	 * @throws Exception - unable to chreate
	 */
	public DirHandler(String filename) throws Exception{
		this.file = new File(filename);
		if (!this.file.isDirectory()) throw new InvalidRequestDataException("Repository doesn't exist");
		this.versions= new File(filename + "/versions.json");
		this.name = filename;
		this.root = objectMapper.readValue(versions, ObjectNode.class);
		this.awaiting = new Semaphore(1);
	}

	/**
	 * Get root of commit, only one thread can work with one handler at the same moment
	 * @return - root of commit
	 * @throws Exception unable to get commit
	 */
	public File getFile() throws Exception{
		awaiting.acquire(1);
		return file;
	}

	/**
	 * Frees file for other threads
	 */
	public void releaseFile() {
		try {
			if (changes > 0) {
				changes = 0;
				PrintStream printStream = new PrintStream(new FileOutputStream(versions));
				printStream.println(root.toString());
			}
		}
		catch (Exception e){
			System.err.println("Can't");
		}
		awaiting.release(1);
	}

	/**
	 * Rewrites new latest version association in stored file versions
	 * @param filename - name of file
	 * @param version - name of version
	 */
	public void rewriteVersionEntry(String filename, String version){
		TextNode node = new TextNode(version);
		root.set(filename, node);
		changes ++;
	}


	public String getLastVersion(String filename) throws Exception {
		try
		{
			return root.get(filename).asText();
		}
		catch (Exception e){
			throw new InvalidRequestDataException("File doesn't exits");
		}
	}

	public File findFile(String filename, String version) throws Exception{
		try {
			File[] files = new File(file.getPath() + "/" + filename + "/" + version).listFiles();
			if (files.length != 1) throw new Exception("Invalid storage state: " + file.getPath() + "/" + filename + "/" + version);
			return files[0];
		}
		catch (Exception e){
			throw new InvalidRequestDataException("Version of file doesn't exists");
		}
	}


}

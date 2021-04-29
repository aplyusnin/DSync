package ru.nsu.fit.dsync.server.storage;

/**
 * Information about file
 */
public class FileInfo {
	private String owner;
	private String commit; 
	private String localpath;
	private String name;

	/**
	 * Create file info
	 * @param owner - file owner
	 * @param commit - initial directory
	 * @param localpath - local path in comited directory
	 * @param name - initial name of file
	 */
	public FileInfo(String owner, String commit, String localpath, String name){
		this.owner = owner;
		this.commit = commit;
		this.localpath = localpath;
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLocalPath(String dir) {
		this.localpath = dir;
	}

	public void setCommit(String commit) {this.commit = commit;}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public String getCommit() {
		return commit;
	}

	public String getLocalpath() {
		return localpath;
	}

	public String getOwner() {
		return owner;
	}


}

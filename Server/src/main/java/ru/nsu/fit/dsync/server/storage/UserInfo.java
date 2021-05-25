package ru.nsu.fit.dsync.server.storage;

import java.util.HashSet;

public class UserInfo {
	private String name;
	private HashSet<String> tokens;

	public UserInfo(String name){
		this.name = name;
		tokens = new HashSet<>();
	}

	public void registerToken(String token)
	{
		tokens.add(token);
	}

	public boolean validateToken(String token)
	{
		return tokens.contains(token);
	}

}

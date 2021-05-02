package ru.nsu.fit.dsync.utils;

public class InvalidRequestDataException extends Exception {
	public InvalidRequestDataException(String errorMessage){
		super(errorMessage);
	}
}

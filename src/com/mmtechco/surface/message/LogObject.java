package com.mmtechco.surface.message;

public class LogObject {
	private static final String
	TIME = "time",
	PRIORITY = "priority",
	CLASSNAME = "path",
	MESSAGE = "message";
	
	private long time;
	private String priority, classname, message;
	
	
	public LogObject(String priority, String classname, String message) {
		time = System.currentTimeMillis();
		this.priority = priority;
		this.classname = classname;
		this.message = message;
	}
}

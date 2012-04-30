package com.mmtechco.surface.message;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.mmtechco.surface.Registration;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class ErrorMessage implements Message {
	private static final String INFO = "info", type = "ERR";

	JSONArray errors;

	public ErrorMessage(String classname, Exception e) {
		this.errors = new JSONArray();
		this.errors.put(new ErrorObject(classname, e));
	}

	public String toJSON() {
		JSONObject outer = new JSONObject();
		try {
			outer.put(ID, Registration.getRegID());
			outer.put(TIME, System.currentTimeMillis() / 1000);
			outer.put(TYPE, type);
			outer.put(INFO, errors);
		} catch (JSONException e) {
			Logger.getInstance().log("REG JSON", e.getMessage());
		}
		return outer.toString();
	}
}

class ErrorObject {
	private static final String
	TIME = "time",
	CLASSNAME = "path",
	EXCEPTION_CLASS = "type",
	EXCEPTION_MESSAGE = "description",
	UPTIME = "upTime";
	
	private long time, uptime;
	private String classname, exceptionClass, exceptionMessage;
	
	public ErrorObject(String classname, Exception e) {
		time = System.currentTimeMillis();
		uptime = ToolsBB.getInstance().getUptimeInSec();
		this.classname = classname;
		this.exceptionClass = e.getClass().getName();
		this.exceptionMessage = e.getMessage();
	}
	
	public String toJSON() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(TIME, time);
			obj.put(CLASSNAME, classname);
			obj.put(EXCEPTION_CLASS, exceptionClass);
			obj.put(EXCEPTION_MESSAGE, exceptionMessage);
			obj.put(UPTIME, uptime);
		} catch (JSONException e) {
			Logger.getInstance().log("ErrorObj JSON", e.getMessage());
		}
		return obj.toString();
	}
}
package com.mmtechco.surface.message;

import java.util.Vector;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.mmtechco.surface.Registration;
import com.mmtechco.util.Logger;

public class LogMessage implements Message {
	private static final String INFO = "info", type = "LOG";

	JSONArray logs;

	public LogMessage(Vector logs) {
		this.logs = new JSONArray();
		this.logs.put(logs);
	}

	public String toJSON() {
		JSONObject outer = new JSONObject();
		try {
			outer.put(ID, Registration.getRegID());
			outer.put(TIME, System.currentTimeMillis() / 1000);
			outer.put(TYPE, type);
			outer.put(INFO, logs);
		} catch (JSONException e) {
			Logger.getInstance().log("REG JSON", e.getMessage());
		}
		return outer.toString();
	}
}

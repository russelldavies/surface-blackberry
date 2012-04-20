package com.mmtechco.surface.net;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.mmtechco.surface.Registration;
import com.mmtechco.util.Logger;

public class EventRequest {
	private static final String ID = "id", TIME = "time", TYPE = "type",
			LOCATION = "location",
			LAT = "lat",
			LONG = "long",
			STATE = "state",
			CURRENT = "current";
	
	private final static String type = "EVE";

	private double latitude, longitude;
	private String state;

	public EventRequest(double latitude, double longitude, String state) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.state = state;
	}

	public String toJSON() {
		JSONObject outer = new JSONObject();
		JSONObject locationObj = new JSONObject();
		JSONObject stateObj = new JSONObject();
		try {
			outer.put(ID, Registration.getRegID());
			outer.put(TIME, System.currentTimeMillis() / 1000);
			outer.put(TYPE, type);
			
			// TODO: why can't I put null?
			/*
			if (latitude == 0 || longitude == 0) {
				outer.put(LOCATION, null);
			} else {
			*/
				outer.put(LOCATION, locationObj);
				locationObj.put(LAT, latitude);
				locationObj.put(LONG, longitude);
			//}
			
			outer.put(STATE, stateObj);
			stateObj.put(CURRENT, state);
			
		} catch (JSONException e) {
			Logger.getInstance().log("REG JSON", e.getMessage());
		}
		return outer.toString();
	}
}
package com.mmtechco.surface.message;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.util.Logger;

public class EventMessage implements Message {
	public static final String STATE_NON = "NON", STATE_ALH = "ALH",
			STATE_SUR = "SUR", STATE_MIS = "MIS", STATE_MNS = "MNS";

	private static final String 
			LOCATION = "location",
			LAT = "lat",
			LONG = "long",
			STATE = "state",
			CURRENT = "current";
	
	private final static String type = "EVE";

	private double latitude, longitude;
	private String state;

	public EventMessage(String state) {
		this.latitude = LocationMonitor.latitude;
		this.longitude = LocationMonitor.longitude;
		this.state = state;
	}
	
	public String getState() {
		return state;
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
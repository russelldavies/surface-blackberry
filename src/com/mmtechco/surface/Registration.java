package com.mmtechco.surface;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.IDENInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.COMMAND_TARGETS;
import com.mmtechco.surface.prototypes.Controllable;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

/**
 * Checks the registration stage that currently the device is in.
 */
public class Registration implements Controllable, SurfaceResource {
	private static final String TAG = ToolsBB
			.getSimpleClassName(Registration.class);
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	public static final long ID = StringUtilities
			.stringHashToLong(Registration.class.getName());

	public final static String KEY_STAGE = "registration_stage";
	public final static String KEY_ID = "registration_id";
	public final static String KEY_NUMBERS = "emergency_numbers";
	
	private final static int intervalShort = 1000 * 60 * 2; // 2 min
	private final static int intervalLong = 1000 * 60 * 60 * 24; // 24h
	
	private static int stage;
	private static String id;
	private static String status = r.getString(i18n_RegRequesting);
	private static Vector emergNums;

	private static Logger logger = Logger.getInstance();
	private static Vector observers = new Vector();

	
	public static void checkStatus() {
		logger.log(TAG, "Checking registration status");
		
		readDetails();
		
		// Contact server and get new values, if any
		logger.log(TAG, "Requesting reg details from server");
		String response = Server.post(new RegistrationRequestObject(id).toJSON());
		logger.log(TAG, "Server response: " + response);
		RegistrationReplyObject reply = RegistrationReplyObject.fromJSON(response);
		if (response == null || reply == null) {
			logger.log(TAG, "Bad server response. Scheduling short run");
			scheduleRun(intervalShort);
			return;
		}
		stage = reply.stage;
		id = reply.id;
		storeDetails();
		
		// Schedule a registration check based on stage
		if (stage < 2) {
			logger.log(TAG, "Scheduling short run");
			scheduleRun(intervalShort);
		} else {
			startComponents();
			logger.log(TAG, "Scheduling long run");
			scheduleRun(intervalLong);
		}
	}

	private static void scheduleRun(int sleepTime) {
		updateStatus();
		new Timer().schedule(new TimerTask() {
			public void run() {
				checkStatus();
			}
		}, sleepTime);
	}
	
	private static void readDetails() {
		// Read registration data or set to default values
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			if (regTable == null) {
				logger.log(TAG, "Populating with default values");
				// Populate with default values
				regTable = new Hashtable();
				regTable.put(KEY_STAGE, "0"); stage = 0;
				regTable.put(KEY_ID, id = "");
				regTable.put(KEY_NUMBERS, emergNums = new Vector());
				// Store to device
				regData.setContents(regTable);
				regData.commit();
			} else {
				logger.log(TAG, "Reading details from storage");
				// Read values from storage
				stage = Integer.parseInt((String) regTable.get(KEY_STAGE));
				id = (String) regTable.get(KEY_ID);
				emergNums = (Vector) regTable.get(KEY_NUMBERS);
			}
		}
	}
	
	private static void storeDetails() {
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			regTable.put(KEY_STAGE, String.valueOf(stage));
			regTable.put(KEY_ID, id);
			regTable.put(KEY_NUMBERS, emergNums);
			// Store to device
			regData.setContents(regTable);
			regData.commit();
		}
		logger.log(TAG, "Stored details");
	}

	private static void startComponents() {
		// Check to see if haven't already started components
		// Using RuntimeStore because the BB class loader doesn't handle
		// static class variables properly
		Boolean started = (Boolean) RuntimeStore.getRuntimeStore().get(ID);
		if (started == null || !started.booleanValue()) {
			if (ApplicationManager.getApplicationManager().postGlobalEvent(ID)) {
				logger.log(TAG, "Fired event to start components");
				RuntimeStore.getRuntimeStore().put(ID, new Boolean(true));
			}
		}
	}
	
	private static void updateStatus() {
		switch (stage) {
		case 0: // Initialization state
			status = r.getString(i18n_RegRequesting);
			break;
		case 1: // Has id but not activated
			status = r.getString(i18n_RegNotActivated);
			// TODO: put this into thread
			// tools.addMsgToInbox(r.getString(i18n_WelcomeMsg));
			break;
		case 2: // Trial
			status = r.getString(i18n_RegTrial);
			break;
		case 3: // Fully active
			status = r.getString(i18n_RegActive);
			break;
		}
		logger.log(TAG, "Update status: " + stage + ";" + id + ";" + status);
		
		// Tell screens to update themselves
		notifyObservers();
	}
	
	private static void notifyObservers() {
		String displayId = id;
		if (id.length() == 0) {
			displayId = "[none]";
		}
		String statusMsg = "ID: " + displayId + " | Status: " + status;
		for (int i = 0; i < observers.size(); i++) {
			((ObserverScreen) observers.elementAt(i)).setStatus(statusMsg);
		}
	}
	
	public static void addObserver(ObserverScreen screen) {
		observers.addElement(screen);
	}

	public static void removeObserver(ObserverScreen screen) {
		observers.removeElement(screen);
	}

	/**
	 * Get the device registration ID. 
	 * 
	 * @return registration ID string. <strong>"0"</strong> if not available.
	 */
	public static String getRegID() {
		if (id.length() == 0) {
			return "0";
		} else {
			return id;
		}
	}

	/**
	 * Gets the emergency numbers associated with the account.
	 * 
	 * @return Vector with each element a string containing a number
	 */
	public static Vector getEmergNums() {
		return emergNums;
	}

	public boolean processCommand(String[] inputArgs) {
		logger.log(TAG, "Processing Owner Number Command...");
		boolean complete = false;
		if (inputArgs[0].equalsIgnoreCase("lost")
				&& inputArgs[1].equalsIgnoreCase("number")) {

			logger.log(TAG, "args[0] :" + inputArgs[0]);
			logger.log(TAG, "args[1] :" + inputArgs[1]);
			logger.log(TAG, "args[2] :" + inputArgs[2]);
			try {
				String[] nums = ToolsBB.getInstance().split(inputArgs[2], "&");
				for (int i = 0; i < nums.length; i++) {
					emergNums.addElement(nums[i]);
				}
				storeDetails();
				complete = true;
			} catch (Exception e) {
				ActivityLog.addMessage(new ErrorMessage(e));
				complete = false;
			}
		}
		return complete;
	}

	public boolean isTarget(COMMAND_TARGETS targets) {
		if (targets == COMMAND_TARGETS.OWNER) {
			return true;
		} else {
			return false;
		}
	}
}

class RegistrationRequestObject {
	private static final String
	ID = "id",
	TIME = "time",
	TYPE = "type",
	INFO = "info",
	CLIENT_VER = "client",
	DEVICE_NUM = "deviceNum",
	PHONE_NUM = "phoneNum",
	MODEL = "model",
	OS = "os",
	OS_VER = "osVer";
	private final static String type = "REG";
	
	private String id;

	public RegistrationRequestObject(String id) {
		this.id = id;
	}

	public String toJSON() {
		JSONObject outer = new JSONObject();
		JSONObject inner = new JSONObject();
		try {
			outer.put(ID, id);
			outer.put(TIME, System.currentTimeMillis() / 1000);
			outer.put(TYPE, type);
			outer.put(INFO, inner);
			
			inner.put(CLIENT_VER, ApplicationDescriptor.currentApplicationDescriptor().getVersion());
			inner.put(DEVICE_NUM, Tools.stringToHex(IDENInfo.imeiToString(IDENInfo.getIMEI())));
			inner.put(PHONE_NUM, Phone.getDevicePhoneNumber(false));
			inner.put(MODEL, String.valueOf(Branding.getVendorId()));
			inner.put(OS, "BlackBerry");
			inner.put(OS_VER, DeviceInfo.getSoftwareVersion());
		} catch (JSONException e) {
			Logger.getInstance().log("REG JSON", e.getMessage());
		}
		return outer.toString();
	}
}

class RegistrationReplyObject {
	private static final String
	ID = "id",
	STAGE = "stage";
	
	public int stage;
	public String id;
	
	public RegistrationReplyObject(String id, int stage) {
		this.id = id;
		this.stage = stage;
	}
	
	public static RegistrationReplyObject fromJSON(String jsonString) {
		if (jsonString == null) {
			return null;
		}
		String id = null;
		int stage = 0;
		try {
			JSONObject object = new JSONObject(jsonString);
			if (object != null) {
				id = object.getString(ID);
				stage = object.getInt(STAGE);
				return new RegistrationReplyObject(id, stage);
			}
		} catch (JSONException e) {
		}
		return null;
	}
}
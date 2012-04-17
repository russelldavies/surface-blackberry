package com.mmtechco.surface;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.util.StringUtilities;

import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.surface.net.Reply;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.COMMAND_TARGETS;
import com.mmtechco.surface.prototypes.Controllable;
import com.mmtechco.surface.prototypes.Message;
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
		Reply response = new Server().contactServer(new RegistrationMessage(stage));
		logger.log(TAG, "Server response: " + response.getREST());
		if (response.isError()) {
			logger.log(TAG, "Bad server response. Scheduling short run");
			scheduleRun(intervalShort);
			return;
		}
		stage = Integer.parseInt(response.getInfo());
		id = response.getRegID();
		storeDetails();
		updateStatus();
		
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

class RegistrationMessage implements Message {
	private final static int type = 9;
	private final String appVersion = ApplicationDescriptor
			.currentApplicationDescriptor().getVersion();
	private String deviceTime;
	private int stage;
	private String phoneNum;
	private String deviceID;
	private String info;
	private String manufacturer;
	private ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	public RegistrationMessage(int stage) {
		this.stage = stage;
		deviceTime = tools.getDate();
		manufacturer = String.valueOf(Branding.getVendorId());
		phoneNum = Phone.getDevicePhoneNumber(false);
		deviceID = Integer.toHexString(DeviceInfo.getDeviceId());
		info = "BlackBerry";
	}

	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + '0'
				+ type + Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + stage
				+ Tools.ServerQueryStringSeparator + phoneNum
				+ Tools.ServerQueryStringSeparator + deviceID
				+ Tools.ServerQueryStringSeparator + manufacturer
				+ Tools.ServerQueryStringSeparator + DeviceInfo.getDeviceName()
				+ Tools.ServerQueryStringSeparator
				+ DeviceInfo.getSoftwareVersion()
				+ Tools.ServerQueryStringSeparator + appVersion
				+ Tools.ServerQueryStringSeparator + info;
	}

	public String getTime() {
		return deviceTime;
	}

	public int getType() {
		return type;
	}
}
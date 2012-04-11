package com.mmtechco.surface;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Branding;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
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

	public static String KEY_STAGE = "registration_stage";
	public static String KEY_ID = "registration_id";
	public static String KEY_NUMBERS = "emergency_numbers";
	
	public static String[] scheduleArgs = {"reg"};

	private final static int intervalShort = 1000 * 60 * 2; // 2 min
	private final static int intervalLong = 1000 * 60 * 60 * 24; // 24h
	
	private static Integer stage;
	private static String id;
	private static String status;
	private static Vector emergNums;

	private static Server server;
	private static Logger logger = Logger.getInstance();
	private static ToolsBB tools = (ToolsBB) ToolsBB.getInstance();

	private static Vector observers = new Vector();

	
	public static void checkStatus() {
		// Read details from storage
		readDetails();
		updateUi();
		
		// Contact server and get new values, if any
		if (stage.intValue() < 2) {
			logger.log(TAG, "Requesting reg details from server");
			Reply response = server.contactServer(new RegistrationMessage(
					stage.intValue()));
			logger.log(TAG, "Server response: " + response.getREST());
			
			if (response.isError()) {
				logger.log(TAG,
						"Bad server response. Sleeping for a short time.");
				scheduleRun(intervalShort);
				return;
			}
			stage = Integer.valueOf(response.getInfo());
			id = response.getRegID();
			updateUi();
			storeDetails();
			checkStatus();
		} else {
			scheduleRun(intervalLong);
		}
	}

	private static void scheduleRun(int sleepTime) {
        ApplicationDescriptor current = ApplicationDescriptor.
                currentApplicationDescriptor();
		current = new ApplicationDescriptor(current, current.getName(), scheduleArgs);
		current.setPowerOnBehavior(ApplicationDescriptor.DO_NOT_POWER_ON);
        ApplicationManager.getApplicationManager().scheduleApplication(current,
				System.currentTimeMillis() + sleepTime, true);
	}
	
	private static void readDetails() {
		// Read registration data or set to default values
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			if (regTable == null) {
				// Populate with default values
				regTable = new Hashtable();
				regTable.put(KEY_STAGE, stage = new Integer(0));
				regTable.put(KEY_ID, id = null);
				regTable.put(KEY_NUMBERS, emergNums = new Vector());
				// Store
				regData.setContents(regTable);
				regData.commit();
			} else {
				// Read values from storage
				stage = (Integer) regTable.get(KEY_STAGE);
				id = (String) regTable.get(KEY_ID);
				emergNums = (Vector) regTable.get(KEY_NUMBERS);
			}
		}
	}
	
	private static void storeDetails() {
		PersistentObject regData = PersistentStore.getPersistentObject(ID);
		synchronized (regData) {
			Hashtable regTable = (Hashtable) regData.getContents();
			regTable.put(KEY_STAGE, stage);
			regTable.put(KEY_ID, id);
			regTable.put(KEY_NUMBERS, emergNums);
			// Store
			regData.setContents(regTable);
			regData.commit();
		}
	}

	private static void updateUi() {
		//Surface app = (Surface) UiApplication.getUiApplication();

		switch (stage.intValue()) {
		case 0: // New install
			status = r.getString(i18n_RegRequesting);
			break;
		case 1:// New & has id
			status = r.getString(i18n_RegNotActivated);
			// TODO: put this into thread
			// tools.addMsgToInbox(r.getString(i18n_WelcomeMsg));
			break;
		case 2: // Trial
			status = r.getString(i18n_RegTrial);
			//app.startComponents();
			// TODO: should this be here?
			ApplicationManager.getApplicationManager().postGlobalEvent(ID);
			break;
		case 3: // Fully active
			status = r.getString(i18n_RegActive);
			//app.startComponents();
			// TODO: should this be here?
			ApplicationManager.getApplicationManager().postGlobalEvent(ID);
			break;
		}
		logger.log(TAG, "Status text updated to: " + status);
		logger.log(TAG, "Reg id:" + id);
		
		// Tell screens to update themselves
		notifyObservers();
	}
	
	private static void notifyObservers() {
		String statusMsg;
		if (id == null) {
			statusMsg = "SN: [none] | Status: " + status;
		} else {
			statusMsg = "SN: " + id + " | Status: " + status;
		}
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
		if (id == null) {
			return "0";
		} else {
			return id;
		}
	}

	public static String getStatus() {
		return status;
	}

	/**
	 * Gets the emergency numbers associated with the account.
	 * 
	 * @return String array of emergency numbers.
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
				emergNums.addElement(inputArgs[2]);
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
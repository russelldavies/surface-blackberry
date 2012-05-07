package com.mmtechco.surface;

import java.util.Hashtable;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.StringUtilities;

public class Settings {
	// Settings keys
	private static long ID = StringUtilities.stringHashToLong(Application
			.getApplication().getClass().getName());
	private static String KEY_GEN_SURFACE = "gen_surface";
	private static String KEY_GEN_SOUND = "gen_sound";
	private static String KEY_ALERT_CALL = "alert_call";
	private static String KEY_ALERT_NUMS = "emergencynums";
	private static String KEY_ALERT_STATIONARY = "alert_stationary";
	private static String KEY_SHIELD = "shield";
	private static String KEY_ALERT_BUTTON = "alertbutton";

	// Values
	public static boolean genSurface;
	public static boolean genSound;

	public static boolean alertCall;
	public static String emergencyNum;
	public static int alertStationary;

	public static boolean shieldOn;
	public static boolean alertOn;

	public static void readSettings() {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			if (settingsTable == null) {
				settingsTable = new Hashtable(7);
				
				// Populate with default values
				settingsTable.put(KEY_GEN_SURFACE, new Boolean(true));
				settingsTable.put(KEY_GEN_SOUND, new Boolean(true));

				settingsTable.put(KEY_ALERT_CALL, new Boolean(false));
				settingsTable.put(KEY_ALERT_NUMS, "112");
				settingsTable.put(KEY_ALERT_STATIONARY, new Integer(5));

				settingsTable.put(KEY_SHIELD, new Boolean(false));
				settingsTable.put(KEY_ALERT_BUTTON, new Boolean(false));
				
				// Store
				settings.setContents(settingsTable);
				settings.commit();
			}
			
			genSurface = ((Boolean) settingsTable.get(KEY_GEN_SURFACE))
					.booleanValue();
			genSound = ((Boolean) settingsTable.get(KEY_GEN_SOUND))
					.booleanValue();

			alertCall = ((Boolean) settingsTable.get(KEY_ALERT_CALL))
					.booleanValue();
			emergencyNum = ((String) settingsTable.get(KEY_ALERT_NUMS));
			alertStationary = ((Integer) settingsTable
					.get(KEY_ALERT_STATIONARY)).intValue();

			shieldOn = ((Boolean) settingsTable.get(KEY_SHIELD)).booleanValue();
			alertOn = ((Boolean) settingsTable.get(KEY_ALERT_BUTTON))
					.booleanValue();
		}
	}

	private static void updateSetting(String key, Object value) {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			settingsTable.put(key, value);
			settings.setContents(settingsTable);
			settings.commit();
		}
	}

	public static void setGenSurface(boolean genSurface) {
		Settings.genSurface = genSurface;
		updateSetting(KEY_GEN_SURFACE, new Boolean(genSurface));
	}

	public static void setGenSound(boolean genSound) {
		Settings.genSound = genSound;
		updateSetting(KEY_GEN_SOUND, new Boolean(genSound));
	}

	public static void setAlertCall(boolean alertCall) {
		Settings.alertCall = alertCall;
		updateSetting(KEY_ALERT_CALL, new Boolean(alertCall));
	}

	public static void setEmergencyNum(String emergencyNum) {
		Settings.emergencyNum = emergencyNum;
		updateSetting(KEY_ALERT_NUMS, emergencyNum);
	}

	public static void setAlertStationary(int alertStationary) {
		Settings.alertStationary = alertStationary;
		updateSetting(KEY_ALERT_STATIONARY, new Integer(alertStationary));
	}

	public static void setShieldOn(boolean shieldOn) {
		Settings.shieldOn = shieldOn;
		updateSetting(KEY_SHIELD, new Boolean(shieldOn));
	}

	public static void setAlertOn(boolean alertOn) {
		Settings.alertOn = alertOn;
		updateSetting(KEY_ALERT_BUTTON, new Boolean(alertOn));
	}
}

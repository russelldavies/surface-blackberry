package com.mmtechco.surface;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.StringUtilities;

public class Settings {
	// Settings keys and values
	public static long ID = StringUtilities.stringHashToLong(Application.getApplication() .getClass().getName());
	public static String KEY_GEN_SURFACE = "gen_surface";
	public static String KEY_GEN_SOUND = "gen_sound";
	public static String KEY_ALERT_CALL = "alert_call";
	public static String KEY_ALERT_NUMS = "emergencynums";
	public static String KEY_ALERT_STATIONARY = "alert_stationary";
	public static String KEY_SHIELD = "shield";
	public static String KEY_ALERT_BUTTON = "alertbutton";
	
	public static boolean genSurface;
	public static boolean genSound;
	
	public static boolean alertCall;
	public static Vector emergencyNums;
	public static int alertStationary;
	
	public static boolean shieldOn;
	public static boolean alertOn;
	
	public static void readSettings() {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			if (settingsTable == null) {
				// Populate with default values
				settingsTable = new Hashtable(7);
				settingsTable.put(KEY_GEN_SURFACE, new Boolean(true));
				settingsTable.put(KEY_GEN_SOUND, new Boolean(false));
				
				settingsTable.put(KEY_ALERT_CALL, new Boolean(false));
				settingsTable.put(KEY_ALERT_NUMS, new Vector());
				settingsTable.put(KEY_ALERT_STATIONARY, new Integer(5));
				
				settingsTable.put(KEY_SHIELD, new Boolean(false));
				settingsTable.put(KEY_ALERT_BUTTON, new Boolean(false));
				// Store
				settings.setContents(settingsTable);
				settings.commit();
			}
			genSurface = ((Boolean) settingsTable.get(KEY_GEN_SURFACE)).booleanValue();
			genSound = ((Boolean) settingsTable.get(KEY_GEN_SOUND)).booleanValue();
			
			alertCall = ((Boolean) settingsTable.get(KEY_ALERT_CALL)).booleanValue();
			emergencyNums = ((Vector) settingsTable.get(KEY_ALERT_NUMS));
			alertStationary = ((Integer) settingsTable.get(KEY_ALERT_STATIONARY)).intValue();
			
			shieldOn = ((Boolean) settingsTable.get(KEY_SHIELD)).booleanValue();
			alertOn = ((Boolean) settingsTable.get(KEY_ALERT_BUTTON)).booleanValue();
		}
	}
	
	public static void updateSettings(String key, boolean value) {
		PersistentObject settings = PersistentStore
				.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			settingsTable.put(key, new Boolean(value));
		}
	}
}

package com.mmtechco.surface;

import java.util.Hashtable;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.StringUtilities;

public class Settings {
	// Settings keys and values
	public static long ID = StringUtilities.stringHashToLong(Application.getApplication() .getClass().getName());
	public static String KEY_LOCKSCREEN = "lockscreen";
	public static String KEY_ALERTBUTTON = "alertbutton";
	
	public static boolean lockOn;
	public static boolean alertOn;
	
	public static void readSettings() {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			if (settingsTable == null) {
				// Populate with default values
				settingsTable = new Hashtable();
				settingsTable.put(KEY_LOCKSCREEN, new Boolean(false));
				settingsTable.put(KEY_ALERTBUTTON, new Boolean(false));
				// Store
				settings.setContents(settingsTable);
				settings.commit();
			}
			lockOn = ((Boolean) settingsTable.get(KEY_LOCKSCREEN))
					.booleanValue();
			alertOn = ((Boolean) settingsTable.get(KEY_ALERTBUTTON))
					.booleanValue();
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

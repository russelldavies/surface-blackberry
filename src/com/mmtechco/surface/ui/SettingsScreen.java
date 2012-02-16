package com.mmtechco.surface.ui;

import java.util.Hashtable;

import com.mmtechco.surface.ui.component.LabeledSwitch;
import com.mmtechco.surface.ui.container.JustifiedHorizontalFieldManager;

import net.rim.device.api.system.Application;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringUtilities;

public class SettingsScreen extends MainScreen {
	private static final long ID = StringUtilities
			.stringHashToLong(Application.getApplication().getClass().getName());
	
	private static String KEY_LOCKSCREEN = "lockscreen";
	private static String KEY_ALERTBUTTON = "alertbutton";
	
	private Boolean lockOn;
	private Boolean alertOn;

	public SettingsScreen() {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			if (settingsTable == null) {
				// Populate with default values
				settingsTable = new Hashtable();
				settingsTable.put(KEY_LOCKSCREEN, null);
				settingsTable.put(KEY_ALERTBUTTON, null);
				// Store
				settings.setContents(settingsTable);
				settings.commit();
			}
			lockOn = (Boolean) settingsTable.get(KEY_LOCKSCREEN);
			alertOn = (Boolean) settingsTable.get(KEY_ALERTBUTTON);
		}
		
		setTitle("Surface Settings");
		
		Bitmap switch_left = Bitmap.getBitmapResource("switch_left.png");
		Bitmap switch_right = Bitmap.getBitmapResource("switch_right.png");
		Bitmap switch_left_focus = Bitmap
				.getBitmapResource("switch_left_focus.png");
		Bitmap switch_right_focus = Bitmap
				.getBitmapResource("switch_right_focus.png");

		final LabeledSwitch lockSwitch = new LabeledSwitch(switch_left, switch_right,
				switch_left_focus, switch_right_focus, "on", "off", lockOn.booleanValue());
		JustifiedHorizontalFieldManager lockScreen = new JustifiedHorizontalFieldManager(
				new LabelField("LockScreen"), lockSwitch, false, USE_ALL_WIDTH);
		lockScreen.setPadding(5, 5, 5, 5);
		add(lockScreen);

		final LabeledSwitch alertSwitch = new LabeledSwitch(switch_left,
				switch_right, switch_left_focus, switch_right_focus, "on",
				"off", alertOn.booleanValue());
		JustifiedHorizontalFieldManager alertButton = new JustifiedHorizontalFieldManager(
				new LabelField("Alert Button"), alertSwitch, false,
				USE_ALL_WIDTH);
		alertButton.setPadding(5, 5, 5, 5);
		add(alertButton);
		
		FieldChangeListener listener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if(field == lockSwitch) {
					if(lockSwitch.getOnState()) {
						updateSettings(KEY_LOCKSCREEN, new Boolean(true));
					} else {
						updateSettings(KEY_LOCKSCREEN, new Boolean(false));
					}
				} else if (field == alertSwitch) {
					if(lockSwitch.getOnState()) {
						updateSettings(KEY_ALERTBUTTON, new Boolean(true));
					} else {
						updateSettings(KEY_ALERTBUTTON, new Boolean(false));
					}
				}
			}
		};
		lockSwitch.setChangeListener(listener);
		alertSwitch.setChangeListener(listener);
	}
	
	private void updateSettings(String key, Boolean value) {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			settingsTable.put(key, value);
		}
	}
}

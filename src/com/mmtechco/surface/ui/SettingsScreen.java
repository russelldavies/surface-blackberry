package com.mmtechco.surface.ui;

import java.util.Hashtable;

import com.mmtechco.surface.Surface;
import com.mmtechco.surface.ui.component.LabeledSwitch;
import com.mmtechco.surface.ui.container.JustifiedHorizontalFieldManager;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

public class SettingsScreen extends MainScreen {
	public SettingsScreen() {
		setTitle("Surface Settings");

		Bitmap switch_left = Bitmap.getBitmapResource("switch_left.png");
		Bitmap switch_right = Bitmap.getBitmapResource("switch_right.png");
		Bitmap switch_left_focus = Bitmap
				.getBitmapResource("switch_left_focus.png");
		Bitmap switch_right_focus = Bitmap
				.getBitmapResource("switch_right_focus.png");

		final LabeledSwitch lockSwitch = new LabeledSwitch(switch_left,
				switch_right, switch_left_focus, switch_right_focus, "on",
				"off", Surface.lockOn.booleanValue());
		JustifiedHorizontalFieldManager lockScreen = new JustifiedHorizontalFieldManager(
				new LabelField("LockScreen"), lockSwitch, false, USE_ALL_WIDTH);
		lockScreen.setPadding(5, 5, 5, 5);
		add(lockScreen);

		final LabeledSwitch alertSwitch = new LabeledSwitch(switch_left,
				switch_right, switch_left_focus, switch_right_focus, "on",
				"off", Surface.alertOn.booleanValue());
		JustifiedHorizontalFieldManager alertButton = new JustifiedHorizontalFieldManager(
				new LabelField("Alert Button"), alertSwitch, false,
				USE_ALL_WIDTH);
		alertButton.setPadding(5, 5, 5, 5);
		add(alertButton);

		FieldChangeListener listener = new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				if (field == lockSwitch) {
					updateSettings(
							Surface.KEY_LOCKSCREEN,
							Surface.lockOn = new Boolean(lockSwitch
									.getOnState()));
				} else if (field == alertSwitch) {
					updateSettings(
							Surface.KEY_ALERTBUTTON,
							Surface.alertOn = new Boolean(alertSwitch
									.getOnState()));
				}
			}
		};
		lockSwitch.setChangeListener(listener);
		alertSwitch.setChangeListener(listener);
	}

	private void updateSettings(String key, Boolean value) {
		PersistentObject settings = PersistentStore
				.getPersistentObject(Surface.ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			settingsTable.put(key, value);
		}
	}
}

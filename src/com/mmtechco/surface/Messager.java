package com.mmtechco.surface;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.RadioException;
import net.rim.device.api.ui.component.LabelField;

import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.MMTools;
import com.mmtechco.surface.ui.component.ActionButtonField;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

public class Messager {
	private static final String TAG = ToolsBB.getSimpleClassName(Messager.class);
	static ResourceBundle r = ResourceBundle.getBundle(
			SurfaceResource.BUNDLE_ID, SurfaceResource.BUNDLE_NAME);
	private static Logger logger = Logger.getInstance();
	private static MMTools tools = ToolsBB.getInstance();

	public static final String type_surface = "10";
	public static final String type_alert = "13";
	public static final String type_mandown = "15";

	private ActionButtonField button;
	private LabelField labelField;

	public Messager(ActionButtonField button, LabelField labelField) {
		this.button = button;
		this.labelField = labelField;
	}

	public void sendMessage(final String type) {
		String statusMsg = "Sending ";
		if (type.equals(type_surface)) {
			statusMsg = statusMsg + "Surface";
			button.setSurface();
		} else if (type.equals(type_alert)) {
			statusMsg = statusMsg + "Alert";
			button.setAlert();
			// Send an SMS to emergency numbers
			sendAlertSMS();
		} else if (type.equals(type_mandown)) {
			statusMsg = statusMsg + "Man Down";
			button.setManDown();
			// Make call to emergency number
			makeCall();
		}

		button.stopSpin();

		statusMsg = statusMsg + "...";
		// Save existing status before changing
		String prevStatus = labelField.getText();
		labelField.setText(statusMsg);

		// Spawn new thread so the event lock is not blocked
		new Thread() {
			public void run() {
				String queryString = Registration.getRegID()
						+ Tools.ServerQueryStringSeparator + type
						+ Tools.ServerQueryStringSeparator + tools.getDate()
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.latitude
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.longitude;
				logger.log(TAG, queryString);
				new Server().contactServer(queryString);
			}
		}.start();
		labelField.setText("Message Sent...");
		labelField.setText(prevStatus);
	}

	private void sendAlertSMS() {
		new Thread() {
			public void run() {
				String[] emergNums = Registration.getEmergNums();
				if (emergNums[0] != "" && emergNums.length > 0) {
					for (int i = 0; i < emergNums.length; i++) {
						try {
							((ToolsBB) ToolsBB.getInstance()).sendSMS(
									emergNums[i],
									r.getString(SurfaceResource.i18n_AlertMsg));
						} catch (Exception e) {
							logger.log(TAG, e.getMessage());
						}
					}
				}
			}
		}.start();
	}

	private void makeCall() {
		String[] emergNums = Registration.getEmergNums();
		if (emergNums[0] != "" && emergNums.length > 0) {
			try {
				Phone.initiateCall(Phone.getLineIds()[0], emergNums[0]);
			} catch (RadioException e) {
				logger.log(TAG, e.getMessage());
			}
		}
	}
}

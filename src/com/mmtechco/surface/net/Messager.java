package com.mmtechco.surface.net;

import java.util.Vector;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.RadioException;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.prototypes.MMTools;
import com.mmtechco.surface.ui.ToastPopupScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

public class Messager {
	private static final String TAG = ToolsBB
			.getSimpleClassName(Messager.class);
	static ResourceBundle r = ResourceBundle.getBundle(
			SurfaceResource.BUNDLE_ID, SurfaceResource.BUNDLE_NAME);
	private static Logger logger = Logger.getInstance();
	private static MMTools tools = ToolsBB.getInstance();

	public static final String type_surface = "10";
	public static final String type_alert = "13";
	public static final String type_mandown = "15";

	public static void sendMessage(final String type, String initialMessage) {
		final ToastPopupScreen screen = new ToastPopupScreen(initialMessage);
		Ui.getUiEngine().pushGlobalScreen(screen,
				Surface.SCREEN_PRIORITY_LOCKSCREEN, UiEngine.GLOBAL_SHOW_LOWER);
		
		// Spawn new thread so the event lock is not blocked
		new Thread() {
			public void run() {
				if (Server.isConnected()) {
					String queryString = Registration.getRegID()
							+ Tools.ServerQueryStringSeparator + type
							+ Tools.ServerQueryStringSeparator
							+ tools.getDate()
							+ Tools.ServerQueryStringSeparator
							+ LocationMonitor.latitude
							+ Tools.ServerQueryStringSeparator
							+ LocationMonitor.longitude;
					logger.log(TAG, queryString);
					String reply = Server.get(queryString);
					/*
					synchronized (UiApplication.getEventLock()) {
						if (reply.isError()) {
							screen.setText("Could not send message");
						} else {
							screen.setText("Message sent");
						}
						screen.dismiss(2000);
					}
					*/
				} else {
					synchronized (UiApplication.getEventLock()) {
						screen.setText("Please check your connectivity settings");
						screen.dismiss(2500);
					}
				}
			}
		}.start();
	}

	/**
	 * Send an SMS to emergency numbers
	 */
	public static void sendAlertSMS() {
		new Thread() {
			public void run() {
				Vector emergNums = Registration.getEmergNums();
				for (int i = 0; i < emergNums.size(); i++) {
					try {
						((ToolsBB) ToolsBB.getInstance()).sendSMS(
								(String) emergNums.elementAt(i),
								r.getString(SurfaceResource.i18n_AlertMsg));
					} catch (Exception e) {
						logger.log(TAG, e.getMessage());
					}
				}
			}
		}.start();
	}

	/**
	 * Make a call to the emergency number. If there are multiple numbers call
	 * the first.
	 */
	public static void makeCall() {
		Vector emergNums = Registration.getEmergNums();
		for (int i = 0; i < emergNums.size(); i++) {
			try {
				Phone.initiateCall(Phone.getLineIds()[0],
						(String) emergNums.elementAt(0));
			} catch (RadioException e) {
				logger.log(TAG, e.getMessage());
			}
		}
	}
}

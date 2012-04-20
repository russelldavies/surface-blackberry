package com.mmtechco.surface.net;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.RadioException;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.ui.ToastPopupScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class Messager {
	private static final String TAG = ToolsBB
			.getSimpleClassName(Messager.class);
	private static Logger logger = Logger.getInstance();
	
	public static final String STATE_NON = "NON", STATE_ALH = "ALH",
			STATE_SU1 = "SU1", STATE_SU2 = "SU2", STATE_MIS = "MIS",
			STATE_MNS = "MNS";
	
	//public static void sendMessage(String json, String toastMessage) {
	//public static void sendMessage(final String type, String toastMessage) {
	public static Response sendMessage(final String type, String toastMessage) {
		final ToastPopupScreen screen = new ToastPopupScreen(toastMessage);
		Ui.getUiEngine().pushGlobalScreen(screen,
				Surface.SCREEN_PRIORITY_LOCKSCREEN, UiEngine.GLOBAL_SHOW_LOWER);
		
		// Spawn new thread so the event lock is not blocked
		new Thread() {
			public void run() {
				if (Server.isConnected()) {
					synchronized (UiApplication.getEventLock()) {
						screen.setText("Please check your connectivity settings");
						screen.dismiss(2500);
					}
					return;
				}
				Response response;
				try {
					response = Server.post(new EventClientRequest(
							LocationMonitor.latitude, LocationMonitor.longitude,
							type).toJSON());
				} catch (IOException e) {
					logger.log(TAG, e.getMessage());
					return;
				}
				int rc = response.getResponseCode();
				synchronized (UiApplication.getEventLock()) {
					if (rc == HttpConnection.HTTP_OK) {
						screen.setText("Message sent");
					} else {
						// TODO: add logging here
						screen.setText("Could not send message");
					}
					screen.dismiss(2000);
				}
				
				processLog();
			}
		}.start();
		// TODO: fix me
		return null;
	}
	
	private static void processLog() {
		logger.log(TAG,
				"Checking for new messages to send. Message queue length: "
						+ ActivityLog.length());
		if (ActivityLog.hasNext()) {
			Object obj = ActivityLog.getMessage();
			if (obj instanceof EventClientRequest) {
				EventClientRequest a = (EventClientRequest) obj;
				sendMessage(Messager.STATE_ALH, "blah");
			}
		}
	}

	/**
	 * Send an SMS to emergency numbers
	 */
	public static void sendAlertSMS() {
		final ResourceBundle r = ResourceBundle.getBundle(
				SurfaceResource.BUNDLE_ID, SurfaceResource.BUNDLE_NAME);
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

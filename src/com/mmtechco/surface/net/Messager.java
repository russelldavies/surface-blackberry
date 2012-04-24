package com.mmtechco.surface.net;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.RadioException;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.data.MessageStore;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.ui.SurfaceScreen;
import com.mmtechco.surface.ui.ToastPopupScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class Messager {
	private static final String TAG = ToolsBB
			.getSimpleClassName(Messager.class);
	private static Logger logger = Logger.getInstance();

	public static final String STATE_NON = "NON", STATE_ALH = "ALH",
			STATE_SUR = "SUR", STATE_MIS = "MIS", STATE_MNS = "MNS";

	// Called by LocationMonitor and processLog()
	public static void sendMessage(final String type) {
		sendMessage(type, null);
	}

	// Called by others
	public static void sendMessage(final String type, String toastMessage) {
		final ToastPopupScreen toast = (toastMessage != null) ? new ToastPopupScreen(
				toastMessage) : null;
		if (toast != null) {
			Ui.getUiEngine().pushGlobalScreen(toast,
					Surface.SCREEN_PRIORITY_LOCKSCREEN,
					UiEngine.GLOBAL_SHOW_LOWER);
		}

		// Spawn new thread so the event lock is not blocked
		new Thread() {
			public void run() {
				Response response;
				try {
					response = Server.post(new EventRequest(
							LocationMonitor.latitude,
							LocationMonitor.longitude, type).toJSON());
				} catch (IOException e) {
					if (toast != null) {
						synchronized (UiApplication.getEventLock()) {
							toast.setText("Please check your connectivity settings");
							toast.dismiss(2500);
						}
					}
					logger.log(TAG, e.getMessage());
					return;
				}
				processResponse(type, response, toast);

				// Process the rest of the logs
				processLog();
			}
		}.start();
	}

	private static void processResponse(String type, Response response,
			ToastPopupScreen toast) {
		int rc = response.getResponseCode();
		logger.log(TAG, "Response code: " + rc);

		if (rc >= HttpConnection.HTTP_BAD_REQUEST
				&& rc < HttpConnection.HTTP_INTERNAL_ERROR) {
			// Malformed json
			logger.log(TAG, "Malformed json");
			return;
		} else if (rc >= HttpConnection.HTTP_INTERNAL_ERROR) {
			// Server error
			logger.log(TAG, "Server has internal error");
			return;
		}

		if (type.equals(STATE_NON) && rc == HttpConnection.HTTP_SEE_OTHER) {
			// HTTP 303 status code indicates to surface
			logger.log(TAG, "Server has requested surface");
			Application.getApplication().invokeLater(new Runnable() {
				public void run() {
					Ui.getUiEngine().pushGlobalScreen(new SurfaceScreen(),
							Surface.SCREEN_PRIORITY_SURFACE,
							UiEngine.GLOBAL_SHOW_LOWER);
				}
			});
		} else {
			if (toast != null) {
				synchronized (UiApplication.getEventLock()) {
					if (rc == HttpConnection.HTTP_OK) {
						toast.setText("Message sent");
					} else {
						// TODO: add logging here
						toast.setText("Could not send message");
					}
					toast.dismiss(2000);
				}
			}
		}
	}

	private static void processLog() {
		// TODO: this is highly incomplete
		logger.log(TAG,
				"Checking for new events to send: " + MessageStore.length());
		if (MessageStore.next() != null) {
			EventRequest obj = MessageStore.next();
			if (obj instanceof EventRequest) {
				EventRequest a = (EventRequest) obj;
				sendMessage(Messager.STATE_ALH);
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

package com.mmtechco.surface.message;

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
import com.mmtechco.surface.message.MessageStore;
import com.mmtechco.surface.net.Response;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.ui.SurfaceScreen;
import com.mmtechco.surface.ui.ToastPopupScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

public class Messager {
	private static final String TAG = ToolsBB .getSimpleClassName(Messager.class);
	private static Logger logger = Logger.getInstance();
	
	// Called by LocationMonitor and processLog()
	public static void sendMessage(Message message) {
		sendMessage(message, null);
	}

	// Called by others
	public static void sendMessage(final Message message, String toastMessage) {
		//MessageStore.pushMesage(message);
		
		final ToastPopupScreen toast = (toastMessage != null) ? new ToastPopupScreen( toastMessage) : null;
		if (toast != null) {
			Ui.getUiEngine().pushGlobalScreen(toast, Surface.SCREEN_PRIORITY_LOCKSCREEN, UiEngine.GLOBAL_SHOW_LOWER);
		}

		// Spawn new thread so the event lock is not blocked
		new Thread() {
			public void run() {
				Response response;
				try {
					response = Server.post(message.toJSON());
				} catch (IOException e) {
					if (toast != null) {
						synchronized (UiApplication.getEventLock()) {
							toast.setText("Please check your connectivity settings");
							toast.dismiss(2500);
						}
					}
					logger.log(TAG, e.getMessage());
					MessageStore.pushMesage(message);
					return;
				}
				
				processResponse(message, response, toast);

				// Process the rest of the messages
				// TODO: process again
				while(MessageStore.length() > 0) {
					Message message = MessageStore.popMessage();
					try {
						response = Server.post(message.toJSON());
						processResponse(message, response, null);
					} catch (IOException e) {
						MessageStore.pushMesage(message);
						return;
					}
				}
			}
		}.start();
	}

	private static void processResponse(Message message, Response response, ToastPopupScreen toast) {
		int rc = response.getResponseCode();
		logger.log(TAG, "Response code: " + rc);
		
		if (rc >= HttpConnection.HTTP_BAD_REQUEST && rc < HttpConnection.HTTP_INTERNAL_ERROR) {
			// HTTP 400: malformed request so remove it from store
			logger.log(TAG, "Malformed request, discarding message");
		} else if (rc >= HttpConnection.HTTP_INTERNAL_ERROR) {
			// HTTP 500: Server error. Keep message and try again later
			logger.log(TAG, "Server error, adding message to store");
			MessageStore.pushMesage(message);
		}
		
		// Special processing is required for Event Messages
		if (!EventMessage.class.isInstance(message)) {
			return;
		}
		// If event is location update and status code is 303 pop surface
		// screen
		String state = ((EventMessage) message).getState();
		if (state.equals(EventMessage.STATE_NON) && rc == HttpConnection.HTTP_SEE_OTHER) {
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

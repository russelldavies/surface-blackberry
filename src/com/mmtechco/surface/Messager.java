package com.mmtechco.surface;

import net.rim.blackberry.api.phone.Phone;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.RadioException;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.MMTools;
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

	public static void sendMessage(final String type) {
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
	}

	/**
	 * Send an SMS to emergency numbers
	 */
	public static void sendAlertSMS() {
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

	/**
	 * Make a call to the emergency number. If there are multiple numbers call
	 * the first.
	 */
	public static void makeCall() {
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

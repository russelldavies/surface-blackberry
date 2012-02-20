//#preprocess
package com.mmtechco.surface.monitor;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.LocationListener;
import javax.microedition.location.LocationProvider;

import net.rim.device.api.gps.BlackBerryCriteria;
import net.rim.device.api.gps.BlackBerryLocationProvider;
import net.rim.device.api.gps.GPSInfo;
//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0 | VER_5.0.0
import net.rim.device.api.gps.LocationInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
//#endif

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.surface.net.Messager;
import com.mmtechco.surface.net.Reply;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.Message;
import com.mmtechco.surface.ui.SurfaceScreen;
import com.mmtechco.util.ErrorMessage;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors and registers location based events.
 */
public class LocationMonitor implements LocationListener {
	private static final String TAG = ToolsBB
			.getSimpleClassName(LocationMonitor.class);

	private Logger logger = Logger.getInstance();

	// Represents the period of the position query, in seconds
	private static int interval = 5;
	// Upload interval (in milliseconds)
	private static int uploadInterval = 30 * 1000;

	private BlackBerryLocationProvider locationProvider;

	public static double latitude;
	public static double longitude;
	private Message locMsg;
	
	private Server server;
	
	private static Vector observers = new Vector();

	public LocationMonitor() throws LocationException {
		//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0 | VER_5.0.0
		// Enable location services
		if (LocationInfo.getAvailableLocationSources() != 0) {
			LocationInfo.setLocationOn();
		//#else
        if (LocationProvider.getInstance(null) == null) {
        //#endif
			// Attempt to start the listening thread
			if (startLocationUpdate()) {
				logger.log(TAG,
						"Location status: " + locationProvider.getState());
			}
		} else {
			logger.log(TAG, "Could not start location services");
			return;
		}

		server = new Server();

		// Initialize lat/long
		latitude = 0;
		longitude = 0;

		// Upload location periodically
		new Timer().scheduleAtFixedRate(new UploadTask(), 0, uploadInterval);
	}

	public boolean startLocationUpdate() {
		boolean started = false;

		try {
			BlackBerryCriteria criteria = new BlackBerryCriteria(
					GPSInfo.GPS_MODE_ASSIST);
			//#ifndef VER_4.5.0 | VER_4.6.0 | VER_4.6.1 | VER_4.7.0 | VER_5.0.0
			criteria.enableGeolocationWithGPS();
			//#endif
			criteria.setFailoverMode(GPSInfo.GPS_MODE_AUTONOMOUS, 3, 100);
			// criteria.setSubsequentMode(GPSInfo.GPS_MODE_CELLSITE);

			criteria.setHorizontalAccuracy(5);
			criteria.setVerticalAccuracy(5);
			criteria.setPreferredPowerConsumption(Criteria.POWER_USAGE_MEDIUM);
			criteria.setPreferredResponseTime(uploadInterval - 1000);

			locationProvider = (BlackBerryLocationProvider) LocationProvider
					.getInstance(criteria);

			if (locationProvider != null) {
				/*
				 * Only a single listener can be associated with a provider, and
				 * unsetting it involves the same call but with null. Therefore,
				 * there is no need to cache the listener instance request an
				 * update every second.
				 */
				locationProvider.setLocationListener(this, interval, -1, -1);
				started = true;
			} else {
				logger.log(TAG, "Failed to obtain a location provider.");
			}
		} catch (final LocationException le) {
			logger.log(TAG, "Failed to instantiate LocationProvider object:"
					+ le.toString());
		}
		return started;
	}

	public void locationUpdated(LocationProvider provider, Location location) {
		// Polls GPS service based on interval specified in constructor and
		// upload to the server.
		if (location.isValid()) {
			float speed = location.getSpeed();
			longitude = location.getQualifiedCoordinates().getLongitude();
			latitude = location.getQualifiedCoordinates().getLatitude();
			locMsg = new LocationMessage(latitude, longitude, speed);
		}
	}

	public void providerStateChanged(LocationProvider provider, int newState) {
		logger.log(TAG, "GPS Provider changed");
		if (newState == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			provider.reset();
		}
	}
	
	private class UploadTask extends TimerTask {
		public void run() {
			// Check there are valid values
			if (longitude != 0 && latitude != 0) {
				logger.log(TAG, "Sending location to server");
				Reply reply = server.contactServer(locMsg.getREST());
				if (reply.getCallingCode().equals(Messager.type_surface)) {
					logger.log(TAG, "Server has requested surface");
					UiApplication.getUiApplication().pushGlobalScreen(new SurfaceScreen(), -1000, UiEngine.GLOBAL_MODAL);
				}
			}
		}
	}
}

/**
 * Holds GPS messages
 */
class LocationMessage implements Message {
	private final int type = 6;
	private double latitude, longitude;
	private String deviceTime;
	private float speed;

	public LocationMessage(double lat, double lon, float speed) {
		latitude = lat;
		longitude = lon;
		this.speed = speed;
		deviceTime = ToolsBB.getInstance().getDate();
	}

	/**
	 * Retrieves the message formatted in to a single string value. Location
	 * message consists of:
	 * <ul>
	 * <li>Registration Serial number.
	 * <li>Location message type which is '06' (two digits number).
	 * <li>Device time.
	 * <li>Latitude.
	 * <li>Longitude.
	 * <li>Speed. *<i>Warning</i> not implemented
	 * </ul>
	 * 
	 * @return a single string containing the entire message.
	 */
	public String getREST() {
		return Registration.getRegID() + Tools.ServerQueryStringSeparator + '0'
				+ type + Tools.ServerQueryStringSeparator + deviceTime
				+ Tools.ServerQueryStringSeparator + latitude
				+ Tools.ServerQueryStringSeparator + longitude
		// + Tools.ServerQueryStringSeparator + speed;
				;
	}

	public String getTime() {
		return deviceTime;
	}

	public int getType() {
		return type;
	}
}
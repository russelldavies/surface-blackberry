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
//#endif

import com.mmtechco.surface.messages.EventMessage;
import com.mmtechco.surface.messages.Messager;
import com.mmtechco.util.Logger;
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
			longitude = location.getQualifiedCoordinates().getLongitude();
			latitude = location.getQualifiedCoordinates().getLatitude();
		} else {
			longitude = 0;
			latitude = 0;
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
			logger.log(TAG, "Sending location to server");
			Messager.sendMessage(EventMessage.STATE_NON);
		}
	}
}
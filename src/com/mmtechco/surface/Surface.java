//#preprocess
package com.mmtechco.surface;

import java.util.Hashtable;

import javax.microedition.location.LocationException;

import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.monitor.LockKeyListener;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.Controllable;
import com.mmtechco.surface.ui.DefaultScreen;
//#ifdef DEBUG
import com.mmtechco.surface.ui.DebugScreen;
//#endif
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.util.StringUtilities;

/**
 * Main entry point of the application.
 */
public class Surface extends UiApplication implements SystemListener2 {
	private static final String TAG = "App";
	public static ResourceBundle r = ResourceBundle.getBundle(
			SurfaceResource.BUNDLE_ID, SurfaceResource.BUNDLE_NAME);
	private Logger logger = Logger.getInstance();
	
	// Settings keys and values
	public static long ID;
	public static String KEY_LOCKSCREEN = "lockscreen";
	public static String KEY_ALERTBUTTON = "alertbutton";
	public static  Boolean lockOn;
	public static  Boolean alertOn;
	
	// Global screen priorities; lower is higher priority
	public static int SCREEN_PRIORITY_SURFACE = 1;
	public static int SCREEN_PRIORITY_LOCKSCREEN = 2;

	private DefaultScreen defaultScreen;
	private Registration reg;

	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Alternate entry point arguments.
	 */
	public static void main(String[] args) {
		// Start logging
		// TODO: implement
		// Logger.startEventLogger();

		Surface app = new Surface();

		// If system startup is still in progress when this
		// application is run.
		if (ApplicationManager.getApplicationManager().inStartup()) {
			// Add a system listener to detect when system is ready and
			// available.
			app.addSystemListener(app);
		} else {
			// System is already ready and available so perform start up
			// work now. Note that this work must be completed using
			// invokeLater because the application has not yet entered the
			// event dispatcher.
			app.initializeLater();
		}

		// Listen for button presses
		app.addKeyListener(new LockKeyListener());
		
		// Load font
		FontManager.getInstance().load("kabel.ttf", "Kabel Dm BT",
				FontManager.APPLICATION_FONT);

		// Start event thread
		app.enterEventDispatcher();
	}

	private void initialize() {
		ID = StringUtilities.stringHashToLong(Application.getApplication()
				.getClass().getName());
		readSettings();
		
		//#ifdef DEBUG
		pushScreen(new DebugScreen());
		//#else
		defaultScreen = new DefaultScreen();
		pushScreen(defaultScreen);
		//#endif

		logger.log(TAG, "Starting registration");
		reg = new Registration();
		reg.start();
	}

	/**
	 * Start components
	 */
	public void startComponents() {
		// Register application indicator
		//alertscreen.registerIndicator();

		// Start monitors
		logger.log(TAG, "Starting monitors...");
		try {
			new LocationMonitor();
		} catch (LocationException e) {
			logger.log(TAG, e.getMessage());
		}

		Controllable[] components = new Controllable[1];
		components[0] = reg;
		new Commander(components).start();

		// Monitor activity log
		new Server().start();
	}

	public void readSettings() {
		PersistentObject settings = PersistentStore.getPersistentObject(ID);
		synchronized (settings) {
			Hashtable settingsTable = (Hashtable) settings.getContents();
			if (settingsTable == null) {
				// Populate with default values
				settingsTable = new Hashtable();
				settingsTable.put(KEY_LOCKSCREEN, new Boolean(false));
				settingsTable.put(KEY_ALERTBUTTON, new Boolean(false));
				// Store
				settings.setContents(settingsTable);
				settings.commit();
			}
			lockOn = (Boolean) settingsTable.get(KEY_LOCKSCREEN);
			alertOn = (Boolean) settingsTable.get(KEY_ALERTBUTTON);
		}
	}
	
	/**
	 * Perform the start up work on a new Runnable using the invokeLater
	 * construct to ensure that it is executed after the event thread has been
	 * created.
	 */
	private void initializeLater() {
		invokeLater(new Runnable() {
			public void run() {
				initialize();
			}
		});
	}

	public void powerUp() {
		Logger.getInstance().log(TAG, "Started from powerup");
		//removeSystemListener(this);
		initialize();
	}

	public void powerOff() {
	}

	public void rootChanged(int state, String rootName) {
	}

	public void backlightStateChange(boolean on) {
		// TODO: lock screen stuff here
	}

	public void batteryLow() {
	}

	public void batteryStatusChange(int status) {
	}

	public void cradleMismatch(boolean mismatch) {
	}

	public void fastReset() {
	}

	public void powerOffRequested(int reason) {
	}

	public void usbConnectionStateChange(int state) {
	}

	public void batteryGood() {
	}
}
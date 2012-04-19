//#preprocess
package com.mmtechco.surface;

import java.util.Hashtable;

import javax.microedition.location.LocationException;

import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.monitor.LockKeyListener;
import com.mmtechco.surface.net.Server;
//#ifdef DEBUG
import com.mmtechco.surface.ui.DebugScreen;
//#else
import com.mmtechco.surface.ui.DefaultScreen;
//#endif
//#ifdef TOUCH
import com.mmtechco.surface.ui.TouchLockScreen;
//#else
import com.mmtechco.surface.ui.KeypadLockScreen;
//#endif
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.SystemListener2;
import net.rim.device.api.ui.FontManager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.util.StringUtilities;

/**
 * Main entry point of the application.
 */
public class Surface extends UiApplication implements SystemListener2, GlobalEventListener {
	private static final String TAG = "App";
	public static ResourceBundle r = ResourceBundle.getBundle(
			SurfaceResource.BUNDLE_ID, SurfaceResource.BUNDLE_NAME);
	private Logger logger = Logger.getInstance();
	
	// Settings keys and values
	public static long ID;
	public static String KEY_LOCKSCREEN = "lockscreen";
	public static String KEY_ALERTBUTTON = "alertbutton";
	public static boolean lockOn;
	public static boolean alertOn;
	
	// Global screen priorities; lower is higher priority
	public static int SCREEN_PRIORITY_SURFACE = 1;
	public static int SCREEN_PRIORITY_LOCKSCREEN = 2;

	private Screen lockscreen;

	/**
	 * Entry point for application
	 * 
	 * @param args
	 *            Alternate entry point arguments.
	 */
	public static void main(String[] args) {
		final Surface app = new Surface();
		
		// Detect when system is ready to startup and also to monitor backlight
		// for lockscreen
		app.addSystemListener(app);
		// Listen for registration events
		app.addGlobalEventListener(app);
		
		//#ifdef DEBUG
		// Start logging if in debugging mode
		Logger.startEventLogger();
		//#endif

		// Load font
		FontManager.getInstance().load("kabel.ttf", "Kabel Dm BT",
				FontManager.APPLICATION_FONT);
		
		if (!ApplicationManager.getApplicationManager().inStartup()) {
			// System is already ready and available so perform start up
			// work now, otherwise it's called from powerup().
			// Note that this work must be completed using invokeLater because
			// the application has not yet entered the event dispatcher.
			app.invokeLater(new Runnable() {
				public void run() {
					app.initialize();
				}
			});
		}

		// Start event thread
		app.enterEventDispatcher();
	}

	/**
	 * Start the screens and ask for registration details. Other app components
	 * are started if registration is successful
	 */
	private void initialize() {
		ID = StringUtilities.stringHashToLong(Application.getApplication()
				.getClass().getName());
		readSettings();
		
		//#ifdef TOUCH
		lockscreen = new TouchLockScreen();
		//#else
		lockscreen = new KeypadLockScreen();
		//#endif

		// Listen for button presses
		KeyListener lockkeyListener = new LockKeyListener(lockscreen);
		addKeyListener(lockkeyListener);
		
		//#ifdef DEBUG
		pushScreen(new DebugScreen(lockkeyListener));
		//#else
		DefaultScreen defaultScreen = new DefaultScreen();
		pushScreen(defaultScreen);
		//#endif
		
		Registration.checkStatus();
	}
	
	public void eventOccurred(long guid, int data0, int data1, Object object0,
			Object object1) {
		if (guid == Registration.ID) {
			logger.log(TAG, "Received event to start components");
			// TODO: enable
			//startComponents();
		}
	}
	
	/**
	 * Start the rest of the components, such as monitors, if registration is
	 * successful.
	 */
	public void startComponents() {
		// Register application indicator
		//defaultScreen.registerIndicator();

		// Start monitors
		logger.log(TAG, "Starting monitors...");
		try {
			new LocationMonitor();
		} catch (LocationException e) {
			logger.log(TAG, e.getMessage());
		}

		// Monitor activity log
		new Server().start();
	}
	
	private void readSettings() {
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
			lockOn = ((Boolean) settingsTable.get(KEY_LOCKSCREEN))
					.booleanValue();
			alertOn = ((Boolean) settingsTable.get(KEY_ALERTBUTTON))
					.booleanValue();
		}
	}

	public void powerUp() {
		Logger.getInstance().log(TAG, "Started from powerup");
		// The system has finished booting so start app initialization
		initialize();
	}
	
	public void backlightStateChange(boolean on) {
		// Display lockscreen when display turns off
		if (lockOn && !on) {
			if (!lockscreen.isDisplayed()) {
				pushGlobalScreen(lockscreen, SCREEN_PRIORITY_LOCKSCREEN,
						UiEngine.GLOBAL_SHOW_LOWER);
			}
		}
	}

	public void powerOff() {
	}
	public void rootChanged(int state, String rootName) {
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
//#preprocess
package com.mmtechco.surface.ui;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import com.mmtechco.surface.Registration;
import com.mmtechco.surface.Surface;
import com.mmtechco.surface.message.EventMessage;
import com.mmtechco.surface.message.MessageStore;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.util.Logger;
import com.mmtechco.util.Tools;
import com.mmtechco.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngine;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * Screen that displays all the logging info. Useful for debugging. Enable by
 * enabling the DEBUG preprocessor directive.
 */
public class DebugScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private LabelField statusLabelField = new LabelField("", Field.NON_FOCUSABLE);

	private KeyListener keyListener;
	
	public DebugScreen(KeyListener keyListener) {
		this.keyListener = keyListener;
		
		Registration.addObserver(this);
		Logger.addObserver(this);

		add(statusLabelField);
		add(new SeparatorField());
	}

	/*
	 * Update the screen label fields
	 */
	public void setStatus(final String status) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				statusLabelField.setText(status);
			}
		});
	}

	public String getStatus() {
		return statusLabelField.getText();
	}

	public void surface() {
		addNewLog("Received Surface message from server");
	}

	/*
	 * Add a new log event to the screen
	 */
	public void addNewLog(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				add(new LabelField(msg, Field.FOCUSABLE));
			}
		});
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem clearMenu = new MenuItem("Clear Screen", 0x100020, 1) {
			public void run() {
				deleteAll();
			}
		};
		MenuItem delRegMenu = new MenuItem("View Registration info",
				0x100030, 2) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(
						new RegPopupScreen());
			}
		};
		MenuItem delStoreMenu = new MenuItem("Delete Activity Log store",
				0x100040, 3) {
			public void run() {
				PersistentStore.destroyPersistentObject(MessageStore.ID);
				System.exit(0);
			}
		};
		MenuItem surfaceMenu = new MenuItem("Send Surface", 0x100040, 3) {
			public void run() {
				sendMessage(EventMessage.STATE_SUR);
			}
		};
		MenuItem alertMenu = new MenuItem("Send Alert", 0x100040, 3) {
			public void run() {
				sendMessage(EventMessage.STATE_ALH);
			}
		};
		MenuItem mandownMenu = new MenuItem("Send Man Down", 0x100040, 3) {
			public void run() {
				sendMessage(EventMessage.STATE_MNS);
			}
		};
		MenuItem alertscreenMenu = new MenuItem("Launch Default Screen", 0x100040,
				3) {
			public void run() {
				ObserverScreen defaultScreen = new DefaultScreen();
				Registration.addObserver(defaultScreen);
				defaultScreen.setStatus(statusLabelField.getText());
				Ui.getUiEngine().pushScreen((Screen)defaultScreen);
			}
		};
		MenuItem lockscreenMenu = new MenuItem("Launch Lock Screen", 0x100040,
				3) {
			public void run() {
				//#ifdef TOUCH
				Ui.getUiEngine().pushScreen(new TouchLockScreen());
				//#else
				Ui.getUiEngine().pushScreen(new KeypadLockScreen());
				//#endif
			}
		};
		MenuItem simulateSurfaceMenu = new MenuItem("Simulate Receiving a Surface", 0x100040,
				3) {
			public void run() {
				Dialog.inform("A Surface screen will display after one minute");
				new Timer().schedule(new TimerTask() {
					public void run() {
						Ui.getUiEngine().pushGlobalScreen(new SurfaceScreen(),
								Surface.SCREEN_PRIORITY_SURFACE,
								UiEngine.GLOBAL_SHOW_LOWER);
					}
				}, 60 * 1000);
			}
		};

		menu.add(clearMenu);
		menu.add(delRegMenu);
		menu.add(delStoreMenu);
		menu.add(surfaceMenu);
		menu.add(alertMenu);
		menu.add(mandownMenu);
		menu.add(alertscreenMenu);
		menu.add(lockscreenMenu);
		menu.add(simulateSurfaceMenu);

		super.makeMenu(menu, instance);
	}

	private void sendMessage(final String type) {
		// Spawn new thread so the event lock is not blocked
		(new Thread() {
			public void run() {
				String queryString = Registration.getRegID()
						+ Tools.ServerQueryStringSeparator + type
						+ Tools.ServerQueryStringSeparator
						+ ToolsBB.getInstance().getDate()
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.latitude
						+ Tools.ServerQueryStringSeparator
						+ LocationMonitor.longitude;
				addNewLog(queryString);
				Server.get(queryString);
			}
		}).start();
	}

	public void close() {
		Application app = Application.getApplication();
		app.removeSystemListener((SystemListener) app);
		app.removeKeyListener(keyListener);
		super.close();
	}

	final class RegPopupScreen extends PopupScreen {

		public RegPopupScreen() {
			super(new VerticalFieldManager());

			add(new LabelField("Registration Information"));
			add(new SeparatorField());

			PersistentObject regData = PersistentStore
					.getPersistentObject(Registration.ID);
			synchronized (regData) {
				Hashtable regTable = (Hashtable) regData.getContents();
				if (regTable == null) {
					add(new LabelField("No values were in the store"));
				} else {
					String stage = (String) regTable.get(Registration.KEY_STAGE);
					String id = (String) regTable.get(Registration.KEY_ID);
					Boolean compStatus = (Boolean) RuntimeStore.getRuntimeStore().get(Registration.ID);
					
					add(new LabelField("Stage: " + stage));
					add(new LabelField("ID: " + id.toString()));
					add(new LabelField("Components started: " + compStatus));
				}
			}

			ButtonField exitButton = new ButtonField("Delete info and exit",
					ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			exitButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
					// Delete details
					PersistentStore.destroyPersistentObject(Registration.ID);
					RuntimeStore.getRuntimeStore().remove(Registration.ID);
					System.exit(0);
				}
			});
			add(exitButton);
		}

		/**
		 * Overrides the default implementation. Closes the popup screen when
		 * the Escape key is pressed.
		 * 
		 * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
		 */
		public boolean keyChar(char c, int status, int time) {
			if (c == Characters.ESCAPE) {
				close();
				return true;
			}

			return super.keyChar(c, status, time);
		}
	}
}
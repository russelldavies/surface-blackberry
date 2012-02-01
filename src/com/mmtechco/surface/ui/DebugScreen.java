package com.mmtechco.surface.ui;

import java.util.Hashtable;

import com.mmtechco.surface.Registration;
import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.surface.monitor.LocationMonitor;
import com.mmtechco.surface.net.Server;
import com.mmtechco.surface.prototypes.ObserverScreen;
import com.mmtechco.surface.util.Constants;
import com.mmtechco.surface.util.Logger;
import com.mmtechco.surface.util.SurfaceResource;
import com.mmtechco.surface.util.Tools;
import com.mmtechco.surface.util.ToolsBB;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.StringProvider;

/**
 * Screen that displays all the logging info. Useful for debugging. Enable by
 * setting the global debug flag in {@link Constants#DEBUG}
 */
public class DebugScreen extends MainScreen implements ObserverScreen,
		SurfaceResource {
	static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

	// GUI widgets
	private TextField statusTextField = new TextField(Field.NON_FOCUSABLE);
	private TextField idTextField = new TextField(Field.NON_FOCUSABLE);

	public DebugScreen() {
		Registration.addObserver(this);
		Logger.addObserver(this);

		// Add label fields with no layout managers
		statusTextField.setLabel("Status: ");
		statusTextField.setText(r.getString(i18n_RegRequesting));
		idTextField.setLabel("ID: ");
		idTextField.setText("[none]");

		add(statusTextField);
		add(idTextField);
		add(new SeparatorField());
	}

	/*
	 * Update the screen label fields
	 */
	public void update() {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				// Leave label blank if reg id doesn't yet exist
				String regId = Registration.getRegID();
				if (!regId.equals("0")) {
					idTextField.setText(regId);
				}
				statusTextField.setText(Registration.getStatus());
			}
		});
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
				add(new LabelField(msg));
			}
		});
	}

	protected void makeMenu(Menu menu, int instance) {
		MenuItem clearMenu = new MenuItem(new StringProvider("Clear Screen"),
				0x100020, 1) {
			public void run() {
				deleteAll();
			}
		};

		MenuItem delRegMenu = new MenuItem(new StringProvider(
				"Delete Registration info"), 0x100030, 2) {
			public void run() {
				UiApplication.getUiApplication().pushScreen(
						new RegPopupScreen());
				PersistentStore.destroyPersistentObject(Registration.ID);
			}
		};

		MenuItem delStoreMenu = new MenuItem(new StringProvider(
				"Delete Activity Log store"), 0x100040, 3) {
			public void run() {
				PersistentStore.destroyPersistentObject(ActivityLog.ID);
				System.exit(0);
			}
		};

		MenuItem surfaceMenu = new MenuItem(new StringProvider("Surface"),
				0x100040, 3) {
			public void run() {
				sendMessage(Constants.type_surface);
			}
		};

		MenuItem alertMenu = new MenuItem(new StringProvider("Alert"), 0x100040, 3) {
			public void run() {
				sendMessage(Constants.type_alert);
			}
		};
		
		MenuItem mandownMenu = new MenuItem(new StringProvider("Man Down"), 0x100040, 3) {
			public void run() {
				sendMessage(Constants.type_mandown);
			}
		};

		menu.add(clearMenu);
		menu.add(delRegMenu);
		menu.add(delStoreMenu);
		menu.add(surfaceMenu);
		menu.add(alertMenu);
		menu.add(mandownMenu);

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
				new Server().contactServer(queryString);
			}
		}).start();
	}

	public void close() {
		// TODO: remove filesystem listener, remove call and sms listeners.
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
					add(new LabelField(regTable.get(Registration.KEY_STAGE)));
					add(new LabelField(regTable.get(Registration.KEY_ID)));
					String num = (String) regTable
							.get(Registration.KEY_NUMBERS);
					if (num.equals("")) {
						add(new LabelField("No emergency numbers stored"));
					} else {
						add(new LabelField(num));
					}
				}
			}

			ButtonField exitButton = new ButtonField("Exit",
					ButtonField.FIELD_HCENTER | ButtonField.CONSUME_CLICK);
			exitButton.setChangeListener(new FieldChangeListener() {
				public void fieldChanged(Field field, int context) {
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
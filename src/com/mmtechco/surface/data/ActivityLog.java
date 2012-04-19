package com.mmtechco.surface.data;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

public class ActivityLog {
	public static final long ID = StringUtilities
			.stringHashToLong(ActivityLog.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector log;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			log = new ContentProtectedVector();
			store.setContents(log);
		}
		log = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addMessage(Object jsonObj) {
		log.addElement(jsonObj);
		commit();
	}

	public static synchronized boolean removeMessage() {
		if (hasNext()) {
			log.removeElementAt(0);
			commit();
			return true;
		}
		return false;
	}

	public static synchronized Object getMessage() {
		if (hasNext()) {
			return log.firstElement();
		}
		return null;
	}

	public static synchronized boolean hasNext() {
		if (log.size() > 0) {
			return true;
		}
		return false;
	}

	public static synchronized int length() {
		return log.size();
	}

	private static void commit() {
		store.setContents(log);
		store.commit();
	}
}

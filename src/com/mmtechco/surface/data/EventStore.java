package com.mmtechco.surface.data;

import com.mmtechco.surface.net.EventRequest;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

public class EventStore {
	public static final long ID = StringUtilities
			.stringHashToLong(EventStore.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector events;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			events = new ContentProtectedVector();
			store.setContents(events);
		}
		events = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addEvent(EventRequest event) {
		events.addElement(event);
		commit();
	}

	public static synchronized boolean removeEvent() {
		if (hasNext()) {
			events.removeElementAt(0);
			commit();
			return true;
		}
		return false;
	}

	public static synchronized EventRequest next() {
		if (hasNext()) {
			return (EventRequest) events.firstElement();
		}
		return null;
	}

	private static synchronized boolean hasNext() {
		if (events.size() > 0) {
			return true;
		}
		return false;
	}

	public static synchronized int length() {
		return events.size();
	}

	private static void commit() {
		store.setContents(events);
		store.commit();
	}
}

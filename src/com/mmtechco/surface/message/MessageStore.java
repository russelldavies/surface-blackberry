package com.mmtechco.surface.message;

import java.util.Enumeration;
import java.util.Vector;


import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.util.ContentProtectedVector;
import net.rim.device.api.util.StringUtilities;

/**
 * Simple stack to hold messages (events, commands, logs, usage, errors). It is
 * persistent across device resets.
 */
class MessageStore {
	public static final long ID = StringUtilities
			.stringHashToLong(MessageStore.class.getName());

	private static PersistentObject store;
	private static ContentProtectedVector messages;

	static {
		store = PersistentStore.getPersistentObject(ID);
		if (store.getContents() == null) {
			messages = new ContentProtectedVector();
			store.setContents(messages);
		}
		messages = (ContentProtectedVector) store.getContents();
	}

	public static synchronized void addEvent(EventMessage event) {
		messages.insertElementAt(event, 0);
		commit();
	}
	
	public static synchronized void addLog(LogObject log) {
		messages.addElement(log);
		commit();
	}
	
	public static synchronized void addError(ErrorObject error) {
		messages.addElement(error);
		commit();
	}

	public static synchronized EventMessage getEvent() {
		if (!messages.isEmpty()) {
			Object message = messages.firstElement();
			if (message instanceof EventMessage) {
				messages.removeElement(message);
				commit();
				return (EventMessage) message;
			}
		}
		return null;
	}
	
	public static synchronized LogMessage getLog() {
		return new LogMessage(getCollection(LogObject.class));
	}
	
	public static synchronized ErrorMessage getError() {
		return new ErrorMessage(getCollection(ErrorObject.class));
	}
	
	private static synchronized Vector getCollection(Class c) {
		Vector v = new Vector();
		for (Enumeration e = messages.elements(); e.hasMoreElements();){
			Object obj = e.nextElement();
			if (c.isInstance(obj)) { 
				v.addElement(obj);
				messages.removeElement(obj);
			}
		}
		return v;
	}

	public static synchronized int length() {
		return messages.size();
	}

	private static void commit() {
		store.setContents(messages);
		store.commit();
	}
}

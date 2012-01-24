package com.mmtechco.surface.util;

import net.rim.device.api.system.Application;
import net.rim.device.api.util.StringUtilities;

public class Constants {
	public static final String APP_NAME = "Surface";
	public static final int APP_VERSION = 1;
	public static final long GUID = StringUtilities.stringHashToLong(Application.getApplication().getClass().getName());
	
	// Global flag to turn on debugging features
	public static final boolean DEBUG = true;
	
	// Types
	public static final String type_surface = "10";
}

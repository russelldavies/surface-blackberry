package com.mmtechco.surface.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.system.DeviceInfo;

import com.mmtechco.surface.data.ActivityLog;
import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors for new actions stored in the local storage for recording actions
 * and sends them to the web server at specific intervals.
 */
public class Server extends Thread {
	private static final String TAG = ToolsBB.getSimpleClassName(Server.class);

	private Logger logger = Logger.getInstance();
	private static final String URL = "http://192.168.2.13/surface_sys/REST.php";
	private static final String PROTOCOL_VER = "1";
	private int freq = 1000 * 30; // 30 seconds


	/**
	 * Monitors the local storage for new messages stored at specific intervals
	 * and sends them to the server.
	 */
	public void run() {
		while (true) {
			if (isConnected()) {
				logger.log(TAG,
						"Checking for new messages to send. Message queue length: "
								+ ActivityLog.length());
				// Check is a message is in the local storage
				while (ActivityLog.length() > 0 && isConnected()) {
					/*
					try {
						// Send the first message from the queue to the server
						// and parse reply
						serverReply = tools.split( get(ActivityLog.getMessage()), Tools.ServerQueryStringSeparator);
						// No error
						if (serverReply.length > 2
								&& (Integer.parseInt(serverReply[2]) == 0)) {
							// Pop the message off the queue
							ActivityLog.removeMessage();
							counter = -1;
						}
					} catch (NullPointerException e) {
						logger.log(TAG, "Could not contact server.");
					}

					if (null == serverReply) {
						logger.log(TAG,
								"Did not receive server reply, sleeping");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} else {
						// Check network: server timeout, does not return type
						if (0 < serverReply[1].length()) {
							counter++;
							if (counter == 2) {
								ActivityLog.removeMessage();
								counter = -1;
							}
						} else {
							// No network
							ActivityLog.removeMessage();
							break;
						}
					}
					*/
				}
			}
			// Sleep so loop doesn't spin
			try {
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static String get(String queryString) {
		//logger.log(TAG, "GET query string: " + queryString);
		try {
			HttpConnection connection = setupConnection(URL + queryString);
			connection.setRequestMethod(HttpConnection.GET);

			int status = connection.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return new String(reply);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//logger.log(TAG, e.toString());
			return null;
		}
	}

	public static String post(String messageBody) {
		if (!isConnected()) {
			return null;
		}
		Logger.getInstance().log(TAG, "POST data" + messageBody);
		try {
			// Setup connection and HTTP headers
			HttpConnection connection = setupConnection(URL);
			connection.setRequestMethod(HttpConnection.POST);
			//connection .setRequestProperty( HttpProtocolConstants.HEADER_CONTENT_TYPE, "application/json");
			connection .setRequestProperty( HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
			connection.setRequestProperty(HttpProtocolConstants.HEADER_ACCEPT, "application/json");

			// Add messageBody and set Content-Length 
			byte[] postData = (PROTOCOL_VER + "=" + messageBody).getBytes("UTF-8");
			connection.setRequestProperty(
					HttpProtocolConstants.HEADER_CONTENT_LENGTH,
					String.valueOf(postData.length));

			// Send data via POST
			OutputStream output = connection.openOutputStream();
			output.write(postData);
			output.flush();

			// Read response
			if (connection.getResponseCode() == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return new String(reply);
			} else {
				// TODO: inspect headers and log error
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getInstance().log(TAG, e.toString());
			return null;
		}
	}
	
	private static HttpConnection setupConnection(String url) throws IOException {
		if (DeviceInfo.isSimulator()) {
			// If running the MDS simulator append ";deviceside=false"
			return (HttpConnection) Connector.open(url + ";deviceside=true",
					Connector.READ_WRITE);
		}
		ConnectionFactory cf = new ConnectionFactory();
		// Ordered list of preferred transports
		int[] transportPrefs = { TransportInfo.TRANSPORT_TCP_WIFI,
				TransportInfo.TRANSPORT_TCP_CELLULAR,
				TransportInfo.TRANSPORT_WAP2, TransportInfo.TRANSPORT_WAP,
				TransportInfo.TRANSPORT_MDS, TransportInfo.TRANSPORT_BIS_B };
		cf.setPreferredTransportTypes(transportPrefs);
		ConnectionDescriptor cd = cf.getConnection(url);
		return (HttpConnection) cd.getConnection();
	}
	
	/**
	 * Checks if there is a valid internet connection.
	 * 
	 * @return true if connected.
	 */
	public static boolean isConnected() {
		String url = "http://www.msftncsi.com/ncsi.txt";
		String expectedResponse = "Microsoft NCSI";

		Logger.getInstance().log(TAG, "Checking connectivity");

		try {
			HttpConnection connection = setupConnection(url);
			connection.setRequestMethod(HttpConnection.GET);

			int status = connection.getResponseCode();
			if (status == HttpConnection.HTTP_OK) {
				InputStream input = connection.openInputStream();
				byte[] reply = IOUtilities.streamToBytes(input);
				input.close();
				connection.close();
				return expectedResponse.equals(new String(reply));
			}
		} catch (Exception e) {
			Logger.getInstance().log(TAG, "Connectivity test failed");
		}
		return false;
	}
}

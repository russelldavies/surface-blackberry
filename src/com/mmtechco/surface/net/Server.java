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

import com.mmtechco.util.Logger;
import com.mmtechco.util.ToolsBB;

/**
 * Monitors for new actions stored in the local storage for recording actions
 * and sends them to the web server at specific intervals.
 */
public class Server {
	private static final String TAG = ToolsBB.getSimpleClassName(Server.class);

	private static final String URL = "http://192.168.2.13/surface_sys/REST.php";
	private static final String PROTOCOL_VER = "1";

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

	public static Response post(String messageBody) throws IOException {
		/*
		if (!isConnected()) {
			return null;
		}
		*/
		Logger.getInstance().log(TAG, "POST data" + messageBody);
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

			// Construct reply
			return new Response(connection);
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

package org.ektorp.http;

import org.apache.http.conn.ClientConnectionManager;

public class IdleConnectionMonitor {

	private static ClientConnectionManagerMonitor monitor;

	public static synchronized void monitor(ClientConnectionManager cm) {
		if (monitor == null) {
			monitor = new DefaultClientConnectionManagerMonitor();
		}
		monitor.monitor(cm);
	}

	public static synchronized void shutdown() {
		if (monitor != null) {
			monitor.shutdown();
		}
	}
	
	public static interface ClientConnectionManagerMonitor {

		void monitor(ClientConnectionManager cm);

		void shutdown();

	}

}

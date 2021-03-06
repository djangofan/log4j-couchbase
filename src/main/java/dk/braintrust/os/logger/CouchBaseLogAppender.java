package dk.braintrust.os.logger;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.couchbase.client.CouchbaseClient;

/**
 * @author Steffen Larsen (slarsen@braintrust.dk)
 * 
 */
public class CouchBaseLogAppender extends AppenderSkeleton {
	private String hosts = "localhost";
	private int port = 8092;
	private String password = "";
	private String loggingBucket = "default";
	private boolean developmentMode = true;
	private int eviction = 0;
	private static CouchbaseClient client = null;
	private static List<URI> uris = new LinkedList<URI>();

	// private Map<String, String> messages; //TODO: store the log object into a
	// temp memtable..
	
	@Override
	public void activateOptions() {
		super.activateOptions();
		try {
			client = setupCouchbase();
		} catch (IOException e) {
			e.printStackTrace();			
			System.exit(-1);
		}
		// TODO: setup mem queue here..

	}

	private CouchbaseClient setupCouchbase() throws IOException {
		if (developmentMode)
			System.setProperty("viewmode", "development");

		String[] couchServers = hosts.split(",");
		for (String srv : couchServers) {
			uris.add(URI.create("http://" + srv + ":" + port + "/pools"));
		}
		return new CouchbaseClient(uris, loggingBucket, password);
	}

	@Override
	protected void append(LoggingEvent event) {
		String key = "logger:" + Utils.getHostName() + "_" + event.getThreadName() + "_" + event.timeStamp;
		client.add(key, eviction, this.layout.format(event));
	}

	public void setLoggingBucket(String loggingBucket) {
		this.loggingBucket = loggingBucket;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDevelopmentMode(boolean developmentMode) {
		this.developmentMode = developmentMode;
	}
	
	public void setEviction(int eviction) {
		this.eviction = eviction;
	}

	public boolean requiresLayout() {
		return true;
	}

	public void close() {
		client.shutdown();
	}
}

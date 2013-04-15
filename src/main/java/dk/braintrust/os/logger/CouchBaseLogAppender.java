package dk.braintrust.os.logger;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.couchbase.client.CouchbaseClient;


public class CouchBaseLogAppender extends AppenderSkeleton {
	private String hosts = "localhost";
	private int port = 8092;
	private String password = "";
	private String defaultMetadataBucket = "default";
	private Boolean developmentMode = true;	
		
	//private Map<String, String> messages; //TODO: store the log object into a temp memtable..
	private CouchbaseClient client = null;
	
	private static List<URI> uris = new LinkedList<URI>(); 
	
	
	@Override
	public void activateOptions() {
		super.activateOptions();
		try {
			client = setupCouchbase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// TODO: setup mem queue here..
		
	}
	
	private CouchbaseClient setupCouchbase() throws IOException {
		if (developmentMode)
			System.setProperty("viewmode", "development");
		
		System.out.println("couchbase viewmode set to: " + System.getProperty("viewmode"));
		System.out.println("metadata bucket for couchbase: " + defaultMetadataBucket);
		
		String[] couchServers = hosts.split(",");		
		for (String srv : couchServers) {
			System.out.println("Server search for couchbase: " + srv);
			uris.add(URI.create("http://" + srv + ":" + port + "/pools"));
		}					
		return new CouchbaseClient(uris,defaultMetadataBucket, password);
	}
	
	@Override
	protected void append(LoggingEvent event) {
		String key = event.getLoggerName()+ "_" + event.getThreadName() +  "_" + event.timeStamp;
		System.out.println(key + " persisted");		
		// TODO: find better key for couchbase lookup
		client.add(key, 0, this.layout.format(event));
		
	}
	
	

	public void setDefaultMetadataBucket(String defaultMetadataBucket) {
		this.defaultMetadataBucket = defaultMetadataBucket;
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
	public void setDevelopmentMode(Boolean developmentMode) {
		this.developmentMode = developmentMode;
	}
	
	public boolean requiresLayout() {
		return true;
	}
	
	public void close() {
		client.shutdown();
	}
}

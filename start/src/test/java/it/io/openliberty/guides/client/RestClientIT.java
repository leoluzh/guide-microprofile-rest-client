package it.io.openliberty.guides.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RestClientIT {

	private static String port;
	
	private Client client;
	
	private final String INVENTORY_SYSTEMS = "inventory/systems" ;
	
	@BeforeAll
	public static void oneTimeSetup() {
		port = System.getProperty("http.port");
	}
	
	@BeforeEach
	public void setup() {
		client = ClientBuilder.newClient();
		client.register(JsrJsonpProvider.class);
	}
	
	@AfterEach
	public void teardown() {
		client.close();
	}

	@Test
	public void testSuite() {
		testDefaultLocalhost();
		testRestClientBuilder();
	}
	
	public void testDefaultLocalhost() {
		String hostname = "localhost" ;
		String url = StringUtils.join("http://localhost",":",port,"/",INVENTORY_SYSTEMS,"/",hostname);
		JsonObject json = fetchProperties(url);
		assertEquals(
				System.getProperty("os.name"), 
				json.getString("os.name"),
				"The system property for the local and remote JVM should match.");
	}
	
	public void testRestClientBuilder() {
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostAddress();
		}catch(UnknownHostException ex) {
			System.err.println("Unknown host.");
		}
		String url = StringUtils.join("http://localhost",":",port,"/",INVENTORY_SYSTEMS,"/",hostname);
		JsonObject json = fetchProperties(url);
		assertEquals(
				System.getProperty("os.name"), 
				json.getString("os.name"),
				"The system property for the local and remote JVM should match.");
	}

	protected JsonObject fetchProperties( String url ) {
		WebTarget target = client.target( url );
		Response response = target.request().get();
		assertEquals( 200 , response.getStatus() , "Incorret response code from " + url );
		JsonObject json = response.readEntity(JsonObject.class);
		return json;
	}
	
}

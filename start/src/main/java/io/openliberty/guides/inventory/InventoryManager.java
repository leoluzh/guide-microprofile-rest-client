package io.openliberty.guides.inventory;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.openliberty.guides.inventory.client.SystemClient;
import io.openliberty.guides.inventory.client.UnknownUriException;
import io.openliberty.guides.inventory.client.UnknownUriExceptionMapper;
import io.openliberty.guides.inventory.model.InventoryList;
import io.openliberty.guides.inventory.model.SystemData;

@ApplicationScoped
public class InventoryManager {

	protected List<SystemData> systems = Collections.synchronizedList( new ArrayList<>() );
	
	@Inject
	@ConfigProperty(name= "default.http.port")
	protected String DEFAULT_PORT;
	
	@Inject
	@RestClient
	protected SystemClient defaultRestClient;
	
	public Properties get(String hostname) {
		Properties properties = null;
		if( "localhost".equals(hostname) ) {
			properties = getPropertiesWithDefaultHostName();
		}else {
			properties = getPropertiesWithGivenHostName(hostname);
		}
		return properties;
	}

	public void add(String hostname, Properties systemProps) {	
		Properties props = new Properties();
		props.setProperty("os.name", systemProps.getProperty("os.name"));
		props.setProperty("user.name", systemProps.getProperty("user.name"));
		SystemData host = new SystemData(hostname,props);
		if( !systems.contains(host)) {
			systems.add( host );
		}
	}

	public InventoryList list() {
		return new InventoryList(systems);
	}
	
	protected Properties getPropertiesWithDefaultHostName() {
		try {
			return defaultRestClient.getProperties();
		}catch(UnknownUriException ex) {
			System.err.println("The given URI is not formatted correctly.");
		}catch(ProcessingException ex) {
			handleProcessingException(ex);
		}
		return null;
	}
	
	protected Properties getPropertiesWithGivenHostName( String hostname ) {
		String customURIString = StringUtils.join("http://",hostname,":",DEFAULT_PORT,"/","system");
		URI customURI = null;
		try {
			customURI = URI.create(customURIString);
			//create with dynamic uri
			SystemClient customRestClient = RestClientBuilder
					.newBuilder()
					.baseUri(customURI)
					.register(UnknownUriExceptionMapper.class)
					.build(SystemClient.class);
			return customRestClient.getProperties();
		}catch(ProcessingException ex) {
			handleProcessingException(ex);
		}catch(UnknownUriException ex) {
			System.err.println("The given URI is unreachable.");
		}
		return null;
	}
	
	protected void handleProcessingException( ProcessingException ex ) {
		Throwable rootEx = ExceptionUtils.getRootCause(ex);
		if( rootEx != null 
				&& ( rootEx instanceof UnknownHostException 
						|| rootEx instanceof ConnectException )) {
			System.err.println("The specified host is unknown.");
		}else {
			throw ex;
		}
	}
	
}

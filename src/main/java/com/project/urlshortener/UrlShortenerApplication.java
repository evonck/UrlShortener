package com.project.urlshortener;


import com.project.urlshortener.resources.UrlShortenerResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
/**
 * Initialize and create the application
 * @author Eleonore
 *
 */
public class UrlShortenerApplication extends Application<UrlShortenerConfiguration> {
	
	public static void main(String[] args) throws Exception {
        new UrlShortenerApplication().run(args);
    }

    @Override
    public String getName() {
        return "Url-Shortener";
    }

    @Override
    public void initialize(Bootstrap<UrlShortenerConfiguration> bootstrap) {
    	 	
    }

    @Override
    /**
     * Initialize the application with the configuration data
     */
    public void run(UrlShortenerConfiguration configuration,
                    Environment environment) {
    	
    	 final UrlShortenerResource resource = new UrlShortenerResource(
    			 configuration.getMaxSizeUrl(),
    			 configuration.getRedis() 
    			 );
    	 
    	 
    	 environment.jersey().register(resource);
    	     }

}
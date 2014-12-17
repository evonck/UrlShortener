package com.project.urlshortener.resources;

import com.google.common.base.Optional;
import com.google.common.hash.Hashing;
import com.project.urlshortener.RedisConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import redis.clients.jedis.*;
/**
 * Define the response of the REST application 
 * @author Eleonore
 *
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UrlShortenerResource {
	

	 private static JedisPool pool; 
	 private static final Logger logger = LogManager.getLogger("urlshortener");
	 private int counter = 0;
	 private final int maxSizeUrl;
	 private RedisConfiguration redisConfiguration;
	 /**
	  * Create the UrlShortenerResource
	  * @param defaultSize
	  * 		MaximumSize of the Short URL define by in the configuration file
	  * @param redisConfiguration
	  * 		Configuration Redis, host name and port
	  */
    public UrlShortenerResource(int maxSizeUrl,RedisConfiguration redisConfiguration) {
    	 this.maxSizeUrl = maxSizeUrl;
    	 this.redisConfiguration = redisConfiguration;
    	 //Catch erreur pour log
    	 this.pool = new JedisPool(new JedisPoolConfig(),redisConfiguration.getHostname(),redisConfiguration.getPort()); 
    }

    @GET
    @Path("/{ShortUrlCode}")
    /**
     * Redirect the navigateur to the right URL
     * 
     * @param ShortUrlCode
     * 				Short URL enter in the navigator
     * @param response
     * @param request
     * @throws IOException
     */
    public void sayLongUrl(@PathParam("ShortUrlCode") String ShortUrlCode, @Context HttpServletResponse response,@Context HttpServletRequest request)throws IOException {
    	String ShortUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "/" + ShortUrlCode).toString();
    	try(Jedis jedis = pool.getResource()){
    		String LongUrl =jedis.get(ShortUrl);
    		if(LongUrl == null){
    			logger.error("The Short URL"+ShortUrl+" was not found on the database");
    			response.sendError(HttpServletResponse.SC_NOT_FOUND);
    		}else{
    			logger.trace("Redirect to URL:"+LongUrl);
    			response.sendRedirect(LongUrl);
    		}
    	}
    }
    
    @POST
    /**
     * Create a short URL coresponding to a URL.
     * Can use the parameter define by the user to create the sort URL
     * Also checke the validity of the long URL.
     * @param LongUrl
     * 			URL that need to be shorten
     * @param preferences
     * 			Preferences of the user for the Short URL, parameter optionnal
     * @param request
     * @return
     * @throws IOException
     * @throws MalformedURLException
     * 			Check that the LongUrl is well formed
     */
    public Response save(@QueryParam("LongUrl")	 String LongUrl,@QueryParam("Preferences") Optional<String> preferences,  @Context HttpServletRequest request)throws IOException,MalformedURLException{
    	final URL url = new URL(LongUrl);
    	HttpURLConnection huc = (HttpURLConnection) url.openConnection();
    	int responseCode = huc.getResponseCode();
    	if (responseCode != 404) {
    		String ShortUrlCode;
    		String ShortUrl;
    		if(preferences.isPresent()){
				ShortUrlCode = preferences.get();    				
    		}else{
    			ShortUrlCode = Hashing.murmur3_32().hashString(LongUrl, StandardCharsets.UTF_8).toString();
    		}
    		try(Jedis jedis = pool.getResource()){
				if(ShortUrlCode.length()>maxSizeUrl){
        			ShortUrlCode = ShortUrlCode.substring(0,maxSizeUrl);
        		}
        		ShortUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "/" + ShortUrlCode).toString();
				String TestUrl =jedis.get(ShortUrl);
				//Check that the ShortUrl is not already in the database
				if(TestUrl!=null){
					if(preferences.isPresent()){
						//Try to find another short URL using the preferences
						int CountPreference = 0;
						//Check that the Short URL is shorter to defaultSize to avoid unlimited loop
						while(TestUrl!=null && (ShortUrlCode.length())<= maxSizeUrl ){
							ShortUrlCode = preferences.get()+CountPreference;    			
							ShortUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "/" + ShortUrlCode).toString();
							TestUrl = jedis.get(ShortUrl);
							CountPreference++;
						}
						if(ShortUrlCode.length()> maxSizeUrl){
							//Respond with a conflict, all of the Short URL using the preference are already taken
							logger.error("All the URL with the"+ preferences.get()+" are taken");
							return Response.status(Response.Status.CONFLICT ).build();
						}else{
							jedis.set(ShortUrl,LongUrl);
			        		jedis.expire(ShortUrl,7889231);
			        		logger.trace("Creation of the short URl:"+ShortUrl);
			        		return Response.ok(ShortUrl).build();
						}
					}else{
						//Respond with the Short URL which was already created for that Long URL
						if(TestUrl == LongUrl){
							logger.trace("Creation of the short URl:"+ShortUrl);
							return Response.ok(ShortUrl).build();
						}else{
							logger.error("The ShortUrlCode already correspond to another Long URL");
							return Response.status(Response.Status.CONFLICT ).build();
						}
					}
				}else{	
					//Add the Short URL to the database
					jedis.set(ShortUrl,LongUrl);
        			jedis.expire(ShortUrl,7889231);
        			logger.trace("Creation of the short URl:"+ShortUrl);
        			return Response.ok(ShortUrl).build();
				}
			}	
        }
    			//Respond with bad request because the URL send respond with error 404
    			logger.error("The URL respond with Error 404");
    	      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    

}
package com.project.urlshortener;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;
/**
 * Define the configuration of the application
 * @author Eleonore
 *
 */

public class UrlShortenerConfiguration extends Configuration {
	@NotNull
    private int maxSizeUrl;
	@Valid
    @NotNull
    @JsonProperty
    private RedisConfiguration redis = new RedisConfiguration();
	/**
	 * Return the Redis COnfiguration
	 * @return
	 * 		return the Redis COnfiguration
	 */
	public RedisConfiguration getRedis()
	{
		return redis;
	}
	/**
	 * Return the maximum size of the Short URL
	 * @return
	 */
    @JsonProperty
    public int getMaxSizeUrl() {
        return maxSizeUrl;
    }
    /**
     * Set the Maximum size of the Short URL
     * @param defaultSize
     */
    @JsonProperty
    public void setMaxSizeUrl(int maxSizeUrl) {
        this.maxSizeUrl = maxSizeUrl;
    }	
}
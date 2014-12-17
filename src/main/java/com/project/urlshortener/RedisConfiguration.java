package com.project.urlshortener;


import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
/**
 * Define the  configuration of the database Redis
 * @author Eleonore
 *
 */
public class RedisConfiguration extends Configuration  {
	@NotEmpty
    @JsonProperty
	private String hostname;

	@Min(1)
    @Max(65535)
    @JsonProperty
	private Integer port;
	/**
	 * get the host name of the Redis host name
	 * @return
	 */
	public String getHostname()
	{
		return hostname;
	}
	/**
	 * Set the host name of the Redis configuration
	 * @param hostname
	 */
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	/**
	 * Get the port of the Redis configuration
	 * @return
	 */
	public Integer getPort()
	{
		return port;
	}
	/**
	 * Set the port of the Redis configuration
	 * @param port
	 */
	public void setPort(Integer port)
	{
		this.port = port;
	}


}

package com.logicaldoc.webdav.session;

/**
 * For more informations, please visit
 * {@link org.apache.jackrabbit.webdav.DavSession}
 * 
 * @author Sebastian Wenzky
 * 
 */
public interface WebdavSession extends org.apache.jackrabbit.webdav.DavSession {

	/**
	 * Puts an object to the session map
	 * 
	 * @param key The Key
	 * @param value the corresponding object
	 */
	public void putObject(String key, Object value);

	/**
	 * Gets an object by passing the appropriated key
	 * 
	 * @param key The Key
	 * 
	 * @return the corresponding object
	 */
	public Object getObject(String key);

	/**
	 * The tenant of this session
	 * 
	 * @return identifier of the tenant
	 */
	public long getTenantId();
}

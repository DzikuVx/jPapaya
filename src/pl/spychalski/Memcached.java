package pl.spychalski;

import java.io.IOException;
import java.net.InetSocketAddress;

import net.spy.memcached.MemcachedClient;

/**
 * @brief Simple wrapper over net.spy.memcached.MemcachedClient
 * @author Pawel
 *
 */
public class Memcached {

	private static int Length = 3600;

	private static Memcached instance = null;

	private MemcachedClient cache = null;

	/**
	 * Returns class instance
	 * @return
	 */
	final public static Memcached getInstance() {

		if (Memcached.instance == null) {
			Memcached.instance = new Memcached();
		}

		return Memcached.instance;
	}

	final public Object get(String key) {
		return this.cache.get(key);
	}
	
	/**
	 * Sets given key with default data expiration time
	 * @param key
	 * @param data
	 * @return
	 */
	final public Boolean set(String key, Object data) {

		this.set(key, data, Memcached.Length);
		
		return true;
	}

	/**
	 * Sets given key
	 * @param key
	 * @param data
	 * @param length
	 * @return
	 */
	final public Boolean set(String key, Object data, int length) {

		this.cache.set(key, length, data);

		return true;
	}

	/**
	 * Private contructor
	 */
	private Memcached() {

		try {
			this.cache = new MemcachedClient( new InetSocketAddress("localhost", 11211));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

}

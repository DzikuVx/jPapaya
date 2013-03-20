package pl.spychalski;

import java.io.IOException;
import java.net.InetSocketAddress;

import pl.papaya.bot.Config;

import net.spy.memcached.MemcachedClient;

public class Couchbase {

	private static int Length = 0;

	private static Couchbase instance = null;

	private MemcachedClient cache = null;

	/**
	 * Returns class instance
	 * @return
	 */
	final public static Couchbase getInstance() {

		if (Couchbase.instance == null) {
			Couchbase.instance = new Couchbase();
		}

		return Couchbase.instance;
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

		this.set(key, data, Couchbase.Length);
		
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

		this.cache.set(key, length, data).getStatus();

		return true;
	}

	/**
	 * Private contructor
	 */
	private Couchbase() {

		try {
			this.cache = new MemcachedClient( new InetSocketAddress(Config.getInstance().get("CouchbaseServer"), Integer.parseInt(Config.getInstance().get("CouchbasePort"))));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

}

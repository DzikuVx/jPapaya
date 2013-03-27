package pl.papaya.bot;

import java.sql.SQLException;
import java.util.ArrayList;

import pl.spychalski.Memcached;

final public class UrlGetter {

	private static UrlGetter instance = null;

	private Datamodel datamodel = null;

	/**
	 * Returns class instance
	 * 
	 * @return
	 */
	final public static UrlGetter getInstance() {

		if (UrlGetter.instance == null) {
			UrlGetter.instance = new UrlGetter();
		}

		return UrlGetter.instance;
	}

	private UrlGetter() {

		try {
			this.datamodel = Datamodel.getInstance();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @brief Gets single link from queue
	 * @return
	 */
	final public Link get()  {

		Link retVal = null;
		String url = null;
		
		try {

			Memcached cache = Memcached.getInstance();

			@SuppressWarnings("unchecked")
			ArrayList<String> cacheValue = (ArrayList<String>) cache
					.get("jPapayaUrlList");

			while (cacheValue == null || cacheValue.size() < 3) {
				cacheValue = this.datamodel.getUrlList();
			}

			url = cacheValue.remove(0).toString();

			cache.set("jPapayaUrlList", cacheValue);

			retVal = new Link(url);
			
		} catch (Exception e) {
			ErrorLog.getInstance().write("UrlGetter.get: " + e.getMessage());
		}
		
		return retVal;
	}

}

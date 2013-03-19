package pl.papaya.bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import pl.spychalski.Memcached;
import pl.spychalski.MysqlWrapper;

public class Config {

	private static Config instance = null;

	private Properties properties = null;

	private void load() {
		this.properties = new Properties();
		try {
			properties.load(new FileInputStream("jPapaya.ini"));
		} catch (IOException e) {
		}
	}

	private Config() {
		this.load();
	}

	/**
	 * Pobranie wpisu konfiguracyjnego
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return this.properties.get(key).toString();
	}

	public static Config getInstance() {

		if (Config.instance == null) {
			Config.instance = new Config();
		}

		return Config.instance;

	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getExcludedDomains() throws SQLException {

		ArrayList<String> retVal = new ArrayList<String>();

		Memcached cache = Memcached.getInstance();

		Object cacheValue = cache.get("jPapayaConfig");

		if (cacheValue == null) {

			ResultSet rs = null;
			Statement statement;

			statement = MysqlWrapper.getInstance().getConnection()
					.createStatement();
			rs = statement
					.executeQuery("SELECT Url FROM excluded_urls WHERE 1");

			while (rs.next()) {
				retVal.add(rs.getString("Url"));
			}

			cache.set("jPapayaConfig", retVal);
			System.out.println("Config From db");

		} else {
			retVal = (ArrayList<String>) cacheValue;
			System.out.println("Config From cache");
		}

		return retVal;
	}

}

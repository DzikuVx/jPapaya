package pl.papaya.bot;

import java.net.MalformedURLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;
import java.net.URL;

import pl.spychalski.Memcached;
import pl.spychalski.MysqlWrapper;
import pl.spychalski.Utils;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public final class Datamodel {

	static private Datamodel instance = null;

	/**
	 * Singleton
	 * 
	 * @return
	 * @throws SQLException
	 */
	static public Datamodel getInstance() throws SQLException {

		if (Datamodel.instance == null) {
			Datamodel.instance = new Datamodel();
		}

		return Datamodel.instance;
	}

	private Connection con = null;

	public Datamodel() throws SQLException {
		this.con = MysqlWrapper.getInstance().getConnection();
	}

	private long insertHost(String Hash, String Host) throws SQLException {

		PreparedStatement prepared;
		ResultSet rs = null;

		long retVal = 0;

		try {

			prepared = this.con
					.prepareStatement("INSERT INTO hosts(Hash,Host) VALUES(?,?)");
			prepared.setString(1, Hash);
			prepared.setString(2, Host);
			prepared.executeUpdate();

			rs = prepared.getGeneratedKeys();

			while (rs.next()) {
				retVal = rs.getLong(1);
			}

			prepared.close();

		} catch (SQLException e) {

			ErrorLog.getInstance().write(
					"Datamodel.insertHost: " + Host + " :" + e.getClass()
							+ ": " + e.getMessage() + " "
							+ e.getStackTrace().toString());

			throw new SQLException(e.getMessage().toString());

		}

		return retVal;
	}

	private long selectHost(String Hash) throws SQLException {
		long retVal = 0;

		PreparedStatement prepared;
		ResultSet rs = null;

		prepared = this.con
				.prepareStatement("SELECT HostID FROM hosts WHERE Hash=? LIMIT 1");
		prepared.setString(1, Hash);
		rs = prepared.executeQuery();

		while (rs.next()) {
			retVal = rs.getLong("HostID");
		}

		prepared.close();

		return retVal;
	}

	/**
	 * Pobranie HostID dla danego urla
	 * 
	 * @param link
	 * @return
	 * @throws ForceErrorException
	 * @throws MalformedURLException
	 * @throws SQLException
	 */
	public long getHostID(String link) throws ForceErrorException {

		long retVal = 0;

		try {

			Memcached cache = Memcached.getInstance();

			URL url = new URL(link);

			String host = url.getHost();

			Object cacheValue = cache.get("papayHostID:" + host);

			if (cacheValue == null) {

				retVal = this.selectHost(Utils.MD5(host));

				if (retVal == 0) {

					retVal = this.insertHost(Utils.MD5(host), host);

				}

				cache.set("papayHostID:" + host, retVal);
			} else {
				retVal = (Long) cacheValue;
			}

		} catch (Exception e) {

			ErrorLog.getInstance().write(
					"Datamodel.getHostID: " + link + " :" + e.getClass() + ": "
							+ e.getMessage() + " "
							+ e.getStackTrace().toString());

			throw new ForceErrorException();
		}

		return retVal;
	}

	public Boolean saveInboundLink(String source, String target)
			throws ForceErrorException {
		// TODO przerobić na obiekty Link
		long sourceID = this.getHostID(source);
		long targetID = this.getHostID(target);

		if (sourceID == targetID) {
			return false;
		}

		PreparedStatement prepared;

		try {

			prepared = this.con
					.prepareStatement("INSERT INTO host_links(`TargetID`,`SourceID`) VALUES(?,?)");
			prepared.setLong(1, targetID);
			prepared.setLong(2, sourceID);
			prepared.executeUpdate();
			prepared.close();

		} catch (MySQLIntegrityConstraintViolationException e) {
			// powtórzony klucz, zignoruj ten błąd
		} catch (Exception e) {
			ErrorLog.getInstance().write(
					"Datamodel.saveInboundLink: " + source + " :" + target
							+ ": " + e.getMessage() + " "
							+ e.getStackTrace().toString());
			throw new ForceErrorException();
		}

		return true;

	}

	/**
	 * Zapisanie linku do bazy danych
	 * 
	 * @param link
	 * @return
	 * @throws ForceErrorException
	 */
	final public Boolean saveLink(String link) throws ForceErrorException {

		// TODO przerobić na obiekt Link

		PreparedStatement prepared;

		long hostId = this.getHostID(link);

		try {

			prepared = this.con
					.prepareStatement("INSERT INTO urls(HostID,`Hash`,Url) VALUES(?,?,?)");
			prepared.setLong(1, hostId);
			prepared.setString(2, Utils.MD5(link));
			prepared.setString(3, link);
			prepared.executeUpdate();
			prepared.close();

		} catch (MySQLIntegrityConstraintViolationException e) {

			/*
			 * Jeśli zduplikowany klucz, jest OK, jeśli coś innego, zapisz
			 */
			if (e.getMessage().startsWith("Duplicate entry") == false) {
				ErrorLog.getInstance().write(
						"Datamodel.saveLink: " + link + " :" + e.getClass()
								+ ": " + e.getMessage() + " "
								+ e.getStackTrace().toString());
				throw new ForceErrorException();
			}

		} catch (SQLException e) {
			/*
			 * Wystąpił inny błąd zapisu
			 */
			ErrorLog.getInstance().write(
					"Datamodel.saveLink: " + link + " :" + e.getClass() + ": "
							+ e.getMessage() + " "
							+ e.getStackTrace().toString());

			throw new ForceErrorException();
		}

		return true;
	}

	final public void saveKeywords(String sUrl, String sTitle, String sKeywords)
			throws ForceErrorException, SQLException {

		PreparedStatement prepared;

		prepared = this.con
				.prepareStatement("INSERT INTO keywords (`Hash`,Title,Keywords) VALUES (?,?,?) ON DUPLICATE KEY UPDATE Title=?, Keywords=?");

		int iCut;

		prepared.setString(1, Utils.MD5(sUrl));

		if (sTitle.length() > 128) {
			iCut = 128;
		} else {
			iCut = sTitle.length();
		}
		prepared.setString(2, sTitle.substring(0, iCut));
		prepared.setString(4, sTitle.substring(0, iCut));

		if (sKeywords.length() > 8192) {
			iCut = 8192;
		} else {
			iCut = sKeywords.length();
		}
		prepared.setString(3, sKeywords.substring(0, iCut));
		prepared.setString(5, sKeywords.substring(0, iCut));

		prepared.executeUpdate();
		prepared.close();

	}

	// FIXME namierzyć co powoduje duplikację kluczy
	public ArrayList<String> getUrlList() {

		ArrayList<String> retVal = new ArrayList<String>();

		try {
			Random randomGenerator = new Random();

			StringBuilder sb = new StringBuilder();
			int randomInt;

			randomInt = randomGenerator.nextInt(1000);
			if (randomInt < 25) {
				/*
				 * Ponowna indeksacja
				 */
				sb.append("SELECT Url FROM urls WHERE LastVisitDate<'");

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -7);
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar
						.getTime().getTime());

				sb.append(currentTimestamp.toString());
				sb.append("' AND `Ignore`=0 ORDER BY ID__ ASC LIMIT ");
			} else {
				/*
				 * Indeksacja nowych
				 */
				sb.append("SELECT Url FROM urls WHERE LastVisitDate IS NULL AND `Ignore`=0 ORDER BY ID__ ASC LIMIT ");
			}
			randomInt = randomGenerator.nextInt(1000);
			sb.append(randomInt);
			sb.append(",1000");

			ResultSet rs = null;
			Statement statement;

			statement = this.con.createStatement();
			rs = statement.executeQuery(sb.toString());

			while (rs.next()) {
				retVal.add(rs.getString("Url"));
			}

			/*
			 * Shuffle
			 */
			Collections.shuffle(retVal);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return retVal;
	}

	public String getUrlFromCache() {
		Memcached cache = Memcached.getInstance();

		@SuppressWarnings("unchecked")
		ArrayList<String> cacheValue = (ArrayList<String>) cache
				.get("jPapayaUrlList");

		if (cacheValue == null) {
			cacheValue = this.getUrlList();
		}

		String retVal = cacheValue.remove(1).toString();

		cache.set("jPapayaUrlList", cacheValue);

		return retVal;
	}

	public void setSuccess(String sUrl) throws SQLException {
		PreparedStatement prepared;

		prepared = this.con
				.prepareStatement("UPDATE urls SET Count=Count+1, ErrorCount=0, LastVisitDate=? WHERE Hash=?");

		Timestamp d = new Timestamp(System.currentTimeMillis());

		prepared.setTimestamp(1, d);
		prepared.setString(2, Utils.MD5(sUrl));

		prepared.executeUpdate();
		prepared.close();

	}

	public void setError(String sUrl) {
		PreparedStatement prepared;

		try {

			prepared = this.con
					.prepareStatement("UPDATE urls SET Count=Count+1, ErrorCount=ErrorCount+1, LastVisitDate=? WHERE Hash=? LIMIT 1");

			Timestamp d = new Timestamp(System.currentTimeMillis());

			prepared.setTimestamp(1, d);
			prepared.setString(2, Utils.MD5(sUrl));

			prepared.executeUpdate();
			prepared.close();

		} catch (Exception e) {
			ErrorLog.getInstance().write(
					"Datamodel.setError[" + sUrl + "]: " + e.getMessage());
		}
	}

	public void setIgnore(String sUrl) {
		PreparedStatement prepared;

		try {

			prepared = this.con
					.prepareStatement("UPDATE urls SET Count=Count+1, `Ignore`=1, LastVisitDate=? WHERE Hash=? LIMIT 1");

			Timestamp d = new Timestamp(System.currentTimeMillis());

			prepared.setTimestamp(1, d);
			prepared.setString(2, Utils.MD5(sUrl));

			prepared.executeUpdate();
			prepared.close();

		} catch (SQLException e) {
			ErrorLog.getInstance().write(
					"Datamodel.setIgnore[" + sUrl + "]: " + e.getMessage());
		}
	}

}

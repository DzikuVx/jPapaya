package pl.papaya.bot;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import pl.spychalski.Utils;

public class Link {

	static private String[] allowedGtld = { "pl", "spychalski.info" };
	static private ArrayList<String> excludedDomains = null;
	
	private String url = null;

	public String getUrl() {

		String retVal = this.urlObject.getProtocol() + "://"
				+ this.urlObject.getHost() + this.urlObject.getPath();

		if (this.urlObject.getQuery() != null) {
			retVal += "?" + this.urlObject.getQuery();
		}

		return retVal;
	}

	private String hash = null;

	public String getHash() {
		return hash;
	}

	private URL urlObject = null;

	public String getHost() {
		return this.urlObject.getHost();
	}

	public URL getUrlObject() {
		return this.urlObject;
	}

	public Link(String url, Link baseUrl) throws MalformedURLException,
			SQLException {

		if (Link.excludedDomains == null) {
			Link.excludedDomains = Config.getInstance().getExcludedDomains();
		}

		url = this.cutUrl(url);

		URL newUrl = new URL(baseUrl.getUrlObject(), url);

		this.url = newUrl.toString();
		this.hash = Utils.MD5(this.url);
		this.urlObject = new URL(this.url);
	}

	public Boolean isValid() {

		if (this.url.length() < 5 || this.url.length() > 64) {
			return false;
		}

		/*
		 * Walidacja dozwolonych GTLD
		 */
		Boolean bFound = false;
		for (String gtld : Link.allowedGtld) {
			if (this.urlObject.getHost().endsWith(gtld)) {
				bFound = true;
				break;
			}
		}

		if (!(bFound)) {
			return false;
		}

		/*
		 * Sprawdzenie wykluczonych domen
		 */
		bFound = false;
		for (String excluded : Link.excludedDomains) {
			if (this.urlObject.getHost().endsWith(excluded)) {
				bFound = true;
				break;
			}
		}
		if (bFound) {
			return false;
		}

		return true;
	}

	public Link(String url) throws MalformedURLException, SQLException {

		if (Link.excludedDomains == null) {
			Link.excludedDomains = Config.getInstance().getExcludedDomains();
		}

		url = this.cutUrl(url);

		this.url = url;
		this.hash = Utils.MD5(url);
		this.urlObject = new URL(url);
	}

	public Link(String url, String hash) throws MalformedURLException,
			SQLException {

		if (Link.excludedDomains == null) {
			Link.excludedDomains = Config.getInstance().getExcludedDomains();
		}

		url = this.cutUrl(url);

		this.url = url;
		this.hash = hash;
		this.urlObject = new URL(url);
	}

	private String cutUrl(String url) {
		return url;
	}

}

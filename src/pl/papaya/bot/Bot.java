package pl.papaya.bot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipException;

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pl.spychalski.Utils;

public class Bot implements Runnable {

	private static Datamodel datamodel = null;

	private static int linksFromUrl = -1;

	private static int maxDoLinks = -1;
	private static int maxTextSize = -1;

	private static String[] charsToRemove = { ",", ">", "<", "[", "]", ")", "(", "&", "!",
		"@", "#", "^", "/", "|", "\\", "©", "←", "→", "+", "-", "%",
		"*", "=", ":", "~", "`", "'", "\"", "?", "\n", "\t", "&nbsp;",
		"nbsp;", "quot;", "gt;", "lt;", "raquo;", "„", "laquo;",
		"copy;", ". " };
	
	Thread t = null;

	private Link url = null;

	Bot(Datamodel datamodel, Link url) {

		if (Bot.linksFromUrl == -1) {
			Bot.linksFromUrl = Integer.parseInt(Config.getInstance().get(
					"LinksFromUrl"));
		}

		if (Bot.maxDoLinks == -1) {
			Bot.maxDoLinks = Integer.parseInt(Config.getInstance().get(
					"MaxDoLinks"));
		}
		
		if (Bot.maxTextSize == -1) {
			Bot.maxTextSize = Integer.parseInt(Config.getInstance().get(
					"MaxTextSize"));
		}

		this.url = url;

		if (Bot.datamodel == null) {
			Bot.datamodel = datamodel;
		}

		// Create a new thread
		this.t = new Thread(this, url.getUrl());
		t.start(); // Start the thread
	}

	public Boolean isAlive() {
		return t.isAlive();
	}

	public void restart(Link url) {

		this.url = url;

		this.t = new Thread(this, url.getUrl());
		t.start();
	}

	final private String sanitize(String sString) {

		sString = sString.toLowerCase();

		sString = sString.replace("ą", "a");
		sString = sString.replace("ę", "e");
		sString = sString.replace("ć", "c");
		sString = sString.replace("ż", "z");
		sString = sString.replace("ź", "z");
		sString = sString.replace("ń", "n");
		sString = sString.replace("ł", "l");
		sString = sString.replace("ó", "o");
		sString = sString.replace("ś", "s");

		for (String sToRemove : Bot.charsToRemove) {
			sString = sString.replace(sToRemove, " ");
		}

		/*
		 * Usunięcie wielokrotnych spacji
		 */
		sString = sString.trim().replaceAll("( )+", " ");

		return sString;
	}

	@SuppressWarnings("rawtypes")
	private String prepareText(String sHtml) {

		sHtml = this.sanitize(sHtml);

		List<String> lWords;
		lWords = new ArrayList<String>(Arrays.asList(sHtml.split(" ")));

		Iterator i;
		i = lWords.iterator();

		/*
		 * Usuń zbyt krótkie i zbyt długie elemenety
		 */
		String element = null;
		while (i.hasNext()) {

			element = i.next().toString();

			if (element.length() < 3 || element.length() > 36
					|| element.trim().length() < 3) {
				i.remove();
			}

		}

		/*
		 * Policz liczbę danych słów
		 */
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();

		i = lWords.iterator();

		while (i.hasNext()) {

			element = i.next().toString();

			if (!countMap.containsKey(element)) {
				countMap.put(element, 1);
			} else {
				Integer count = countMap.get(element);
				count = count + 1;
				countMap.put(element, count);
			}

		}

		/*
		 * Usuń elementy ze zbyt dużą liczbą wystąpień
		 */
		i = lWords.iterator();

		while (i.hasNext()) {

			if (countMap.get(i.next().toString()) > 10) {
				i.remove();
			}

		}

		String sString = Utils.implode(lWords, " ");
		sString = sString.trim().replaceAll("( )+", " ");

		return sString;

	}

	/**
	 * Metoda zajmuje się znalezionymi linkami
	 * 
	 * @param oDoc
	 * @throws ForceErrorException
	 * @throws SQLException
	 */
	private void doUrls(Document oDoc) throws ForceErrorException, SQLException {
		// TODO StringIndexOutOfBoundsException

		Elements oLinks = oDoc.select("a[href]");

		HashMap<String, Link> links = new HashMap<String, Link>();

		int counter = 0;

		for (Element oLink : oLinks) {

			counter++;

			if (counter > Bot.maxDoLinks) {
				break;
			}

			Link tmpLink;

			try {

				tmpLink = new Link(oLink.attr("href"), this.url);

				if (!tmpLink.isValid()) {
					continue;
				}

			} catch (MalformedURLException e) {
				/*
				 * błąd tworzenia urla, więc go pomiń
				 */
				continue;
			}

			if (!(links.containsKey(tmpLink.getUrl()))) {
				links.put(tmpLink.getUrl(), tmpLink);
			}

		}
		
		ArrayList<Link> linkMap = new ArrayList<Link>(links.values());

		/*
		 * Dobierz się do mapy w koleności losowej
		 */
		Collections.shuffle(linkMap);

		/**
		 * Zapisz linki przychodzące jako mapę
		 */
		this.saveInboudMap(linkMap);

		this.saveLinks(linkMap);

	}

	private void saveInboudMap(ArrayList<Link> linkMap)
			throws ForceErrorException {

		for (Link link : linkMap) {
			Bot.datamodel.saveInboundLink(this.url.getUrl(), link.getUrl());
		}

	}

	/**
	 * Zapisanie nowych linków pobranych ze strony
	 * 
	 * @param linkMap
	 * @throws ForceErrorException
	 */
	private void saveLinks(ArrayList<Link> linkMap) throws ForceErrorException {

		int iCounter = 0;

		for (Link link : linkMap) {

			if (iCounter > Bot.linksFromUrl) {
				break;
			}

			if (Bot.datamodel.saveLink(link.getUrl())) {
				iCounter++;
			}
		}

	}

	/**
	 * Główna funkcja
	 */
	public void run() {

		Document oDoc;

		System.out.println(this.url.getUrl());

		if (this.url.getUrl() == null) {
			return;
		}

		/*
		 * Przeparsuj
		 */
		try {
			oDoc = Jsoup.connect(this.url.getUrl()).get();

			oDoc.outputSettings().charset("UTF-8");

			this.doUrls(oDoc);

			this.doContent(oDoc);

			Bot.datamodel.setSuccess(this.url.getUrl());

		} catch (UnknownHostException e) {
			/*
			 * Nieznany host
			 */
			Bot.datamodel.setIgnore(this.url.getUrl());
		} catch (SocketException e) {
			/*
			 * Timeout
			 */
			Bot.datamodel.setError(this.url.getUrl());
		} catch (SocketTimeoutException e) {
			/*
			 * Timeout
			 */
			Bot.datamodel.setError(this.url.getUrl());
		} catch (SSLHandshakeException e) {
			/*
			 * Błąd certyfikatu SSl
			 */
			Bot.datamodel.setError(this.url.getUrl());
		} catch (ZipException e) {
			/*
			 * Błąd certyfikatu SSl
			 */
			Bot.datamodel.setIgnore(this.url.getUrl());
		} catch (IOException e) {

			if (e.getMessage().startsWith("Unhandled content type")) {
				/*
				 * Nieobsługiwany typ zasobu, ignoruj
				 */
				Bot.datamodel.setIgnore(this.url.getUrl());
			} else if (e.getMessage().startsWith("Too many redirects occurred")) {
				/*
				 * Za dużo 301 po drodze, zignoruj go
				 */
				Bot.datamodel.setIgnore(this.url.getUrl());

			} else if (e.getMessage().startsWith("404")
					|| e.getMessage().startsWith("400")) {
				/*
				 * 404
				 */
				Bot.datamodel.setIgnore(this.url.getUrl());

			} else if (e.getMessage().startsWith("403")
					|| e.getMessage().startsWith("500")
					|| e.getMessage().startsWith("503")
					|| e.getMessage().startsWith("-1")) {
				/*
				 * 403 i inne powtarzalne
				 */
				Bot.datamodel.setError(this.url.getUrl());

			} else {
				/*
				 * Nie rozpoznany błąd, ustaw błąd i zapisz komunikat błędu
				 */
				ErrorLog.getInstance().write(
						"Bot.Run: " + this.url.getUrl() + " :" + e.getClass()
								+ ": " + e.getMessage() + " "
								+ e.getStackTrace().toString());
				Bot.datamodel.setError(this.url.getUrl());
			}

		} catch (ForceErrorException e) {

			/*
			 * nastąpuł już jakiś błąd, log został zapisny wcześniej, po prostu
			 * zapisz błąd urla
			 */
			Bot.datamodel.setError(this.url.getUrl());

		} catch (Exception e) {
			ErrorLog.getInstance().write(
					"Bot.Run: " + this.url.getUrl() + " :" + e.getClass()
							+ ": " + e.getMessage() + " "
							+ e.getStackTrace().toString());

			Bot.datamodel.setError(this.url.getUrl());
		}

	}

	private void doContent(Document oDoc) throws ForceErrorException,
			SQLException {

		String content = oDoc.text();

		if (content.length() > Bot.maxTextSize) {
			content = content.substring(0, Bot.maxTextSize);
		}

		Bot.datamodel.saveKeywords(this.url.getUrl(), oDoc.title(),
				this.prepareText(content));

	}

}

package pl.papaya.bot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;

public class jPapaya {

	/**
	 * Metoda główna
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		String path = jPapaya.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			String decodedPath = URLDecoder.decode(path, "UTF-8");
		
			System.out.println(path);
			System.out.println(decodedPath);
			System.exit(-1);
		
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		System.out.println("Start");

		int botCount = Integer.parseInt(Config.getInstance().get("BotCount"));

		int loopSleepTime = Integer.parseInt(Config.getInstance().get(
				"LoopSleep"));

		ArrayList<Bot> threadList = new ArrayList<Bot>();

		try {

			for (int i = 0; i < botCount; i++) {

				threadList.add(new Bot(Datamodel.getInstance(), UrlGetter
						.getInstance().get()));

			}

			while (true) {
				for (Bot bot : threadList) {
					if (bot.isAlive() == false) {
						bot.restart(UrlGetter.getInstance().get());
					}
				}

				/*
				 * Pazuza przed kolejn rundą
				 */
				try {
					Thread.sleep(loopSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}

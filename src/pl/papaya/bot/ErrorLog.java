package pl.papaya.bot;

public class ErrorLog extends Log {

	protected String logName = "jPapaya_error.log";
	
	private static ErrorLog instance = null;

	public void write(String message) {
		super.write(this.logName, message);
	}
	
	/**
	 * Returns class instance
	 * 
	 * @return
	 */
	final public static ErrorLog getInstance() {

		if (ErrorLog.instance == null) {
			ErrorLog.instance = new ErrorLog();
		}

		return ErrorLog.instance;
	}

}

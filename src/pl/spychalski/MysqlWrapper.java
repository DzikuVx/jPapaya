package pl.spychalski;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlWrapper {

//	static final String URL = "jdbc:mysql://127.0.0.1:3306/searcher?useUnicode=true&characterEncoding=UTF-8";
	static final String URL = "jdbc:mysql://192.168.2.99:3306/searcher?useUnicode=true&characterEncoding=UTF-8";
	static final String USER = "searcher";
	static final String PASSWORD = "searcher";
	static final String DRIVER = "com.mysql.jdbc.Driver";

	static private MysqlWrapper instance = null;

	private Connection con = null;

	static public MysqlWrapper getInstance() throws SQLException {

		if (MysqlWrapper.instance == null) {
			MysqlWrapper.instance = new MysqlWrapper();
		}

		return MysqlWrapper.instance;
	}

	final private void connect() throws SQLException {

		this.con = null;

		try {
			Class.forName(DRIVER);
			this.con = DriverManager.getConnection(URL, USER, PASSWORD);

			Statement statement;

			statement = this.con.createStatement();
			statement.executeQuery("SET NAMES UTF8");
			statement.executeQuery("SET CHARACTER SET UTF8");

		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}

	final public Connection getConnection() {
		return this.con;
	}

	public MysqlWrapper() throws SQLException {
		this.connect();
	}

}

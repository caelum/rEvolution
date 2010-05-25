package edu.usp.ime.revolution.persistence;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import edu.usp.ime.revolution.metrics.MetricSet;

public class SqlitePersistence implements MetricPersistence {

	private final String db;
	private final String projectName; 
	
	private Connection connection = null;  
    private PreparedStatement statement = null;
	
	public SqlitePersistence(String db, String projectName) {
		this.db = db;
		this.projectName = projectName;
	}
	
	public void save(MetricSet set) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + db);
			
			int csId = saveChangeSet(set);
			for(Map.Entry<String, Double> metric: set.getMetrics().entrySet()) {
				saveMetric(csId, metric.getKey(), metric.getValue());
			} 
			
			connection.close();
		}
		catch(Exception e) {
			throw new PersistenceException(e);
		}
	}

	private void saveMetric(int csId, String name, Double value) throws SQLException {
		statement = connection.prepareStatement(
			"INSERT INTO METRICS (CHANGESET, NAME, VALUE) VALUES (?,?,?)");

		statement.setInt(1, csId);
		statement.setString(2, name);
		statement.setDouble(3, value);
		
		statement.execute();
	}

	private int saveChangeSet(MetricSet set) throws SQLException {
		statement = connection.prepareStatement(
			"INSERT INTO CHANGESETS (PROJECT, NAME, DATE) VALUES (?,?,?)",
			Statement.RETURN_GENERATED_KEYS);

		statement.setString(1, projectName);
		statement.setString(2, set.getName());
		statement.setDate(3, new Date(set.getDate().getTimeInMillis()));
		
		statement.execute();
		ResultSet rs = statement.getGeneratedKeys();
		
		int id = 0;
		if(rs.next()){
			id = rs.getInt(1);
		}
		
		rs.close();
		return id;
	}

}
	
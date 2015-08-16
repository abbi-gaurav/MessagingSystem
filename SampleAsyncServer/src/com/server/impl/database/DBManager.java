package com.server.impl.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.jdbc2.optional.PoolingDataSource;

import com.asl.utils.QueryName;


public class DBManager {
	private static final Logger LOGGER = Logger.getLogger(DBManager.class.getCanonicalName());

	private static final String db_URL = "localhost";//"jdbc:postgresql://localhost/";
	private static final String db_NAME = "asl_project_db";
	private static final String db_USER = "user26";
	private static final String db_PASS = "123";
	
	private PoolingDataSource pool;
	
	public DBManager(int nbCon) throws SQLException{
		this(db_URL, 5432, db_NAME, db_USER, db_PASS, nbCon);
	}
	
	public DBManager(int nCon, String dbHost, int dbPort, String userName) throws SQLException{
		this(dbHost,dbPort,db_NAME,userName,db_PASS,nCon);
	}
	
	public DBManager(String dbHost, int dbPort, String dbName, String userName, String password, int nbCon) throws SQLException{
		System.out.format("connecting to db on %s with port %d, user name %s, password %s, connection pool %d, dbName %s",
														dbHost, dbPort, userName, password, nbCon, dbName);
		System.out.println();
		pool = new PoolingDataSource();
		pool.setServerName(dbHost);
		pool.setPortNumber(dbPort);
		pool.setDatabaseName(dbName);
		pool.setUser(userName);
//		pool.setPassword(password);
		pool.setMaxConnections(nbCon);
		Connection con=null;
		try {
			con = pool.getConnection();
			cachePreparedStatements(con);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE,"DB Error, couldn't create connection and cache preparedStatements: "+e.getMessage(),e);
			throw e;
		} finally{
			releaseConnection(con);
		}
	}
	
	//according to http://www.theserverside.com/news/1365244/Why-Prepared-Statements-are-important-and-how-to-use-them-properly
	//the statements will be created once and then cached so that when we create the same prepared stmt for another con from the pool
	//we directly get the cached version and there are no performances troubles.
	private void cachePreparedStatements(Connection con) throws SQLException{
		for(QueryName q : QueryName.values()){
			if(q.equals(QueryName.ABSTRACT)||q.equals(QueryName.ONE_WAY)){
				continue;
			}
			getQuery(q,con);
		}
	}
	
	public PreparedStatement getQuery(QueryName queryName, Connection con) throws SQLException{
		switch (queryName) {
			case NEW_CLIENT:
				// ?::role because it is necessary to cast the string from the prepared stmt to the role enum type defined in postgresql
				return con.prepareStatement("SELECT create_client(?::role)");
			case NEW_QUEUE:
				return con.prepareStatement("SELECT create_queue()");
			case DELETE_QUEUE:
				return con.prepareStatement("SELECT delete_queue(?)");
			case LIST_QUEUE:
				return con.prepareStatement("SELECT list_queues()");
			case LIST_QUEUE_WITH_MESSAGE:
				return con.prepareStatement("SELECT list_queues_with_message(?)");
			case POST:
				return con.prepareStatement("SELECT post(row(?,?,?,?,?),?)");
			case BROADCAST:	
				return con.prepareStatement("SELECT broadcast(row(?,?,?,?,?))");
			case READ:	
				return con.prepareStatement("SELECT read(?,?)");
			case CHECK_MESSAGE_FROM:	
				return con.prepareStatement("SELECT check_message_from(?,?)");
			case RETRIEVE_MESSAGE:	
				return con.prepareStatement("SELECT retrieve_message(?,?,?)");
			case MGMT_FETCH_QUEUES:
				return con.prepareStatement("SELECT mgmt_fetch_queues(?,?)");
			case MGMT_FETCH_MESSAGES:
				return con.prepareStatement("SELECT mgmt_fetch_messages(?,?,?)");
			case MGMT_FETCH_MESSAGE_DETAILS:
				return con.prepareStatement("SELECT mgmt_fetch_message_details(?)");
			default:
				LOGGER.log(Level.SEVERE, "Error, requested DB query doesn't exist: {0}",new Object[] {queryName});
				return null; //this case shouldn't happen.
		}
	}
	
	public Connection getConnection() throws SQLException{
		return pool.getConnection();
//		return DriverManager.getConnection("jdbc:postgresql://localhost:5432/asl_project_db", pool.getUser(), pool.getPassword());
	}
	
	public void releaseConnection(Connection con){
		if(con!=null)
			try {
				LOGGER.log(Level.FINE, "connection is released	");
				con.close();
			} catch (SQLException e) {
				LOGGER.log(Level.SEVERE, "DB Error, couldn't release connection: "+e.getMessage(),e);
			}
	}
	
	public void terminate(){
		pool.close();
	}
	
	/* NO CONNECTION POOL
	private Connection db_con;
	private HashMap<QueryName, PreparedStatement> db_query;
	
	public DBManager(){
		this(db_URL+db_NAME,db_USER,db_PASS);
	}
	
	public DBManager(String url, String user, String password){
		try {
            db_con = DriverManager.getConnection(url, user, password);
            createPreparedStatements();
        } catch (SQLException ex) {
        	System.err.println("Couldn't instantiate the third tier: "+ex);
        }
	}
	
	private void createPreparedStatements() throws SQLException{
		db_query = new HashMap<>();
		// ?::role because it is necessary to cast the string from the prepared stmt to the role enum type defined in postgresql
		db_query.put(QueryName.NEW_CLIENT, db_con.prepareStatement("SELECT create_client(?::role)"));
		
		db_query.put(QueryName.NEW_QUEUE, db_con.prepareStatement("SELECT create_queue()"));
		db_query.put(QueryName.DELETE_QUEUE, db_con.prepareStatement("SELECT delete_queue(?)"));
		db_query.put(QueryName.LIST_QUEUE, db_con.prepareStatement("SELECT list_queues()"));
		db_query.put(QueryName.LIST_QUEUE_WITH_MESSAGE, db_con.prepareStatement("SELECT list_queues_with_message(?)"));
		
		db_query.put(QueryName.POST, db_con.prepareStatement("SELECT post(row(?,?,?,?,?),?)"));
		db_query.put(QueryName.BROADCAST, db_con.prepareStatement("SELECT broadcast(row(?,?,?,?,?))"));
		
		db_query.put(QueryName.READ, db_con.prepareStatement("SELECT read(?,?)"));
		db_query.put(QueryName.CHECK_MESSAGE_FROM, db_con.prepareStatement("SELECT check_message_from(?,?)"));
		db_query.put(QueryName.RETRIEVE_MESSAGE, db_con.prepareStatement("SELECT retrieve_message(?,?,?)"));
	}
	
	public PreparedStatement getQuery(QueryName queryName){
		return db_query.get(queryName);
	}
	
	public boolean terminate(){
		try {
			for (QueryName qn: QueryName.values()) {
				PreparedStatement pst = db_query.get(qn);
				if(pst!=null) pst.close();
			}
            if (db_con != null) {
                db_con.close();
            }
            return true;
        } catch (SQLException ex) {
            System.err.println("Couldn't close the DBManager: "+ex);
            return false;
        }
	}
	
	public Connection getConnection(){
		return db_con;
	}
	
	public void testConnection(){
		try{
			Statement st = db_con.createStatement();
			ResultSet rs = st.executeQuery("SELECT VERSION()");
	
	        if (rs.next()) {
	            System.out.println(rs.getString(1));
	        }
		} catch (SQLException ex) {
        	System.err.println("Test failed: "+ex);
        }
	}
	*/
}

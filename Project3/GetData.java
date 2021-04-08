import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Vector;



//json.simple 1.1
// import org.json.simple.JSONObject;
// import org.json.simple.JSONArray;

// Alternate implementation of JSON modules.
import org.json.JSONObject;
import org.json.JSONArray;

public class GetData{
	
    static String prefix = "project3.";
	
    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;
	
    // You must refer to the following variables for the corresponding 
    // tables in your database

    String cityTableName = null;
    String userTableName = null;
    String friendsTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;
    String programTableName = null;
    String educationTableName = null;
    String eventTableName = null;
    String participantTableName = null;
    String albumTableName = null;
    String photoTableName = null;
    String coverPhotoTableName = null;
    String tagTableName = null;

    // This is the data structure to store all users' information
    // DO NOT change the name
    JSONArray users_info = new JSONArray();		// declare a new JSONArray

	
    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
	super();
	String dataType = u;
	oracleConnection = c;
	// You will use the following tables in your Java code
	cityTableName = prefix+dataType+"_CITIES";
	userTableName = prefix+dataType+"_USERS";
	friendsTableName = prefix+dataType+"_FRIENDS";
	currentCityTableName = prefix+dataType+"_USER_CURRENT_CITIES";
	hometownCityTableName = prefix+dataType+"_USER_HOMETOWN_CITIES";
	programTableName = prefix+dataType+"_PROGRAMS";
	educationTableName = prefix+dataType+"_EDUCATION";
	eventTableName = prefix+dataType+"_USER_EVENTS";
	albumTableName = prefix+dataType+"_ALBUMS";
	photoTableName = prefix+dataType+"_PHOTOS";
	tagTableName = prefix+dataType+"_TAGS";
    }
	

    //implement this function

    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException{
    	JSONArray users_info = new JSONArray();
        try (Statement stmt = oracleConnection.createStatement()) {
            ResultSet rst = stmt.executeQuery(
                "SELECT * FROM " + userTableName
            );
            HashMap<Long, JSONObject> User_Hash = new HashMap<Long, JSONObject>();
            while (rst.next()) {
                JSONObject one_user = new JSONObject();
                one_user.put("user_id", rst.getLong(1));
                one_user.put("first_name", rst.getString(2));
                one_user.put("last_name", rst.getString(3));
                one_user.put("YOB", rst.getInt(4));
                one_user.put("MOB", rst.getInt(5));
                one_user.put("DOB", rst.getInt(6));
                one_user.put("gender", rst.getString(7));
                one_user.put("current", new JSONObject());
                one_user.put("hometown", new JSONObject());
                User_Hash.put(rst.getLong(1), one_user);
            }
            rst = stmt.executeQuery(
                "SELECT U.User_ID, C1.Country_Name, C1.City_Name, C1.State_Name, C2.Country_Name, C2.City_Name, C2.State_Name " +
                "FROM " + userTableName + " U " +
                "LEFT JOIN " + hometownCityTableName + " UHC ON UHC.User_ID = U.User_ID " +
                "LEFT JOIN " + cityTableName + " C1 ON C1.City_ID = UHC.Hometown_City_ID " +
                "LEFT JOIN " + currentCityTableName + " UCC ON UCC.User_ID = U.User_ID " +
                "LEFT JOIN " + cityTableName + " C2 ON C2.City_ID = UCC.Current_City_ID"
            );
            long user_id_city = 0;
            while (rst.next()) {
                user_id_city = rst.getLong(1);
                if (rst.getString(2) != null) {
                    JSONObject hometown_city = new JSONObject();
                    hometown_city.put("country", rst.getString(2));
                    hometown_city.put("city", rst.getString(3));
                    hometown_city.put("state", rst.getString(4));
                    User_Hash.get(user_id_city).put("hometown", hometown_city);
                }
                if (rst.getString(5) != null) {
                    JSONObject current_city = new JSONObject();
                    current_city.put("country", rst.getString(5));
                    current_city.put("city", rst.getString(6));
                    current_city.put("state", rst.getString(7));
                    User_Hash.get(user_id_city).put("current", current_city);
                }
                users_info.put(User_Hash.get(user_id_city));
            }
            rst = stmt.executeQuery(
                "SELECT U.User_ID, F.User1_ID, F.User2_ID FROM " + userTableName + " U " +
                "LEFT JOIN " + friendsTableName + " F ON U.User_ID = F.User1_ID " +
                "ORDER BY U.User_ID, F.User1_ID, F.User2_ID"
            );
            long user_id_friend = 0;
            long last_id = -1;
            long friend_id = 0;
            JSONArray friends_info = new JSONArray();
            while (rst.next()) {
                user_id_friend = rst.getLong(1);
                friend_id = rst.getLong(3);
                if (user_id_friend != last_id) {
                    if (last_id != -1) {
                        User_Hash.get(last_id).put("friends", friends_info);
                        users_info.put(User_Hash.get(last_id));
                    }
                    friends_info = new JSONArray();
                    if (friend_id != 0) {
                        friends_info.put(friend_id);
                    }
                }
                else {
                    friends_info.put(friend_id);
                }
                last_id = user_id_friend;
            }
            User_Hash.get(last_id).put("friends", friends_info);
            users_info.put(User_Hash.get(last_id));
            rst.close();
            stmt.close();
        }
		return users_info;
    }

    // This outputs to a file "output.json"
    public void writeJSON(JSONArray users_info) {
	// DO NOT MODIFY this function
	try {
	    FileWriter file = new FileWriter(System.getProperty("user.dir")+"/output.json");
	    file.write(users_info.toString());
	    file.flush();
	    file.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}
		
    }
}

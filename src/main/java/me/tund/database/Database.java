package me.tund.database;

import io.github.cdimascio.dotenv.Dotenv;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import me.tund.utils.Utilities;
import org.mariadb.jdbc.export.Prepare;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    public Connection con;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("DatabaseClient");

    public Database(){
        Dotenv dotenv = Dotenv.load();
        try {
            con = DriverManager.getConnection(
                    dotenv.get("DB_URL"),
                    dotenv.get("DB_USER"), dotenv.get("DB_PWD")
            );
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean addUserEntry(String uuid, String username, boolean[] brs, String preferred_unit, int preferred_br, String activity, double kd, boolean replace, int priority){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("""
        INSERT INTO user_track(discord_id, in_game_name, `13.0`, `12.0`, 
                       `11.0`, `10.0`, `9.0`, `8.0`, `7.0`, `6.0`, `5.0`, 
                       `4.0`, prefered_unit, preferred_br, activity, kd,
                       `replace`, priority)
        VALUES (?, ?, ? , ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """)) {
            statement.setString(1, uuid);
            statement.setString(2, username);
            int d = 3;
            for (int i = 0; i <= 9; i++ ){
                statement.setBoolean(d, brs[i]);
                d++;
            }
            statement.setString(13, preferred_unit);
            statement.setInt(14, preferred_br);
            statement.setString(15, activity);
            statement.setDouble(16, kd);
            statement.setBoolean(17, replace);
            statement.setInt(18, priority);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 && createUserTable(uuid.toString());
        }catch(Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserEntry(String uuid, String username, boolean[] brs, String preferred_unit, int preferred_br, String activity, double kd, boolean replace, int priority){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("""
        UPDATE user_track SET discord_id =?, in_game_name =?, `13.0` =?, `12.0` =?, 
                       `11.0` =?, `10.0` =?, `9.0` =?, `8.0` =?, `7.0` =?, `6.0` =?, `5.0` =?, 
                       `4.0` =?, prefered_unit =?, preferred_br =?, activity =?, kd =?,
                       `replace` =?, priority =?
        WHERE `discord_id` = ?
        """)) {
            statement.setString(1, uuid);
            statement.setString(2, username);
            int d = 3;
            for (int i = 0; i <= 9; i++ ){
                statement.setBoolean(d, brs[i]);
                d++;
            }
            statement.setString(13, preferred_unit);
            statement.setInt(14, preferred_br);
            statement.setString(15, activity);
            statement.setDouble(16, kd);
            statement.setBoolean(17, replace);
            statement.setInt(18, priority);
            statement.setString(19, uuid);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0 && createUserTable(uuid.toString());
        }catch(Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserEntry(String uuid){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("""
        SELECT * FROM user_track WHERE discord_id = ?
        """)) {
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            logger.debug("Received answer from Database for Query FIND_USER_ENTRY. In_Game_Name if found: {}.", resultSet.first());
            return resultSet.first();
        }catch(Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }

    public boolean changeUserName(String uuid, String username){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET in_game_name = ? WHERE discord_id = ?")) {
            statement.setString(1, username);
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }
    public boolean changePreferredRole(String uuid, String role){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET prefered_unit = ? WHERE discord_id = ?")) {
            statement.setString(1, role);
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }

    public boolean changePreferredBR(String uuid, int br){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET preferred_br = ? WHERE discord_id = ?")) {
            statement.setInt(1, br);
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }

    public boolean changeKillRation(String uuid, double kd){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET kd = ? WHERE discord_id = ?")) {
            statement.setDouble(1, kd);
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }

    public boolean changeActivity(String uuid, int activity){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET activity = ? WHERE discord_id = ?")) {
            statement.setString(1, String.valueOf(activity));
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }
    public boolean changeReplace(String uuid, boolean replace){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("UPDATE user_track SET `replace` = ? WHERE discord_id = ?")) {
            statement.setBoolean(1, replace);
            statement.setString(2, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }

    public boolean changeBrs(String uuid, boolean[] brs ){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("" +
                "UPDATE user_track SET " +
                "`13.0` =?,  " +
                "`12.0` =?," +
                "`11.0` =?," +
                "`10.0` =?," +
                "`9.0` =?," +
                "`8.0` =?," +
                "`7.0` =?," +
                "`6.0` =?," +
                "`5.0` =?," +
                "`4.0` =? " +
                "WHERE discord_id = ?")) {
            for(int i = 0; i < brs.length; i++){
                statement.setBoolean(i+1, brs[i]);
            }
            statement.setString(brs.length+1, uuid);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. \n Stacktrace: {}", e.getMessage());
            return false;
        }
    }






    public boolean addSessionEntry(String startime, String endtime, Long[] ids, HashMap<Long, Integer> playtime, HashMap<Long, Integer> waittime, int total_rounds, double winration, double br){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("""
        INSERT INTO session_track(starttime, endtime, participants, participants_playtime, participants_waittime, total_rounds, `win-ration`, BR) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """)) {
            DateTime start = new DateTime(startime, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00")));
            DateTime end = new DateTime(endtime, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00")));
            statement.setDate(1, new Date(start.getMillis()));
            statement.setDate(2, new Date(end.getMillis()));
            StringBuilder s = new StringBuilder();
            for (Long l : ids){
                s.append(l.longValue()+";");
            }
            statement.setString(3, s.toString());
            s = new StringBuilder();
            for(Map.Entry e : playtime.entrySet()){
                s.append(e.getKey()+";"+e.getValue()+"\n");
            }
            statement.setString(4, s.toString());
            s = new StringBuilder();
            for(Map.Entry e : waittime.entrySet()){
                s.append(e.getKey()+";"+e.getValue()+"\n");
            }
            statement.setString(5, s.toString());
            statement.setInt(6, total_rounds);
            statement.setDouble(7, winration );
            statement.setDouble(8, br);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }catch (Exception e){
            logger.error("Something went wrong trying to add a UserEntry. Stacktrace: {}", e.getStackTrace());
            return false;
        }
    }


    public SquadMember getSquadMemberById(long id){
        con = Utilities.checkValidConnection(this.con);
        try(PreparedStatement statement = con.prepareStatement("""
        SELECT * from user_track WHERE discord_id = ?
        """)) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                int user_no = rs.getInt("user_no");
                long discord_id = Long.parseLong(rs.getString("discord_id"));
                String in_game_name = rs.getString("in_game_name");
                boolean[] brs = new boolean[10];
                for(int i = 0; i<9; i++){
                    brs[i] = rs.getBoolean(String.valueOf(i+4)+".0");
                }
                String preferred_unit = rs.getString("prefered_unit");
                double preferred_br = rs.getDouble("preferred_br");
                int activity = rs.getInt("activity");
                double kd = rs.getDouble("kd");
                boolean replace = rs.getBoolean("replace");
                int trainings = rs.getInt("trainings");
                int cookies = rs.getInt("cookies");
                int priority = rs.getInt("priority");
                SquadMember sq = new SquadMember(user_no, discord_id, in_game_name, brs, preferred_unit, preferred_br, activity, trainings, cookies, kd, replace, priority);
                return sq;
            }
        }catch (Exception e){
            Logger.getLogger("Database").log(Level.SEVERE, "Something went wrong trying to fetch UserEntry", e);
            return null;
        }
        return null;
    }



    public boolean createUserTable(String userid){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("" +
                "create table user_"+userid+"\n"+
                "(\n" +
                "    sess_no      int auto_increment,\n" +
                "    sess_id      TEXT not null,\n" +
                "    starttime    DATETIME default CURRENT_DATE() not null,\n" +
                "    endtime      DATETIME default CURRENT_DATE() not null,\n" +
                "    leader       long     default 0              not null,\n" +
                "    participants TEXT                            null,\n" +
                "    time_waited  int      default 0              not null,\n" +
                "    time_played  int      default 0              not null,\n" +
                "    constraint sess_key\n" +
                "        primary key (sess_no)\n" +
                ");")){
            int inserted = statement.executeUpdate();
            if(inserted > 0) return true;
            return false;
        } catch (SQLException e) {
            logger.warn("Couldn't create table UserEntry. \n Stacktrace: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /*
create table useridname
(
    sess_no      int auto_increment,
    sess_id      TEXT not null,
    starttime    DATETIME default CURRENT_DATE() not null,
    endtime      DATETIME default CURRENT_DATE() not null,
    leader       long     default 0              not null,
    participants TEXT                            null,
    time_waited  int      default 0              not null,
    time_played  int      default 0              not null,
    constraint sess_key
        primary key (sess_no)
);
*/
}

package me.tund.database;

import io.github.cdimascio.dotenv.Dotenv;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import me.tund.utils.Utilities;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    public Connection con;


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

    public boolean addUserEntry(String uuid, String username, boolean[] brs, String preferred_unit, int preferred_br, String activity, double kd, boolean replace ){
        con = Utilities.checkValidConnection(this.con);
        try (PreparedStatement statement = con.prepareStatement("""
        INSERT INTO user_track(discord_id, in_game_name, `13.0`, `12.0`, 
                       `11.0`, `10.0`, `9.0`, `8.0`, `7.0`, `6.0`, `5.0`, 
                       `4.0`, prefered_unit, preferred_br, activity, kd,
                       `replace`)
        VALUES (?, ?, ? , ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """)) {
            statement.setString(1, uuid);
            statement.setString(2, username);
            int d = 3;
            for (int i = 10; i >= 0; i-- ){
                statement.setBoolean(d, brs[i]);
                d++;
            }
            statement.setString(13, preferred_unit);
            statement.setInt(14, preferred_br);
            statement.setString(15, activity);
            statement.setDouble(16, kd);
            statement.setBoolean(17, replace);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }catch(Exception e){
            Logger.getLogger("Database").log(Level.SEVERE, "Something went wrong trying to add a UserEntry. \n Stacktrace: "+ e.getStackTrace());
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
            Logger.getLogger("Database").log(Level.SEVERE, "Something went wrong trying to add a UserEntry.", e);
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
                boolean[] brs = {};
                for(int i = 0; i<10; i++){
                    brs[i] = rs.getBoolean(String.valueOf(i+4)+".0");
                }
                String preferred_unit = rs.getString("preferred_unit");
                double preferred_br = rs.getDouble("preferred_br");
                int activity = rs.getInt("activity");
                double kd = rs.getDouble("kd");
                boolean replace = rs.getBoolean("replace");
                int trainings = rs.getInt("trainings");
                int cookies = rs.getInt("cookies");
                SquadMember sq = new SquadMember(user_no, discord_id, in_game_name, brs, preferred_unit, preferred_br, activity, trainings, cookies, kd, replace);
                return sq;
            }
        }catch (Exception e){
            Logger.getLogger("Database").log(Level.SEVERE, "Something went wrong trying to fetch UserEntry", e);
            return null;
        }
        return null;
    }

}

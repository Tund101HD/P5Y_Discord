package me.tund.utils.sessions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.tund.database.Database;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionHandler {

    private List<Session> sessions = new ArrayList<>();
    private HashMap<Session, ScheduledFuture> sessionMap = new HashMap<>();
    private Database db = new Database();
    private final Gson gson = new Gson();
    private ScheduledExecutorService executorService;
    private Writer writer;

    public SessionHandler() {
        try {
            writer = new FileWriter("src/main/resources/active-sessions.json");
        }catch (Exception e){
            Logger.getLogger("SessionHandler").log(Level.WARNING, "Couldn't find active-session.json in resources.", e);
            try{
                File file = new File("src/main/resources/active-sessions.json");
                file.createNewFile();
                writer = new FileWriter(file);
            }catch (Exception e2){
                Logger.getLogger("SessionHandler").log(Level.WARNING, "Failed to initialize active-session.json. Guess we're fucked now! :) No saving YAY", e);
            }
        }
        loadSessionsFromJson();
        startExecutorService();
    }

    private JsonObject convertSessionToJson(Session session) {

        //TODO Add Conversion

        return new JsonObject();
    }
    private JsonObject saveSessionToJson(JsonObject session) {


        return session;
    }
    private void removeSessionFromJson(JsonObject session) {

    }

    private List<Session> loadSessionsFromJson() { // In case the Bot crashed and there were open sessions

        return new ArrayList<>();
    }


    private void startExecutorService(){
        executorService = Executors.newScheduledThreadPool(5);
        for (Session session : sessions) {
            SessionRefreshTask s = new SessionRefreshTask(session, this);
            sessionMap.put(session, executorService.scheduleAtFixedRate(s ,0,1000, TimeUnit.MILLISECONDS));
        }
        Logger.getLogger("SessionHandler").log(Level.INFO, "Sessions have been loaded and tasks have been scheduled. Current number of sessions: "+sessions.size());
    }


    public List<Session> getSessions() {
        return sessions;
    }
    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
    public void addSession(Session session) {
        sessions.add(session);
    }
    public void removeSession(Session session) {
        sessions.remove(session);
    }

    public void updateSession(Session session) {
        for(Session s : sessions){
            if(s.getSession_id().equals(session.getSession_id())){
                sessions.set(sessions.indexOf(s), session);
            }
        }
        saveSessionToJson(convertSessionToJson(session));
    }

    public void saveAndCloseSession(Session session) {
        DateTime time = new DateTime(System.currentTimeMillis(), DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00")));
        session.setEnd_time(new Date(time.getMillis()));
        removeSessionFromJson(convertSessionToJson(session)); //remove session with ID
        sessionMap.get(session).cancel(true);
        boolean success =db.addSessionEntry(session.getStart_time().toString(), session.getEnd_time().toString(),
               session.getParticipants().toArray(new Long[100]), session.getParticipant_time_played(),
                session.getParticipant_time_waited(), session.getTotal_rounds(),
                (double)(session.getWins()/session.getTotal_rounds()), session.getBattle_rating());
        if(success)
            Logger.getLogger("SessionHandler").log(Level.INFO, "Session " + session.getSession_id() + " has been saved and closed. Closing time: "+ time);
        else
            Logger.getLogger("SessionHandler").log(Level.WARNING, "Session " + session.getSession_id() + ": Failed to save Session to Database! Timestamp:"+ time);
    }
}

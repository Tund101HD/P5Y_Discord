package me.tund.utils.sessions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionHandler {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-Sessionhandler-Client");
    private List<Session> sessions = new ArrayList<>();
    public  List<SquadMember> waiting = new ArrayList<>();

    public HashMap<SquadMember, List<Session>> waiting_sessions = new HashMap<>();
    private HashMap<Session, ScheduledFuture> sessionMap = new HashMap<>();
    private ScheduledFuture waitingSession = null;

    private Database db = new Database();
    private final Gson gson = new Gson();
    private ScheduledExecutorService executorService;
    private Writer writer;

    public SessionHandler() {
        logger.debug("Initializing SessionHandler; Searching for active-sessions.json");
        try {
            writer = new FileWriter("src/main/resources/active-sessions.json");
        }catch (Exception e){
            logger.warn("Couldn't find active-session.json in resources. Trying to create file again.");
            try{
                File file = new File("src/main/resources/active-sessions.json");
                file.createNewFile();
                writer = new FileWriter(file);
            }catch (Exception e2){
                logger.error("Failed to initialize active-session.json. Stacktrace: {}", e2.getStackTrace());}
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
        waitingSession = executorService.scheduleAtFixedRate(new SessionWaitingRefreshTask(),0,1000, TimeUnit.MILLISECONDS);
        for (Session session : sessions) {
            SessionRefreshTask s = new SessionRefreshTask(session, this);
            sessionMap.put(session, executorService.scheduleAtFixedRate(s ,0,1000, TimeUnit.MILLISECONDS));
        }
        logger.debug("Sessions have been loaded and tasks have been scheduled. Current number of sessions: {}", sessions.size());
    }

    public Session getSessionById(String sessionId) {
        for(Session session : sessions) {
            if(session.getSession_id().equals(sessionId)) return session;
        }
        return null;
    }

    public Session getSessionByLeader(long leaderId){
        for(Session session : sessions) {
            if(session.getLeader_id() == leaderId) return session;
        }
        return null;
    }

    public List<Session> getSessions() {
        return sessions;
    }
    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
    public void addSession(Session session) {
        sessions.add(session);
        if(executorService == null) executorService = Executors.newScheduledThreadPool(5);
        logger.debug("Starting Session ({}) on a new thread. Current number of active Sessions minus WaitingRefresh is: {}", session.getSession_id(), sessions.size());
        sessionMap.put(session, executorService.scheduleAtFixedRate(new SessionRefreshTask(session, this),0,1000, TimeUnit.MILLISECONDS));

    }
    public void removeSession(Session session) {
        sessions.remove(session);
    }
    public Session getSessionByUser(long id){
        for(Session session : sessions) {
            if(session.getActive_participants().contains(id)) return session;
        }
        return null;
    }

    /**
     * Updates a specific session's value in memory. Needs to be called everytime a value (Like the Active_Participants) changes.
     * @param session The Session to be updated
     */
    public void updateSession(Session session) {
        for(Session s : sessions){
            if(s.getSession_id().equals(session.getSession_id())){
                sessions.set(sessions.indexOf(s), session);
            }
        }
        saveSessionToJson(convertSessionToJson(session));
    }

    public boolean saveAndCloseSession(Session session) {
        DateTime time = new DateTime(System.currentTimeMillis(), DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00")));
        session.setEnd_time(new Date(time.getMillis()));
        removeSessionFromJson(convertSessionToJson(session)); //remove session with ID
        sessionMap.get(session).cancel(true);
        removeSession(session);
        boolean success =db.addSessionEntry(session.getStart_time().toString(), session.getEnd_time().toString(),
               session.getParticipants().toArray(new Long[255]), session.getParticipant_time_played(),
                session.getParticipant_time_waited(), session.getTotal_rounds(),
                (double)(session.getWins()/session.getTotal_rounds()), session.getBattle_rating());
        if(success)
            logger.info("Session {} has been saved and closed. Closing time: {}", session.getSession_id(), time);
        else
            logger.warn("Session {}: Failed to save Session to Database! Timestamp:{}", session.getSession_id(), time);
        return success;
    }
}

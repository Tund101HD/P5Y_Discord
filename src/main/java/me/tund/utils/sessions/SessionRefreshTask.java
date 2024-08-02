package me.tund.utils.sessions;

import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

public class SessionRefreshTask implements Runnable {

    private final Session session;
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("SessionRefreshClient");

    public SessionRefreshTask(Session session, SessionHandler handler) {
        this.session = session;
        this.handler = handler;
    }



    @Override
    public void run() {
        Session session = this.session;
        /*TODO
            - Check for waiting users in the waiting_room and compare to active_participants!
            - Update all values that have changed; Check the channels, check list of users
            - If channel doesn't have 8 participants, check for users in the waiting channel. ( add delay 10 Seconds)
            - Only add to user_play_time when isActive() results in true
        */
        if(!session.isActive()){
            logger.debug("Session {} is currently waiting.", session.getSession_id());
            if(session.getActive_participants().size() >= 8){
                session.setActive(true);
            }
        }else{

        }
        handler.updateSession(session);
        if(session.isClosing) {
           handler.saveAndCloseSession(session);
        }
    }
}

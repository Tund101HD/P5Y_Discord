package me.tund.utils.sessions;

import java.util.logging.Logger;

public class SessionRefreshTask implements Runnable {

    private final Session session;
    private final SessionHandler handler;

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
        */
        if(!session.isActive()){
            Logger.getLogger("Session "+session.getSession_id()+" is currently waiting.");
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

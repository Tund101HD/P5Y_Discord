package utils.sessions;

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
        //TODO data refresh


        handler.updateSession(session);
        if(session.isClosing) {
           handler.saveAndCloseSession(session);
        }
    }
}

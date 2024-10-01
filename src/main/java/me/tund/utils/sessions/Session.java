package me.tund.utils.sessions;

import java.util.*;

public class Session {

    private final String session_id;
    private final Date start_time;// UUID for given Session
    private final double battle_rating;
    private long leader_id;
    private boolean isSqaudOne = true;

    protected boolean STATUS = false;// Is the Session active or currently waiting?
    protected long LAST_ACTIVE = 0;
    protected boolean isClosing = false; // Is the session marked for close?
    private Date end_time;
    private List<Long> participants = new ArrayList<>();
    private List<Long> active_participants = new ArrayList<>();
    private HashMap<Long, Integer> participant_time_played = new HashMap<>();
    private HashMap<Long, Integer> participant_time_waited = new HashMap<>();
    private int total_rounds = 0;
    private double wins = 0;
    private List<Long> exclude_ids;
    private int min_acitivty;
    private int min_priority;


    public Session(Date start_time, double battle_rating, long leader_id) {
        session_id = new UUID(System.currentTimeMillis(), System.currentTimeMillis()+UUID.randomUUID().getLeastSignificantBits()).toString();
        this.start_time = start_time;
        this.battle_rating = battle_rating;
        this.leader_id = leader_id;
    }

    public Session(Session s) {
        this.session_id = s.session_id;
        this.start_time = s.start_time;
        this.battle_rating = s.battle_rating;
        this.STATUS = s.STATUS;
        this.leader_id = s.leader_id;
        this.participants = s.participants;
        this.active_participants = s.active_participants;
        this.participant_time_played = s.participant_time_played;
        this.participant_time_waited = s.participant_time_waited;
        this.total_rounds = s.total_rounds;
        this.wins = s.wins;
        this.isClosing = s.isClosing;
        this.end_time = s.end_time;
        this.exclude_ids = s.exclude_ids;
        this.min_acitivty = s.min_acitivty;
        this.min_priority = s.min_priority;

    }

    public Session closeSession(Date end_time) {
        this.end_time = end_time;
        isClosing = true;
        return this;
    }


    public Session copySession(){
        return new Session(this);
    }
    public String getSession_id() {
        return session_id;
    }

    public boolean isActive() {
        return STATUS;
    }

    public void setActive(boolean STATUS) {
        this.STATUS = STATUS;
    }

    public Date getStart_time() {
        return start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public List<Long> getParticipants() {
        return participants;
    }

    // Alle Teilnehmer der Session, egal ob gewartet oder gespielt
    public List<Long> setParticipants(List<Long> participants) {
        this.participants = participants;
        return participants;
    }
    public List<Long> addParticipant(Long participant) {
        participants.add(participant);
        return participants;
    }
    public List<Long> removeParticipant(Long participant) {
        participants.remove(participant);
        return participants;
    }


    // Alle geraden aktiven Teilnehmer
    public List<Long> getActive_participants() {
        return active_participants;
    }

    public List<Long> setActive_participants(List<Long> active_participants) {
        this.active_participants = active_participants;
        return active_participants;
    }
    public List<Long> addActive_participant(Long participant) {
        active_participants.add(participant);

        return active_participants;
    }
    public List<Long> removeActive_participant(Long participant) {
        active_participants.remove(participant);
        return active_participants;
    }


    // Gespielte Zeit eines jeden Teilnehmers
    public HashMap<Long, Integer> getParticipant_time_played() {
        return participant_time_played;
    }

    public HashMap<Long, Integer> setParticipant_time_played(HashMap<Long, Integer> participant_time_played) {
        this.participant_time_played = participant_time_played;
        return participant_time_played;
    }

    public HashMap<Long, Integer> updateParticipant_time_played(Long participant, int time_played) {
        participant_time_played.put(participant, time_played);
        return participant_time_played;
    }

    // Gewartete Zeit in der Session eines Teilnehmers
    public HashMap<Long, Integer> getParticipant_time_waited() {
        return participant_time_waited;
    }

    public  HashMap<Long, Integer> setParticipant_time_waited(HashMap<Long, Integer> participant_time_waited) {
        this.participant_time_waited = participant_time_waited;
        return participant_time_waited;
    }
    public  HashMap<Long, Integer> updateParticipant_time_waited(Long participant, int time_waited) {
        participant_time_waited.put(participant, time_waited);
        return participant_time_waited;
    }



    public int getTotal_rounds() {
        return total_rounds;
    }

    public void setTotal_rounds(int total_rounds) {
        this.total_rounds = total_rounds;
    }

    public double getWins() {
        return wins;
    }

    public void setWins(double wins) {
        this.wins = wins;
    }

    public double getBattle_rating() {
        return battle_rating;
    }

    public long getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(long leader_id) {
        this.leader_id = leader_id;
    }

    public List<Long> getExclude_ids() {
        return exclude_ids;
    }

    public void setExclude_ids(List<Long> exclude_ids) {
        this.exclude_ids = exclude_ids;
    }

    public int getMin_acitivty() {
        return min_acitivty;
    }

    public void setMin_acitivty(int min_acitivty) {
        this.min_acitivty = min_acitivty;
    }

    public int getMin_priority() {
        return min_priority;
    }

    public void setMin_priority(int min_priority) {
        this.min_priority = min_priority;
    }

    public boolean isClosing() {
        return isClosing;
    }

    public long getLAST_ACTIVE() {
        return LAST_ACTIVE;
    }

    public void setLAST_ACTIVE(long LAST_ACTIVE) {
        this.LAST_ACTIVE = LAST_ACTIVE;
    }

    public boolean isSqaudOne() {
        return isSqaudOne;
    }

    public void setSqaudOne(boolean sqaudOne) {
        isSqaudOne = sqaudOne;
    }
}

package me.tund.utils.sessions;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SessionRefreshTask implements Runnable {

    private final Session session;
    private final SessionHandler handler;
    private static Logger logger;
    private Database db = new Database();

    public SessionRefreshTask(Session session, SessionHandler handler) {
        this.session = session;
        this.handler = handler;
        this.logger=LoggerFactory.getLogger("SessionRefreshClient-"+session.getSession_id());
        logger.debug("Session has been successfully started. Current activity: {} ; Current members: {} ; Time is: {}",
                session, session.getActive_participants(), System.currentTimeMillis());
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
            if(session.getActive_participants().size() >= 8){
                logger.debug("Session is now active. ID: {} ; Timestamp: {}", session.getSession_id(), System.currentTimeMillis());
                session.setActive(true);
            }else{
                if(System.currentTimeMillis() - session.getLAST_ACTIVE() > 10000){
                    List<SquadMember> waiting = handler.waiting;
                    List<SquadMember> members = new ArrayList<>();
                    List<SquadMember> suitable = new ArrayList<>();
                    for(SquadMember member : waiting){
                        if(member.getActivity() >= session.getMin_acitivty() && member.getPriority() >= session.getMin_priority()){
                            suitable.add(member);
                        }
                    }
                    for(long l : session.getActive_participants()){
                        members.add(db.getSquadMemberById(l));
                    }

                    /**
                     *  Evaluates the internal rating of a member using a formula. Internal Rating is dynamically calculated using user stats
                     *  and average rating of the squad to assure a good fit to the squad. Fitness is OBJECTIVE except the priority given by
                     *  squad leaders.
                     */
                    while(session.getActive_participants().size() < 8 && suitable.size() > 0){
                        HashMap<SquadMember, Double> internal_rating = new HashMap<>();
                        double average_rating = 0;
                        for (SquadMember sq : members) {
                            average_rating += sq.getActivity();
                        }
                        average_rating /= members.size();
                        for (SquadMember sq : suitable) {
                            double rating = sq.getPriority() * ((sq.getActivity() * Math.pow((sq.getKd() + 0.3), 2)) + sq.getCookies() + 0.5 * (sq.getTrainings()) / 2.5 * average_rating);
                            internal_rating.put(sq, rating);
                        }
                        List<Double> s = new ArrayList<>();
                        for (Map.Entry<SquadMember, Double> entry : internal_rating.entrySet()) {
                            s.add(entry.getValue());
                        }
                        LinkedHashMap<SquadMember, Double> sortedMap = new LinkedHashMap();
                        Collections.sort(s);
                        for (double num : s) {
                            for (Map.Entry<SquadMember, Double> entry : internal_rating.entrySet()) {
                                if (entry.getValue().equals(num)) {
                                    sortedMap.put(entry.getKey(), num);
                                }
                            }
                        }
                        SquadMember m = sortedMap.keySet().iterator().next();
                        Member suitable_m = Main.bot.getGuildById(Main.GUILD_ID).getMemberById(m.getDiscord_id());
                        Member leader = Main.bot.getGuildById(Main.GUILD_ID).getMemberById(session.getLeader_id());

                        if(suitable_m.getVoiceState().isDeafened()){ //TODO ADD AFK-Channel !!
                            handler.waiting.remove(m);
                            continue;
                        }

                        // Check if current Squad is Squad 1 or Squad 2 to move member.
                        if(session.isSqaudOne() ){
                            if(m.getPreferred_unit().equalsIgnoreCase("ground")){
                                Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(suitable_m, Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                                if(!session.getParticipants().contains(m.getDiscord_id())) session.addParticipant(m.getDiscord_id());
                                session.addActive_participant(m.getDiscord_id());

                            }else{
                                Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(suitable_m, Main.bot.getVoiceChannelById(Main.SQUAD1_AIR)).queue();
                                if(!session.getParticipants().contains(m.getDiscord_id())) session.addParticipant(m.getDiscord_id());
                                session.addActive_participant(m.getDiscord_id());
                            }
                        }else{
                            if(m.getPreferred_unit().equalsIgnoreCase("ground")){
                                Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(suitable_m, Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                                if(!session.getParticipants().contains(m.getDiscord_id())) session.addParticipant(m.getDiscord_id());
                                session.addActive_participant(m.getDiscord_id());
                            }else{
                                Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(suitable_m, Main.bot.getVoiceChannelById(Main.SQUAD2_AIR)).queue();
                                if(!session.getParticipants().contains(m.getDiscord_id())) session.addParticipant(m.getDiscord_id());
                                session.addActive_participant(m.getDiscord_id());
                            }
                        }
                        suitable.remove(m);
                    }
                    if(session.getActive_participants().size() >= 8){
                        session.setActive(true);
                    }
                }
            }
        }else{
            List<Long> l = session.getActive_participants();
            if(session.isSqaudOne()){
                for(long l1 : l){
                    if(l1 == session.getLeader_id()) continue;
                    Member m = Main.bot.getGuildById(Main.GUILD_ID).getMemberById(l1);
                    if(!Main.bot.getGuildById(Main.GUILD_ID).getVoiceChannelById(Main.SQUAD1_GROUND).getMembers().contains(m) && !Main.bot.getGuildById(Main.GUILD_ID).getVoiceChannelById(Main.SQUAD1_AIR).getMembers().contains(m) || m.getVoiceState().isDeafened()){
                        session.removeActive_participant(l1);
                        if(m.getVoiceState().inAudioChannel() && !m.getVoiceState().isDeafened()) handler.waiting.add(db.getSquadMemberById(l1));
                    }
                }
            }else{
                for(long l1 : l){
                    if(l1 == session.getLeader_id()) continue;
                    Member m = Main.bot.getGuildById(Main.GUILD_ID).getMemberById(l1);
                    if(!Main.bot.getGuildById(Main.GUILD_ID).getVoiceChannelById(Main.SQUAD2_GROUND).getMembers().contains(m) && !Main.bot.getGuildById(Main.GUILD_ID).getVoiceChannelById(Main.SQUAD2_AIR).getMembers().contains(m)){
                        session.removeActive_participant(l1);
                        if(m.getVoiceState().inAudioChannel()) handler.waiting.add(db.getSquadMemberById(l1));
                    }
                }
            }


            if(session.getActive_participants().size() <= 8){
                session.setActive(false);
                session.setLAST_ACTIVE(System.currentTimeMillis());
            }

            // Playtime for active players in the channels.
            HashMap<Long, Integer> map = session.getParticipant_time_played();
            List<Long> participants = session.getActive_participants();
            List<Long> participants2 = session.getParticipants();

            for(long l2 : participants){

                if (!participants2.contains(l2)){
                    participants2.add(l2);
                }
                int played;
                if(!map.containsKey(l2)){
                    played = 0;
                }else{
                    played = map.get(l2);
                }
                played++;
                map.put(l2, played);
            }
            session.setParticipant_time_played(map);

            // Waittime for users not in the channel.
            List<SquadMember> waiting = handler.waiting;
            HashMap<Long, Integer> map2 = session.getParticipant_time_played();

            for (SquadMember member : waiting) {
                if(member.getActivity() >= session.getMin_acitivty() && member.getPriority() >= session.getMin_priority()){
                    if (!participants2.contains(member.getDiscord_id())){
                        participants2.add(member.getDiscord_id());
                    }
                    int waited;
                    if(!map2.containsKey(member.getDiscord_id())){
                        waited = 0;
                    }else{
                        waited = map2.get(member.getDiscord_id());
                    }
                    waited++;
                    map.put(member.getDiscord_id(), waited);
                }
            }
            session.setParticipant_time_waited(map2);
            session.setParticipants(participants2);

        }
        handler.updateSession(session);
        if(session.isClosing) {
           handler.saveAndCloseSession(session);
        }
    }
}

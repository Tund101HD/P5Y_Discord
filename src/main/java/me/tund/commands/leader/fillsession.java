package me.tund.commands.leader;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.Utilities;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class fillsession extends ListenerAdapter {
    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-fillsession-command");
    private final SessionHandler handler;
    public fillsession(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if(!event.getName().equalsIgnoreCase("fillsession"))return;
        event.deferReply().setEphemeral(true).queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        Session activeSession = null;
        for(Session session : handler.getSessions()) {
            if(session.getLeader_id() == event.getMember().getIdLong()) activeSession = session;
        }
        logger.info("Trying to fill Session for Squad-Leader {}", event.getMember().getEffectiveName());
        String minact = (!event.getOptions().contains("min-activity"))
                ? String.valueOf(activeSession.getMin_acitivty()) : event.getOption("min-activity").getAsString(); // min activity
        double br;
        try {
            br = (event.getOption("br").getAsString() == null || !event.getOptions().contains("br"))
                    ? 13.0d : Double.parseDouble(event.getOption("br").getAsString()); //br
        } catch (Exception e) {
            br = activeSession.getBattle_rating();
        }
        String exlude_id = (!event.getOptions().contains("exclude_id")) ? "" : event.getOption("exlude_id").getAsString(); //CSV von IDs
        String min_priority = (!event.getOptions().contains("min-priority"))
                ? String.valueOf(activeSession.getMin_priority()) : event.getOption("min-priority").getAsString(); //minprio

        if(activeSession == null){
            logger.info("This Squad-Leader doesn't have a Session to fill! ({})", event.getMember().getEffectiveName());
            event.getHook().editOriginal("Sorry, aber du besitzt keinen Squad mit einer Session! Bitte führe zuerst '/startsession' aus!").queue();
            return;
        }
        activeSession.setLocked(true);
        handler.updateSession(activeSession);
        //Search in the waiting channel for suitable people. TODO Add people from SessionHadler.waiting // FIXME Is nuking the DB worth it -- just check for role?
        List<SquadMember> sqs = new ArrayList<>();
        for (Member m : Main.bot.getVoiceChannelById(Main.WARTERAUM_ID).getMembers()) {
            SquadMember sq = db.getSquadMemberById(m.getIdLong());
            if (sq != null)
                sqs.add(sq);
        }
        sqs.addAll(Main.sessionHandler.waiting);
        try{
            for (SquadMember sq : sqs) {
                activeSession.addParticipant(sq.getDiscord_id());
                logger.info("Adding Squad-Member to Session-List with the id {}", sq.getDiscord_id());
                // TODO Loop through all participants that are not active and check for wait in channel in updateSession() !
                if (!(sq.getActivity() >= Integer.parseInt(minact)) || !(sq.getPriority() >= Integer.parseInt(min_priority))) {  // FIXME Convert boolean to double for BR check  && Arrays.asList(sq.getBrs()).contains(session.getBattle_rating()) || !session.getExclude_ids().contains(sq.getDiscord_id())
                    logger.info("Removing Squad-Member from Session-List with the id {}. Activity is: {}, Priority is {} ", sq.getDiscord_id(), Integer.parseInt(minact), Integer.parseInt(min_priority));
                    sqs.remove(sq);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (sqs.size() > (7-activeSession.getActive_participants().size())) {
            HashMap<SquadMember, Double> internal_rating = new HashMap<>();
            double average_rating = 0;
            for (SquadMember sq : sqs) {
                average_rating += sq.getActivity();
            }
            average_rating /= sqs.size();
            for (SquadMember sq : sqs) {
                double rating = sq.getPriority() * ((sq.getActivity() * Math.pow((sq.getKd() + 0.3), 2)) + sq.getCookies() + 0.5 * (sq.getTrainings()) / 2.5 * average_rating);
                internal_rating.put(sq, rating);
            }
            List<Double> s = new ArrayList<>();
            for (Map.Entry<SquadMember, Double> entry : internal_rating.entrySet()) {
                s.add(entry.getValue());
            }
            LinkedHashMap<SquadMember, Double> sortedMap = new LinkedHashMap();
            Collections.sort(s);
            s.subList(0, sqs.size() - 7 + 1).clear();
            for (double num : s) {
                for (Map.Entry<SquadMember, Double> entry : internal_rating.entrySet()) {
                    if (entry.getValue().equals(num)) {
                        sortedMap.put(entry.getKey(), num);
                    }
                }
            }

            for (Map.Entry<SquadMember, Double> entry : sortedMap.entrySet()) {
                // Move into the corresponding channels, determined by preferred unit.
                if (entry.getKey().getPreferred_unit().equalsIgnoreCase("ground")) {
                    if (Utilities.isSquadOne(activeSession.getLeader_id())) {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                    } else {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                    }
                } else {
                    if (Utilities.isSquadOne(activeSession.getLeader_id())) {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD1_AIR)).queue();
                    } else {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD2_AIR)).queue();
                    }
                }
                Session finalActiveSession = activeSession;
                Main.bot.getUserById(entry.getKey().getDiscord_id()).openPrivateChannel().
                        flatMap(channel -> channel.sendMessage("Danke für's warten. Ein Squadleader hat seine Session aufgefüllt und du wurdest automatisch" +
                                "aus dem Wartebereich in einen Channel nachgerückt, der zu deiner Präferenz passt. " +
                                "Bitte tausche dich mit " + Main.bot.getUserById(finalActiveSession.getLeader_id()).getEffectiveName() + " aus, welche Rolle du einnehmen sollst.")).queue();
                activeSession.addActive_participant(entry.getKey().getDiscord_id());

            }
            activeSession.setActive(true);
            activeSession.setLocked(false);
            if(Utilities.isSquadOne(activeSession.getLeader_id())) activeSession.setSqaudOne(true);
            else activeSession.setSqaudOne(false);
            handler.updateSession(activeSession);
        } else if (sqs.isEmpty()) {
            logger.info("List of Squad-Members is empty. Not moving anyone.");

            Main.bot.getUserById(activeSession.getLeader_id()).openPrivateChannel().flatMap(
                    channel -> channel.sendMessage("Der Wartebereich hat keine Nutzer, die für den Squad ready sind. " +
                            "Warte bis Spieler manuell joinen oder nutze ``/fillsession`` mit angepassten Werten. Um die Session wieder zu schließen nutze " +
                            "``/endsession``. Bitte beachte das Nachrichten und Tags nicht wieder zurückgezogen werden können!")).queue();
            activeSession.setActive(false);
            activeSession.setLocked(false);
            handler.updateSession(activeSession);
        } else {
            logger.info("Moving members into active Session!");
            for (SquadMember sq : sqs) {
                activeSession.addParticipant(sq.getDiscord_id());
                activeSession.addActive_participant(sq.getDiscord_id());

                // Move into the corresponding channels, determined by preferred unit.
                if (sq.getPreferred_unit().equalsIgnoreCase("ground")) {
                    if (Utilities.isSquadOne(activeSession.getLeader_id())) {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                    } else {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                    }
                } else {
                    if (Utilities.isSquadOne(activeSession.getLeader_id())) {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD1_AIR)).queue();
                    } else {
                        Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                Main.bot.getVoiceChannelById(Main.SQUAD2_AIR)).queue();
                    }
                }
                Session finalActiveSession1 = activeSession;
                Main.bot.getUserById(sq.getDiscord_id()).openPrivateChannel().
                        flatMap(channel -> channel.sendMessage("Danke für's warten. Ein Squadleader hat eine Session aufgefüllt und du wurdest automatisch" +
                                "aus dem Wartebereich in einen Channel nachgerückt, der zu deiner Präferenz passt. " +
                                "Bitte tausche dich mit " + Main.bot.getUserById(finalActiveSession1.getLeader_id()).getEffectiveName() + " aus, welche Rolle du einnehmen sollst.")).queue();
                activeSession.addActive_participant(sq.getDiscord_id());
                activeSession.setLocked(false);
                handler.updateSession(activeSession);
            }
            if(activeSession.getActive_participants().size() < 8){
                Main.bot.getUserById(activeSession.getLeader_id()).openPrivateChannel().flatMap(
                        channel -> channel.sendMessage("Der Wartebereich hat nicht genug Nutzer, die für den Squad ready sind. " +
                                "Warte bis Spieler manuell joinen oder nutze ``/fillsession`` mit angepassten Werten. Um die Session wieder zu schließen nutze " +
                                "``/endsession``. Bitte beachte das Nachrichten und Tags nicht wieder zurückgezogen werden können!")).queue();
            }
        }

    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("fillsession")) {
            switch (event.getFocusedOption().getName()) {
                case "min-activity":
                    String[] words = new String[]{"500", "800", "1000", "1200", "0"};
                    List<Command.Choice> options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                            .map(word -> new Command.Choice(word, word)) // map the words to choices
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "br":
                    words = new String[]{"13.0", "12.0", "11.0", "10.0", "9.0", "8.0", "7.0", "6.0", "5.0", "4.0"};
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "exclude_id":
                    words = new String[]{"1146100281273233441"}; //FIXME Liste an gebannten Nutzern aus der Vergangenheit
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "min-priority":
                    words = new String[]{"3", "2", "1"}; //Imagine
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
            }


        }
    }
}

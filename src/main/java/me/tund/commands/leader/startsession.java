package me.tund.commands.leader;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import me.tund.utils.sessions.Session;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class startsession extends ListenerAdapter {
    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("StartSessionCommand");
    public startsession() {

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("startsession")) {

            event.deferReply().setEphemeral(true).queue();
            if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.GUILD_ID))) { //FIXME Replace with role id
                event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
                return;
            }
            logger.info("Trying to start Session for Squad-Leader {}", event.getMember().getEffectiveName());
            String minact = (event.getOption("min-activity").getAsString() == null || event.getOption("min-activity").getAsString().isEmpty())
                    ? "0" : event.getOption("min-activity").getAsString(); // min activity
            double br;
            try {
                br = (event.getOption("br").getAsString() == null || event.getOption("br").getAsString().isEmpty())
                        ? 13.0d : event.getOption("br").getAsDouble(); //br
            } catch (Exception e) {
                br = 13.0d;
            }
            String exlude_id = event.getOption("exlude_id").getAsString() == null ? "" : event.getOption("exlude_id").getAsString(); //CSV von IDs
            String min_priority = (event.getOption("min-priority").getAsString() == null || event.getOption("min-priority").getAsString().isEmpty())
                    ? "0" : event.getOption("min-priority").getAsString(); //minprio

            Session session = new Session(new Date(new DateTime(System.currentTimeMillis(),
                    DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00"))).getMillis()), br, event.getMember().getIdLong());
            session.setExclude_ids(Lists.transform(Arrays.asList(exlude_id.split(";")), Longs.stringConverter()));
            try {
                session.setMin_acitivty(Integer.parseInt(minact));
            } catch (Exception e) {
                session.setMin_acitivty(0);
            }
            try {
                session.setMin_priority(Integer.parseInt(min_priority));
            } catch (Exception e) {
                session.setMin_priority(0);
            }
            Main.sessionHandler.addSession(session);
            session.addParticipant(session.getLeader_id());
            session.addActive_participant(session.getLeader_id());
            event.getHook().editOriginal("Session wurde gestartet.").queue();
            logger.info("Session has been added to the handler. Now searching for participants by checking channels. ID={}", session.getSession_id());

            //Search in the waiting channel for suitable people. TODO Add people from SessionHadler.waiting
            List<SquadMember> sqs = new ArrayList<>();
            for (Member m : Main.bot.getVoiceChannelById(Main.WARTERAUM_ID).getMembers()) {
                SquadMember sq = db.getSquadMemberById(m.getIdLong());
                if (sq != null)
                    sqs.add(sq);
            }
            sqs.addAll(Main.sessionHandler.waiting);
            for (SquadMember sq : sqs) {
                session.addParticipant(sq.getDiscord_id()); // TODO Loop through all participants that are not active and check for wait in channel in updateSession() !
                if (!(sq.getActivity() >= session.getMin_acitivty()) || !(sq.getPriority() >= session.getMin_priority())
                        || !session.getExclude_ids().contains(sq.getDiscord_id()) && Arrays.asList(sq.getBrs()).contains(session.getBattle_rating())) {
                    sqs.remove(sq);
                }
            }

            // Effizientester Algorithmus EO WEST BRO WTF DAS IST EIN KRIEGSVERBRECHEN IN MANCHEN LÄNDERN
            if (sqs.size() > 7) {
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
                        if (isSquadOne(session.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND));
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND));
                        }
                    } else {
                        if (isSquadOne(session.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_AIR));
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(entry.getKey().getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_AIR));
                        }
                    }
                    Main.bot.getUserById(entry.getKey().getDiscord_id()).openPrivateChannel().
                            flatMap(channel -> channel.sendMessage("Danke für's warten. Ein Squadleader hat eine Session erstellt und du wurdest automatisch" +
                                    "aus dem Wartebereich in einen Channel gezogen der zu deiner Präferenz passt. " +
                                    "Bitte tausche dich mit " + Main.bot.getUserById(session.getLeader_id()).getEffectiveName() + " aus, welche Rolle du einnehmen sollst.")).queue();
                    session.addActive_participant(entry.getKey().getDiscord_id());

                }
                session.setActive(true);

            } else if (sqs.isEmpty()) {
                Main.bot.getUserById(session.getLeader_id()).openPrivateChannel().flatMap(
                        channel -> channel.sendMessage("Der Wartebereich hat keine Nutzer, die für den Squad ready sind. " +
                                "Warte bis Spieler manuell joinen oder nutze ``/fillsession`` mit angepassten Werten. Um die Session wieder zu schließen nutze " +
                                "``/endsession``. Bitte beachte das Nachrichten und Tags nicht wieder zurückgezogen werden können!")).queue();
                session.setActive(false);
            } else {
                for (SquadMember sq : sqs) {
                    session.addParticipant(sq.getDiscord_id());
                    session.addActive_participant(sq.getDiscord_id());

                    // Move into the corresponding channels, determined by preferred unit.
                    if (sq.getPreferred_unit().equalsIgnoreCase("ground")) {
                        if (isSquadOne(session.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND));
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND));
                        }
                    } else {
                        if (isSquadOne(session.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_AIR));
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_AIR));
                        }
                    }

                }
            }
        }
    }


    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("startsession")) {
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
                    words = new String[]{"1146100281273233441"}; //FIXME Liste and gebannten Nutzern aus der Vergangenheit
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

    public boolean isSquadOne(long leader_id) {
        return Main.bot.getVoiceChannelById(Main.SQUAD1_AIR).getMembers().contains(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(leader_id)) ||
                Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND).getMembers().contains(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(leader_id));
    }
}

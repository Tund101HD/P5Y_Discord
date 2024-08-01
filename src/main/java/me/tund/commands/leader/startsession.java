package me.tund.commands.leader;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import me.tund.utils.sessions.Session;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class startsession extends ListenerAdapter {
    private Database db = new Database();

    public startsession(){

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("startsession")) {
            event.deferReply().queue();
            String minact = (event.getOption("min-activity").getAsString()==null || event.getOption("min-activity").getAsString().isEmpty())
                        ?"0":event.getOption("min-activity").getAsString(); // min activity
            double br;
            try {
                br = (event.getOption("br").getAsString()==null || event.getOption("br").getAsString().isEmpty())
                        ?13.0d:event.getOption("br").getAsDouble(); //br
            }catch (Exception e){
                br = 13.0d;
            }
            String exlude_id = event.getOption("exlude_id").getAsString()==null?"":event.getOption("exlude_id").getAsString(); //CSV von IDs
            String min_priority = (event.getOption("min-priority").getAsString()==null || event.getOption("min-priority").getAsString().isEmpty())
                        ?"0":event.getOption("min-priority").getAsString(); //minprio

            Session session = new Session(new Date(new DateTime(System.currentTimeMillis(),
                    DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00"))).getMillis()), br, event.getMember().getIdLong());
            session.setExclude_ids(Lists.transform(Arrays.asList(exlude_id.split(";")), Longs.stringConverter()));
            try {
                session.setMin_acitivty(Integer.parseInt(minact));
            }catch (Exception e) {
                session.setMin_acitivty(0);
            }
            try {
                session.setMin_priority(Integer.parseInt(min_priority));
            }catch (Exception e) {
                session.setMin_priority(0);
            }
            Main.sessionHandler.addSession(session);


            //Search in the waiting channel for suitable people.
            List<SquadMember> sqs = new ArrayList<>();
            for(Member m : Main.bot.getVoiceChannelById(Main.WARTERAUM_ID).getMembers()){
                SquadMember sq = db.getSquadMemberById(m.getIdLong());
                if(sq != null)
                    sqs.add(sq);
            }

            for(SquadMember sq : sqs){
                session.addParticipant(sq.getDiscord_id()); // TODO Loop through all participants that are not active and check for wait in channel in updateSession() !
                if(!(sq.getActivity() >= session.getMin_acitivty()) || !(sq.getCookies() >= session.getMin_priority())
                    || !session.getExclude_ids().contains(sq.getDiscord_id()) || Arrays.asList(sq.getBrs()).contains(session.getBattle_rating())){
                    sqs.remove(sq);
                }
            }
            if(sqs.size() > 8){
                // TODO Add algorithm to decide who to add.
            }else if(sqs.isEmpty()){
                Main.bot.getUserById(session.getLeader_id()).openPrivateChannel().flatMap(
                        channel -> channel.sendMessage("Der Wartebereich hat keine Nutzer, die für den Squad ready sind. " +
                                "Warte bis Spieler manuell joinen oder nutze ``/fillsession`` mit angepassten Werten. Um die Session wieder zu schließen nutze " +
                                "``/endsession``. Bitte beachte das Nachrichten und Tags nicht wieder zurückgezogen werden können!")).queue();
            } else{
                for(SquadMember sq : sqs){
                    session.addParticipant(sq.getDiscord_id());
                    session.addActive_participant(sq.getDiscord_id());

                    // Move into the corresponding channels, determined by preferred unit.
                    if(sq.getPreferred_unit().equals("ground")){
                        if(isSquadOne(session.getLeader_id())){
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND));
                        }else{
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND));
                        }
                    } else{
                        if(isSquadOne(session.getLeader_id())){
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(sq.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_AIR));
                        }else{
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
            switch (event.getFocusedOption().getName()){
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

    public boolean isSquadOne(long leader_id){
        return  Main.bot.getVoiceChannelById(Main.SQUAD1_AIR).getMembers().contains(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(leader_id)) ||
                Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND).getMembers().contains(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(leader_id));
    }
}

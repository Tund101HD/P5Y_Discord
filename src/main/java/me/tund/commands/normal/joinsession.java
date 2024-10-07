package me.tund.commands.normal;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.Utilities;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class joinsession extends ListenerAdapter {
    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("JoinSessionCommand");
    public joinsession(SessionHandler handler) {
        this.handler = handler;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if(!event.getName().equalsIgnoreCase("joinsession"))return;
        event.deferReply().setEphemeral(true).queue();
        if(handler.waiting.contains(db.getSquadMemberById(event.getMember().getIdLong()))) {
            event.getHook().editOriginal("Sorry, aber du bist bereits auf der Warteliste! Bitte gedulde dich ein wenig.").queue();
            return;
        }
        for(Session s : handler.getSessions()){
            if(s.getActive_participants().contains(event.getMember().getIdLong()) || s.getLeader_id() == event.getMember().getIdLong()){
                event.getHook().editOriginal("Sorry, aber du bist bereits ein Teilnehmer!").queue();
                return;
            }
        }
        SquadMember member = db.getSquadMemberById(event.getMember().getIdLong());
        for(Session s : handler.getSessions()){
            if(!s.isActive() && !s.isClosing()){
                if(s.getMin_acitivty() < member.getActivity() && s.getMin_priority() < member.getPriority()){ //TODO Add unit+BR check!!!
                    if(!Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()).getVoiceState().inAudioChannel()){
                        Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().
                                flatMap(channel -> channel.sendMessage("Es wurde eine passende Session für dich gefunden! Bitte begib dich in einen Sprachkanal und führe den Befehl erneut aus um hinzugefügt zu werden.")).queue();
                        return;
                    }
                    if (member.getPreferred_unit().equalsIgnoreCase("ground")) {
                        if (Utilities.isSquadOne(s.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                        }
                    } else {
                        if (Utilities.isSquadOne(s.getLeader_id())) {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD1_AIR)).queue();
                        } else {
                            Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()),
                                    Main.bot.getVoiceChannelById(Main.SQUAD2_AIR)).queue();
                        }
                    }

                    Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().
                            flatMap(channel -> channel.sendMessage("Es wurde eine Session für dich gefunden, und du wurdest in einen Kanal gezogen." +
                                    "Bitte tausche dich mit " + Main.bot.getUserById(s.getLeader_id()).getEffectiveName() + " aus, welche Rolle du einnehmen sollst.")).queue();
                    s.addActive_participant(member.getDiscord_id());
                    handler.updateSession(s);
                    return;
                }
            }
        }
        Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().
                flatMap(channel -> channel.sendMessage("Es wurde leider keine passende Session für dich gefunden, in die du beitreten kannst, da du entweder nicht die Anforderungen triffst oder alle Sessions bereits voll sind.")).queue();
        if(!Main.bot.getGuildById(Main.GUILD_ID).getMemberById(member.getDiscord_id()).getVoiceState().inAudioChannel()){
            Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().
                    flatMap(channel -> channel.sendMessage("Da du in keinem Sprachkanal bist wurdest du nicht auf die Warteliste gesetzt. Bitte begib dich in einen Sprachkanal und führe den Befehl noch einmal aus, oder begib dich in den Warteraum um automatisch auf die Warteliste gesetzt zu werden. Du kannst danach in einen anderen Sprachkanal wechseln.")).queue();
            return;
        }
        Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().
                flatMap(channel -> channel.sendMessage("Du wurdest nun auf die Warteliste gesetzt um später automatisch gezogen zu werden. Falls du das nicht willst, dann verlasse bitte kurz den Sprachkanal.")).queue();
        handler.waiting.add(member);
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        // Maybe add Option for specific Session.
    }
}

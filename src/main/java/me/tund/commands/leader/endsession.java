package me.tund.commands.leader;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class endsession extends ListenerAdapter {
    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("EndSessionCommand");
    private final SessionHandler handler;
    public endsession(SessionHandler handler) {
        this.handler = handler;
    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if(!event.getName().equalsIgnoreCase("endsession")) return;
        event.deferReply().setEphemeral(true).queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        logger.info("Trying to end Session for Squad-Leader {}", event.getMember().getEffectiveName());
        Session activeSession = null;
        for(Session s : handler.getSessions()) {
            if(s.getLeader_id() == event.getMember().getIdLong()) {activeSession = s;}
        }
        if(activeSession == null) {
            event.getHook().editOriginal("Sorry, aber du scheinst keine Aktive Session zu besitzen.").queue();
            logger.info("This user doesn't own a Session to end. {}", event.getMember().getEffectiveName());
            return;
        }
        activeSession.setActive(false);
        activeSession.close();
        logger.info("Received end-request for Session {}", activeSession.getSession_id());
        handler.updateSession(activeSession);
        boolean success = handler.saveAndCloseSession(activeSession);
        if(success){
            for(Long l : activeSession.getParticipants()) {
                Main.bot.getGuildById(Main.GUILD_ID).getMemberById(l).getUser().openPrivateChannel().flatMap(pc -> pc.sendMessage("Dein Squad-Leader hat die Session beendet. Danke für deine Teilnahme!")).queue();
            }
            event.getHook().editOriginal("Du hast die Session `"+ activeSession.getSession_id() +"` beendet.").queue();
            logger.info("Successfully completed end-request for Session {}", activeSession.getSession_id());
        }else{
            for(Long l : activeSession.getParticipants()) {
                Main.bot.getGuildById(Main.GUILD_ID).getMemberById(l).getUser().openPrivateChannel().flatMap(pc -> pc.sendMessage("Dein Squad-Leader hat die Session beendet. Danke für deine Teilnahme!")).queue();
            }
            event.getHook().editOriginal("Oh nein! Es schein so als wäre etwas schief gelaufen. Irgendetwas hat bei der Beendung der Session nicht gestimmt.").queue();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("______Data Dump______");
            eb.addField("Session-ID", activeSession.getSession_id(), false);
            eb.addField("Session-Leader", String.valueOf(activeSession.getLeader_id()), false);
            eb.addField("Timestamp", String.valueOf(System.currentTimeMillis()), false);
            eb.addField("Object", activeSession.toString(), false);
            eb.addField("Total Rounds",  String.valueOf(activeSession.getTotal_rounds()), false);
            eb.setFooter("Auto Dump");
            event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
            logger.info("Unsuccessful end-request for Session {} at {}", activeSession.getSession_id(), System.currentTimeMillis());
        }
    }
}

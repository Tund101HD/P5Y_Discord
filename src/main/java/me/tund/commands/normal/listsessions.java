package me.tund.commands.normal;

import me.tund.database.Database;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.List;

public class listsessions extends ListenerAdapter {

    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-listsessions-Command");

    public listsessions(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(!event.getName().equals("listsessions")) return;
        if(event.getUser().isBot()) return;
        event.deferReply().setEphemeral(true).queue();
        List<Session> sessions = handler.getSessions();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Alle aktiven Sessions:");
        StringBuilder sb = new StringBuilder();

        for (Session session : sessions) {
            sb.append("```\n");
            if(session.isSqaudOne()){
                sb.append("Session 1 (ID: "+session.getSession_id()+")"); // FIXME Expects only two sessions -> UPDATE ON SESSION MAX INCREASE
            }else{
                sb.append("Session 2 (ID: "+session.getSession_id()+")");
            }
            sb.append("Rundenanzahl: "+session.getTotal_rounds()+"\n");
            sb.append("Win/Loss: "+session.getWins()/session.getTotal_rounds()+"\n");
            sb.append("Leader: "+event.getJDA().getUserById(session.getLeader_id()).getEffectiveName());
            sb.append("Aktive Teilnehmer: ");
            for(long l : session.getActive_participants()){
                sb.append(event.getJDA().getUserById(l).getEffectiveName()+"\n");
            }
            sb.append("```\n");
        }
        eb.setDescription(sb.toString());
        event.getHook().setEphemeral(true).editOriginal("").setEmbeds(eb.build()).queue();
    }


}

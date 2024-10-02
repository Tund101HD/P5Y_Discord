package me.tund.commands.leader;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class move extends ListenerAdapter {

    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("MoveSessionCommand");
    private final SessionHandler handler;
    public move(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if(!event.getName().equalsIgnoreCase("move")) return;
        event.deferReply().setEphemeral(true).queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        for(Session s : handler.getSessions()){
            if(s.getActive_participants().contains(event.getUser().getIdLong()) || s.getParticipants().contains(event.getUser().getIdLong()) || handler.waiting.contains(db.getSquadMemberById(event.getMember().getIdLong()))){
                event.getHook().editOriginal("Sorry, aber du bist bereits Teil einer Session. Bitte verlasse diese zuerst in dem du aus dem Sprachkanal gehst!").queue();
                return;
            }
        }


    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

    }
}

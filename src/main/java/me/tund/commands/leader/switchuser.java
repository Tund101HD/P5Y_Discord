package me.tund.commands.leader;

import me.tund.database.Database;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class switchuser extends ListenerAdapter {

    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-move-Command");
    private final SessionHandler handler;
    public switchuser(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

    }
}

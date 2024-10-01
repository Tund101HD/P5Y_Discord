package me.tund.commands.normal;

import me.tund.database.Database;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class joinsession extends ListenerAdapter {
    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("JoinSessionCommand");
    public joinsession() {

    }


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e){

    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

    }
}

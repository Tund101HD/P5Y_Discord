package me.tund.commands.normal;

import me.tund.Main;
import me.tund.database.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

public class register extends ListenerAdapter {

    private final Database db = new Database();
    private final Guild g = Main.bot.getGuildById(Main.GUILD_ID);
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("RegisterCommand");

    public register(){

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        if (event.getUser().isBot()){
            event.reply("Es tut mir leid, aber nur echte Nutzer können diesen Command anwenden.").setEphemeral(true).complete().deleteOriginal().queue();
            return;
        }
        if(db.isUserEntry(event.getUser().getId())){
            event.reply("Es tut mir leid, aber du bist bereits registriert").setEphemeral(true).complete().deleteOriginal().queue();
            return;
        }
        event.reply("Bitte sieh in deine DMs um mit der Registration fortzufahren.").setEphemeral(true).complete().deleteOriginal().queue();
    }

}

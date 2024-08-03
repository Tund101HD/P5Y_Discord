package me.tund.commands.normal.register;

import me.tund.Main;
import me.tund.database.Database;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import org.slf4j.LoggerFactory;

public class register extends ListenerAdapter {

    private final Database db = new Database();
    private final Guild g = Main.bot.getGuildById(Main.GUILD_ID);
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("RegisterCommand");

    public register(){

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        if (event.getUser().isBot()){
            event.getHook().editOriginal("Es tut mir leid, aber nur echte Nutzer können diesen Command anwenden.").queue();
            return;
        }
        if(db.isUserEntry(event.getUser().getId())){
            event.getHook().editOriginal("Es tut mir leid, aber du bist bereits registriert").queue();
            return;
        }
        event.getHook().editOriginal("Bitte sieh in deine DMs um mit der Registration fortzufahren.").queue();
        event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
            event.getJDA().addEventListener(new RegisterListener(privateChannel.getIdLong(), event.getMember().getIdLong()));
            return privateChannel.sendMessage("Bitte klicke auf 'Start' sobald du bereit bist mit der Registration fortzufahren." +
                    " Du wirst für die Registration Stats brauchen, also stelle sicher dass du dein Warthunder offen hast oder deine Stats" +
                    "anderst wo, z.B. auf https://thunderskill.com/en ausließt. \n Du kannst die Registration immer abbrechen, in dem du ``ABBRUCH`` schreibst.").addActionRow(
                    Button.success("button_ui_startregister", "Start"),
                    Button.danger("button_ui_stopregister", "Abbruch")
            );
        }).queue();
    }

}

package me.tund.utils.matchUtils;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class test extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("TestCommandClient");
    public test(){

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("test")){
            logger.info("test");
            event.deferReply().setEphemeral(true).queue();
            event.getGuild().getCategories().forEach(category -> {
                if(category.getName().equals("test")){
                    category.createTextChannel("testChannel").queue(textChannel -> {
                        textChannel.getJDA().addEventListener(new SessionChatListener(textChannel.getIdLong()));
                    });
                }
            });
            event.getHook().editOriginal("Added listener to channel").queue();
        }
    }

}

package me.tund.commands.normal.register;

import me.tund.Main;
import me.tund.database.SquadMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterListener extends ListenerAdapter {


    private static final Logger logger = LoggerFactory.getLogger("RegisterListenerClient");
    private long channel_id;
    private long user_id;
    private SquadMember member_object = new SquadMember();

    public RegisterListener(long channel_id, long user_id){
        this.channel_id = channel_id;
        this.user_id = user_id;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if (!(event.getAuthor().getIdLong() == user_id)) return;
        if(!(event.getChannel().getIdLong() == channel_id)) return;


        event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    List<Message> botMessages = messages.stream()
                            .filter(m -> m.getAuthor().equals(Main.bot.getSelfUser()))
                            .collect(Collectors.toList());
                    switch (botMessages.get(0).getEmbeds().get(0).getTitle().toLowerCase()) {
                        case "in-game name":
                            Message response = event.getMessage();
                            String response_content = response.getContentRaw().replace("`", "").strip();
                            EmbedBuilder builder = new EmbedBuilder();
                            builder.setTitle("In-Game Name");
                            builder.setDescription("Bitte bestätige, dass ``" + response_content + "`` dein In-Game Name ist.");
                            builder.setFooter("Du kannst diese Information auch im Nachhinein ändern.");
                            event.getChannel().sendMessage("").setEmbeds(builder.build())
                                    .addActionRow(Button.success("button_ui_confirmname", "Ja, stimmt so!"), Button.danger("button_ui_repeatname", "Nein, stimmt nicht!")).queue();
                            break;
                        default:
                            break;
                    }
                });

        //Ganz am Ende TODO YOU'RE A FUCKING RETARD
        // event.getJDA().removeEventListener(this);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getChannelIdLong() != channel_id) return;
        if(event.getChannel().asPrivateChannel().getUser().getIdLong() != user_id) return;

        switch (event.getComponentId().toLowerCase()) {
            case "button_ui_startregister":
                event.reply("Fangen wir an.").setEphemeral(true).queue();
                event.getMessage().delete().queue();
                member_object.setDiscord_id(event.getUser().getIdLong());

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("In-Game Name");
                builder.setDescription("Bitte schreibe deinen In-Game Namen in den Chat. Achte darauf, dass falls der Name Unterstriche," +
                        " Sternchen oder andere Sonderzeichen verwendet, diese möglicherweise von Discord als Markdown verwendet werden. Füge zur Sicherheit vor und " +
                        "nach dem Name zwei ` hinzu.");
                builder.setFooter("Frage 1/9");
                event.getHook().sendMessage("").setEmbeds(builder.build()).queue();
                long message_id = event.getChannel().getLatestMessageIdLong();
                logger.debug("Message ID is: {}", message_id);
                break;
            case "button_ui_stopregister":
                event.reply("Die Registration wurde auf deine Nachfrage geschlossen.").setEphemeral(true).queue();
                event.getMessage().delete().queue();
                break;
            case "button_ui_confirmname":
                event.deferReply().setEphemeral(true).queue();
                event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    List<Message> messages1 = messages.stream().filter(m -> m.getAuthor().equals(event.getUser())).collect(Collectors.toList());
                    member_object.setIn_game_name(messages1.get(0).getContentRaw().replace("`", "").strip());
                    event.getHook().sendMessage("Hallo, "+member_object.getIn_game_name()).setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                break;
            case "button_ui_repeatname":

                break;
            default:
                break;
        }
    }


}

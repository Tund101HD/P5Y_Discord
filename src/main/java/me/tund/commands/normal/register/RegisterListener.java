package me.tund.commands.normal.register;

import me.tund.Main;
import me.tund.database.SquadMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                        case "präferiertes br":
                            response = event.getMessage();
                            double preferred_br = 13.0d;
                            try {
                                preferred_br = Double.parseDouble(response.getContentStripped());
                                if(preferred_br >= 13.0) preferred_br = 13.0;
                                if(preferred_br <= 4.0) preferred_br = 4.0;
                                preferred_br = Math.ceil(preferred_br);
                                member_object.setPreferred_br(preferred_br);
                                builder = new EmbedBuilder();
                                builder.setTitle("Präferiertes BR");
                                builder.setDescription("Bitte bestätige, dass "+preferred_br+" dein präferiertes Battle-Rating ist.");
                                builder.setFooter("Du kannst diese Information auch im Nachhinein ändern.");
                                event.getChannel().sendMessage("").setEmbeds(builder.build())
                                        .addActionRow(Button.success("button_ui_confirmprefbr", "Ja, stimmt so!"), Button.danger("button_ui_repeatprefbr", "Nein, stimmt nicht!")).queue();
                            }catch (Exception e){
                                builder = new EmbedBuilder();
                                builder.setTitle("Präferiertes BR");
                                builder.setDescription("Oh nein! Es schein als konnte die Nachricht nicht verwertet werden. " +
                                        "Bitte gib erneut dein präferiertes Battle-Rating an und achte darauf, dass es eine Zahl ist!");
                                builder.setDescription("Frage 3/9");
                                event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                                event.getMessage().delete().queue();
                            }
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
                builder = new EmbedBuilder();
                builder.setTitle("Max BR");
                builder.setDescription("Bitte reagiere mit den passenden Emojis, auf welchen Battle-Ratings du mitspielen möchtest/mitspielen kannst. " +
                        "Bitte beachte, dass du nur für die Battle-Ratings gepingt oder berücksichtigt wirst, für die du hier abstimmst (BR x.3 wird aufgerundet)" +
                        ". Auch diese Information kannst du noch im nachhinein ändern. \n" +
                        "BR 13.0: :zero:  \n" +
                        "BR 12.0: :one:   \n" +
                        "BR 11.0: :two:   \n" +
                        "BR 10.0: :three:   \n" +
                        "BR 9.0: :four:   \n" +
                        "BR 8.0: :five:   \n" +
                        "BR 7.0: :six:   \n" +
                        "BR 6.0: :seven:   \n" +
                        "BR 5.0: :eight:   \n" +
                        "BR 4.0: :nine:   \n" +
                        "Wenn du fertig bist, drücke auf 'Ok'.");
                builder.setFooter("Frage 2/9");
                event.getChannel().sendMessage("").setEmbeds(builder.build())
                        .addActionRow(Button.success("button_ui_okaybr", "Ok")).queue(message -> {
                            message.addReaction(Emoji.fromFormatted(0+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(1+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(2+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(3+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(4+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(5+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(6+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(7+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(8+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(9+"\u20E3")).queue();
                        });
                break;
            case "button_ui_repeatname":
                event.reply("Bitte schreibe deinen Namen noch einmal in den Chat. Bitte achte erneut darauf, " +
                        "dass Discord Sonderzeichen wie Unterstriche oder Sternchen als Markdown verwendet. **Zur Sicherheit bietet es sich an, vor und nach dem Namen" +
                        " zwei ` zu setzen.**").setEphemeral(true).queue();
                break;
            case "button_ui_okaybr":
                event.deferReply().setEphemeral(true).queue();
                event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    List<Message> messages1 = messages.stream().filter(m -> m.getAuthor().equals(event.getJDA().getSelfUser())).collect(Collectors.toList());
                    boolean[] brs = {};
                    for(MessageReaction reaction : messages1.get(0).getReactions()) {
                        if(reaction.getCount() > 1){
                            String id = reaction.getEmoji().getName(); //FIXME idk this is weird #fuck_unicode
                            logger.info("ID is {}", id);
                           /* switch (id){
                                case 0+"\u20E3":
                                    brs[0] = true;
                                    break;
                                case 1+"\u20E3":
                                    brs[1] = true;
                                    break;
                                case 2+"\u20E3":
                                    brs[2] = true;
                                    break;
                                case 3+"\u20E3":
                                    brs[3] = true;
                                    break;
                                case 4+"\u20E3":
                                    brs[4] = true;
                                    break;
                                case 5+"\u20E3":
                                    brs[5] = true;
                                    break;
                                case 6+"\u20E3":
                                    brs[6] = true;
                                    break;
                                case 7+"\u20E3":
                                    brs[7] = true;
                                    break;
                                case 8+"\u20E3":
                                    brs[8] = true;
                                    break;
                                case 9+"\u20E3":
                                    brs[9] = true;
                                    break;
                                default:
                            }
                            */
                        }
                    }
                    member_object.setBrs(brs);
                    logger.info("BRs are: {}", brs);
                    event.getHook().sendMessage("Vielen Dank für diese Infos, so können wir leichter einen Einsatz für dich finden! :smile:").setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                builder = new EmbedBuilder();
                builder.setTitle("Präferiertes BR");
                builder.setDescription("Bitte schreibe das Battle-Rating in den Chat, auf dem du am liebsten Spielst. Es sind alle Battle-" +
                        "Ratings von 4.0 bis 13.0 enthalten. Bitte beachte, dass Battle-Ratings die keine ganzen Zahlen sind, aufgerundet werden." +
                        " Des weiteren ist zu beachten, dass wir das Battle-Rating für Realistische Bodenschlachten verwenden.");
                builder.setFooter("Frage 3/9");
                event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                break;
            case "button_ui_confirmprefbr":
                event.reply("Dein präferiertes BR ist nun "+member_object.getPreferred_br()+".").setEphemeral(true).queue();
                event.getMessage().delete().queue();
                builder = new EmbedBuilder();
                builder.setTitle("Präferierte Rolle");
                builder.setDescription("Bitte reagiere mit den passenden Emojis, welche Rolle du am liebsten Spielst. Bitte beachte dabei, dass Luftabwehr" +
                        " auch als 'Air' gewertet wird. Dies ist wichtig um dich später in die richtigen Channels einzuordnen. Drücke danach wieder auf 'Ok' um" +
                        " deine Eingabe zu bestätigen. \n" +
                        "Air: :zero:   \n" +
                        "Ground: :one:   \n"+
                        "Beides: :two:   \n");
                builder.setFooter("Frage 4/9");
                event.getChannel().sendMessage("").setEmbeds(builder.build())
                        .addActionRow(Button.success("button_ui_okayunit", "Ok")).queue(message -> {
                            message.addReaction(Emoji.fromFormatted(0+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(1+"\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(2+"\u20E3")).queue();
                        });
                break;
            case "button_ui_repeatprefbr":
                event.reply("Bitte schreibe nochmal in den Chat, was deine präferiertes Battle-Rating ist. Bitte achte darauf, dass du ein valides" +
                        " Battle-Rating reinschreibst.").setEphemeral(true).queue();
                break;
            default:
                break;
        }
    }


}

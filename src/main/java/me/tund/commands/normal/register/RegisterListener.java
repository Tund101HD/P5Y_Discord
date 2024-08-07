package me.tund.commands.normal.register;

import com.google.protobuf.Field;
import edu.cmu.sphinx.fst.utils.Utils;
import me.tund.Main;
import me.tund.database.SquadMember;
import me.tund.utils.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterListener extends ListenerAdapter {


    private static final Logger logger = LoggerFactory.getLogger("RegisterListenerClient");
    private long channel_id;
    private long user_id;
    private SquadMember member_object = new SquadMember();

    public RegisterListener(long channel_id, long user_id) {
        this.channel_id = channel_id;
        this.user_id = user_id;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!(event.getAuthor().getIdLong() == user_id)) return;
        if (!(event.getChannel().getIdLong() == channel_id)) return;

        if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
            event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
            event.getMessage().delete().queue();
            event.getJDA().removeEventListener(this);
            return;
        }
        event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
            List<Message> botMessages = messages.stream()
                    .filter(m -> m.getAuthor().equals(Main.bot.getSelfUser()))
                    .collect(Collectors.toList());
            switch (botMessages.get(0).getEmbeds().get(0).getTitle().toLowerCase().strip()) {
                case "in-game name":
                    Message response = event.getMessage();
                    String response_content = response.getContentRaw();
                    if(response_content.startsWith("``") && response_content.endsWith("``")) {
                        response_content = response_content.replaceAll("``", "");
                    }
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("In-Game Name");
                    builder.setDescription("Bitte bestätige, dass ``" + response_content + "`` dein In-Game Name ist.");
                    builder.setFooter("Du kannst diese Information auch im Nachhinein ändern.");
                    event.getChannel().sendMessage("").setEmbeds(builder.build())
                            .addActionRow(Button.success("button_ui_confirmname", "Ja, stimmt so!"), Button.danger(
                                    "button_ui_repeatname", "Nein, stimmt nicht!")).queue();
                    break;
                case "präferiertes br":
                    response = event.getMessage();
                    double preferred_br = 13.0d;
                    try {
                        preferred_br = Double.parseDouble(response.getContentStripped().replaceAll(",","."));
                        if (preferred_br >= 13.0) preferred_br = 13.0;
                        if (preferred_br <= 4.0) preferred_br = 4.0;
                        preferred_br = Math.ceil(preferred_br);
                        member_object.setPreferred_br(preferred_br);
                        builder = new EmbedBuilder();
                        builder.setTitle("Präferiertes BR");
                        builder.setDescription("Bitte bestätige, dass " + preferred_br + " dein präferiertes " +
                                "Battle-Rating ist.");
                        builder.setFooter("Du kannst diese Information auch im Nachhinein ändern.");
                        event.getChannel().sendMessage("").setEmbeds(builder.build())
                                .addActionRow(Button.success("button_ui_confirmprefbr", "Ja, stimmt so!"),
                                        Button.danger("button_ui_repeatprefbr", "Nein, stimmt nicht!")).queue();
                    } catch (Exception e) {
                        builder = new EmbedBuilder();
                        builder.setTitle("Präferiertes BR");
                        builder.setDescription("Oh nein! Es scheint, als konnte die Nachricht nicht verwertet werden." +
                                " " +
                                "Bitte gib erneut dein präferiertes Battle-Rating an und achte darauf, dass es eine " +
                                "Zahl ist!");
                        builder.setDescription("Frage 3/9");
                        event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                        event.getMessage().delete().queue();
                    }
                    break;
                case "kill-ratio":
                    logger.info("ARRIVED AT KILL RATIO");
                    response = event.getMessage();
                    logger.info("Response is: {}", response.getContentStripped());
                    float kd = 1.0f;
                    try {
                        kd = Float.parseFloat(response.getContentStripped().replaceAll(",","."));
                        if (kd < 6.0) {
                            builder = new EmbedBuilder();
                            builder.setTitle("Kill-Ratio");
                            builder.setDescription("Bitte bestätige, dass " + kd + " deine K/D ist.");
                            builder.setFooter("Es lohnt sich, diese Information oft aufzufrischen.");
                            event.getChannel().sendMessage("").setEmbeds(builder.build())
                                    .addActionRow(Button.success("button_ui_confirmkd", "Ja, stimmt so!"),
                                            Button.danger("button_ui_repeatkd", "Nein, stimmt nicht!")).queue();
                            member_object.setKd(kd);
                        } else {
                            builder = new EmbedBuilder();
                            builder.setTitle("Kill-Ratio");
                            builder.setDescription("Deine K/D von " + kd + " erschein mir unrealistisch. Bitte sei " +
                                    "ehrlich bei " +
                                    "deiner Eingabe oder überprüfe sie noch einmal. Solltest du doch so ein Brecher " +
                                    "sein, " +
                                    "gib nun '1' als deine K/D an und schick einen Screenshot an Kilian oder " +
                                    "Tund_101_HD.");
                            builder.setFooter("Ein Moderator prüft alle Werte kurz nach der Registration, sei also " +
                                    "ehrlich!");
                            event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                        }
                    } catch (Exception e) {
                        builder = new EmbedBuilder();
                        builder.setTitle("Kill-Ratio");
                        builder.setDescription("Oh nein! Es scheint, als konnte die Nachricht nicht verwertet werden." +
                                " " +
                                "Bitte gib erneut deine K/D ein und achte darauf, dass es eine Dezimalzahl ist!");
                        builder.setDescription("Frage 5/6");
                        event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                        event.getMessage().delete().queue();
                    }
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getChannelIdLong() != channel_id) return;
        if (event.getChannel().asPrivateChannel().getUser().getIdLong() != user_id) return;

        switch (event.getComponentId().toLowerCase()) {
            case "button_ui_startregister":
                event.reply("Fangen wir an.").setEphemeral(true).queue();
                event.getMessage().delete().queue();
                member_object.setDiscord_id(event.getUser().getIdLong());

                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("In-Game Name");
                builder.setDescription("Bitte schreibe deinen In-Game Namen in den Chat. Achte darauf, dass falls der" +
                        " Name Unterstriche," +
                        " Sternchen oder andere Sonderzeichen verwendet, diese möglicherweise von Discord als " +
                        "Markdown verwendet werden. Füge zur Sicherheit vor und " +
                        "nach dem Name zwei ` hinzu.");
                builder.setFooter("Frage 1/6");
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
                    List<Message> messages1 =
                            messages.stream().filter(m -> m.getAuthor().equals(event.getUser())).collect(Collectors.toList());
                    member_object.setIn_game_name(messages1.get(0).getContentRaw().replace("`", "").strip());
                    event.getHook().sendMessage("Hallo, " + member_object.getIn_game_name()).setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                builder = new EmbedBuilder();
                builder.setTitle("Max BR");
                builder.setDescription("Bitte reagiere mit den passenden Emojis, auf welchen Battle-Ratings du " +
                        "mitspielen möchtest/mitspielen kannst. " +
                        "Bitte beachte, dass du nur für die Battle-Ratings gepingt oder berücksichtigt wirst, für die" +
                        " du hier abstimmst (BR x.3 wird aufgerundet)" +
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
                builder.setFooter("Frage 2/6");
                event.getChannel().sendMessage("").setEmbeds(builder.build())
                        .addActionRow(Button.success("button_ui_okaybr", "Ok")).queue(message -> {
                            message.addReaction(Emoji.fromFormatted(0 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(1 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(2 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(3 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(4 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(5 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(6 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(7 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(8 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(9 + "\u20E3")).queue();
                        });
                break;
            case "button_ui_repeatname":
                event.reply("Bitte schreibe deinen Namen noch einmal in den Chat. Bitte achte erneut darauf, " +
                        "dass Discord Sonderzeichen wie Unterstriche oder Sternchen als Markdown verwendet. **Zur " +
                        "Sicherheit bietet es sich an, vor und nach dem Namen" +
                        " zwei ` zu setzen.**").setEphemeral(true).queue();
                break;
            case "button_ui_okaybr":
                event.deferReply().setEphemeral(true).queue();
                event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    List<Message> messages1 =
                            messages.stream().filter(m -> m.getAuthor().equals(event.getJDA().getSelfUser())).collect(Collectors.toList());
                    boolean[] brs = new boolean[10];
                    for (MessageReaction reaction : messages1.get(0).getReactions()) {
                        if (reaction.getCount() > 1) {
                            String id = reaction.getEmoji().getName(); //This returns a unicode
                            switch (id) {
                                case '\u0030' + "\u20E3": // 0
                                    brs[0] = true;
                                    break;
                                case '\u0031' + "\u20E3": // 1
                                    brs[1] = true;
                                    break;
                                case '\u0032' + "\u20E3": // 2
                                    brs[2] = true;
                                    break;
                                case '\u0033' + "\u20E3": // 3
                                    brs[3] = true;
                                    break;
                                case '\u0034' + "\u20E3": // 4
                                    brs[4] = true;
                                    break;
                                case '\u0035' + "\u20E3": // 5
                                    brs[5] = true;
                                    break;
                                case '\u0036' + "\u20E3": // 6
                                    brs[6] = true;
                                    break;
                                case '\u0037' + "\u20E3": // 7
                                    brs[7] = true;
                                    break;
                                case '\u0038' + "\u20E3": // 8
                                    brs[8] = true;
                                    break;
                                case '\u0039' + "\u20E3": // 9
                                    brs[9] = true;
                                    break;
                                default:
                            }
                        }
                    }
                    member_object.setBrs(brs);
                    event.getHook().sendMessage("Vielen Dank für diese Infos, so können wir leichter einen Einsatz " +
                            "für dich finden! :smile:").setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                builder = new EmbedBuilder();
                builder.setTitle("Präferiertes BR");
                builder.setDescription("Bitte schreibe das Battle-Rating in den Chat, auf dem du am liebsten Spielst." +
                        " Es sind alle Battle-" +
                        "Ratings von 4.0 bis 13.0 enthalten. Bitte beachte, dass Battle-Ratings die keine ganzen " +
                        "Zahlen sind, aufgerundet werden." +
                        " Des weiteren ist zu beachten, dass wir das Battle-Rating für Realistische Bodenschlachten " +
                        "verwenden.");
                builder.setFooter("Frage 3/6");
                event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                break;
            case "button_ui_confirmprefbr":
                event.reply("Dein präferiertes BR ist nun " + member_object.getPreferred_br() + ".").setEphemeral(true).queue();
                event.getMessage().delete().queue();
                builder = new EmbedBuilder();
                builder.setTitle("Präferierte Rolle");
                builder.setDescription("Bitte reagiere mit den passenden Emojis, welche Rolle du am liebsten Spielst." +
                        " Bitte beachte dabei, dass Luftabwehr" +
                        " auch als 'Air' gewertet wird. Dies ist wichtig um dich später in die richtigen Channels " +
                        "einzuordnen. Drücke danach wieder auf 'Ok' um" +
                        " deine Eingabe zu bestätigen. \n" +
                        "Air: :zero:   \n" +
                        "Ground: :one:   \n" +
                        "Beides: :two:   \n");
                builder.setFooter("Frage 4/6");
                event.getChannel().sendMessage("").setEmbeds(builder.build())
                        .addActionRow(Button.success("button_ui_okayunit", "Ok")).queue(message -> {
                            message.addReaction(Emoji.fromFormatted(0 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(1 + "\u20E3")).queue();
                            message.addReaction(Emoji.fromFormatted(2 + "\u20E3")).queue();
                        });
                break;
            case "button_ui_repeatprefbr":
                event.reply("Bitte schreibe nochmal in den Chat, was deine präferiertes Battle-Rating ist. Bitte " +
                        "achte darauf, dass du ein valides" +
                        " Battle-Rating reinschreibst.").setEphemeral(true).queue();
                break;
            case "button_ui_okayunit":
                event.deferReply().setEphemeral(true).queue();
                event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    List<Message> messages1 =
                            messages.stream().filter(m -> m.getAuthor().equals(event.getJDA().getSelfUser())).collect(Collectors.toList());
                    int count = 0;
                    String unit = "both";
                    for (MessageReaction reaction : messages1.get(0).getReactions()) {
                        if (reaction.getCount() > 1) {
                            count++;
                            String id = reaction.getEmoji().getName();
                            switch (id) {
                                case '\u0030' + "\u20E3": // 0
                                    unit = "air";
                                    break;
                                case '\u0031' + "\u20E3": // 1
                                    unit = "ground";
                                    break;
                                case '\u0032' + "\u20E3": // 2
                                    unit = "both";
                                    break;
                                default:
                            }
                        }
                    }
                    if (count > 1) {
                        unit = "both";
                    }
                    member_object.setPreferred_unit(unit);
                    logger.info("Preferred unit is: {}", unit);
                    event.getHook().sendMessage("Vielen Dank für diese Infos, so können wir leichter einen Einsatz " +
                            "für dich finden! :smile:").setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                builder = new EmbedBuilder();
                builder.setTitle("Kill-Ratio");
                builder.setDescription("Bitte gib deine jetzige K/D als Dezimalzahl an. Dabei geht es um deine K/D in" +
                        " realistischen Schlachten. Solltest du ThunderSkill als Quelle verwenden" +
                        ", ist der Prozess recht einfach, auf dem Warthunder Client musst du auf dein Profil gehen, " +
                        "realistische Schlachten auswählen " +
                        "und dann deine Luftkills + Bodenkills durch die Anzahl der gesamten Tode rechnen. Um die " +
                        "Anzahl der Tode zu sehen musst du über" +
                        " die Reihe mit den Kills hovern. ");
                builder.setFooter("Frage 5/6");
                try {
                    event.getChannel().sendMessage("").setEmbeds(builder.build()).
                            addFiles(FileUpload.fromData(new File("src/main/resources/images/HomeScreen.png")),
                                    FileUpload.fromData(new File("src/main/resources/images/Stats.png"))).queue();
                } catch (Exception e) {
                    logger.warn("Couldn't find picture-files to attach to the message.");
                    event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                }
                break;
            case "button_ui_confirmkd":
                event.deferReply().setEphemeral(true).queue();
                event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                    event.getHook().sendMessage("Danke für diese Information, nun kann ich deine Spielstärke besser " +
                            "einschätzen.").setEphemeral(true).queue();
                    event.getMessage().delete().queue();
                });
                builder = new EmbedBuilder();
                builder.setTitle("Replacement");
                builder.setDescription("Bitte gib nun an, ob du damit Einverstanden bist deine Position im Team auch " +
                        "aufzugeben, wenn einer" +
                        " der Hauptspieler/besseren Spieler einen Platz braucht. Diese Information ist nur wichtig " +
                        "wenn du aktiv an Sitzungen des 1. Teams Teilnehmen möchtest.");
                builder.setFooter("Frage 6/6");
                event.getChannel().sendMessage("").setEmbeds(builder.build())
                        .addActionRow(Button.success("button_ui_okayreplace", "Ja, das ist okay."),
                                Button.success("button_ui_noreplace", "Nein, das möchte ich nicht.")).queue();
                break;
            case "button_ui_repeatkd":
                event.deferReply().setEphemeral(true).queue();
                event.getHook().sendMessage("Bitte schreibe erneut deine K/D in den Chat. Achte darauf, dass du deine" +
                        " K/D als " +
                        "Dezimalzahl angibst -also z.B. 0.69- und nicht als Bruch, da sonst die nächst-beste Zahl " +
                        "genommen wird.").queue();
                break;
            case "button_ui_okayreplace":
                event.deferReply().setEphemeral(true).queue();
                event.getMessage().delete().queue();
                member_object.setReplace(true);
                event.getHook().sendMessage("Danke dass du dich bereit erklärst auswechselt zu werden. Ein ausgewogeneres " +
                            "Team hat auch Vorteile für dich und deinen Spielspaß.").setEphemeral(true).queue();
                float max_br = 13.0f;
                for(int i = 0; i<10; i++){
                    if(member_object.getBrs()[i]){
                        max_br = 13.0f-i;
                    }
                }
                String role = "Beides/Egal";

                switch (member_object.getPreferred_unit()){
                    case "both":
                        role = "Beides/Egal";
                        break;
                    case "ground":
                        role = "Boden";
                        break;
                    case "air":
                        role = "Luft";
                        break;
                }
                builder = new EmbedBuilder();
                builder.setTitle("Account: "+member_object.getIn_game_name());
                builder.addField("**IGN**", member_object.getIn_game_name(), false);
                builder.addField("**Rolle**", role, false);
                builder.addField("**Präf. BR**", String.valueOf(member_object.getPreferred_br()), false);
                builder.addField("**Max BR**", String.valueOf(max_br), false);
                builder.addField("**K/D**", String.valueOf(Utilities.round((float)member_object.getKd(), 2)), false);
                builder.addField("**Ersetzen**", (member_object.isReplace()?"Ja":"Nein"), false);
                builder.setDescription("Bitte überprüfe diese Werte noch einmal auf ihre Richtigkeit.");
                builder.appendDescription("```ansi\n" +
                        "- \u001B[2;31mMit dieser Registration erlaubst du es uns, Daten über deine Aktivität im CW-Channel, der CW-Voicechats und deiner In-Game-Performance zu sammeln und zu verwerten.\u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31mAlle angegebenen Daten werden von einem Moderator auf ihre Richtigkeit geprüft. Sollten Werte offensichtlich gefälscht sein, können Strafen verhängt werden. Um Werte zu bearbeiten kannst du nochmals \u001B[4;31m/register\u001B[0m\u001B[2;31m in einem Channel eingeben. \u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31mEs macht Sinn Werte wie deine Activity oder deine K/D häufig zu ändern. Du kannst nur diese Werte spezifisch ändern, indem du \u001B[4;31m/register\u001B[0m\u001B[2;31m\u001B[4;31m \u001B[0m\u001B[2;31m\u001B[4;31m<wert>\u001B[0m\u001B[2;31m eingibst.\n" +
                        "\u001B[0m\n" +
                        "- \u001B[2;31mDer Squad-Leader hat das aller letzte Wort. Der Bot dient zur Hilfe und garantiert keinen Platz/keine Spielzeit in einem Squad. Squad-Leader können jeder Zeit Mitspieler beliebig nach ihrem belangen austauschen.\u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31m\u001B[1;31m\u001B[1;36mUm Hilfe mit den Commands zu erhalten, kannst du im CW Channel /cwhelp eingeben oder dem Bot HILFE als DM-Nachricht schreiben. Es wird empfohlen mindestens einmal den Befehl ausgeführt zu haben.\u001B[0m\u001B[1;31m\u001B[0m\u001B[2;31m\u001B[0m\n" +
                        "```");
                builder.setFooter("UID: "+member_object.getDiscord_id());
                event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                event.getJDA().removeEventListener(this);
                break;
            case "button_ui_noreplace":
                event.deferReply().setEphemeral(true).queue();
                event.getMessage().delete().queue();
                member_object.setReplace(false);
                event.getHook().sendMessage("Squadleader werden darüber informiert, dass du nur ungern ausgewechselt wirst." +
                        " Bitte denk daran, dass ein ausgewogeneres Team auch *deinen* Spielspaß fördert.").setEphemeral(true).queue();
                max_br = 13.0f;
                for(int i = 0; i<10; i++){
                    if(member_object.getBrs()[i]){
                        max_br = 13.0f-i;
                    }
                }
                role = "Beides/Egal";

                switch (member_object.getPreferred_unit()){
                    case "both":
                        role = "Beides/Egal";
                        break;
                    case "ground":
                        role = "Boden";
                        break;
                    case "air":
                        role = "Luft";
                        break;
                }
                builder = new EmbedBuilder();
                builder.setTitle("Account: "+member_object.getIn_game_name());
                builder.addField("**IGN**", member_object.getIn_game_name(), false);
                builder.addField("**Rolle**", role, false);
                builder.addField("**Präf. BR**", String.valueOf(member_object.getPreferred_br()), false);
                builder.addField("**Max BR**", String.valueOf(max_br), false);
                builder.addField("**K/D**", String.valueOf(Utilities.round((float)member_object.getKd(), 2)), false);
                builder.addField("**Ersetzen**", (member_object.isReplace()?"Ja":"Nein"), false);
                builder.setDescription("Bitte überprüfe diese Werte noch einmal auf ihre Richtigkeit.");
                builder.appendDescription("```ansi\n" +
                        "- \u001B[2;31mMit dieser Registration erlaubst du es uns, Daten über deine Aktivität im CW-Channel, der CW-Voicechats und deiner In-Game-Performance zu sammeln und zu verwerten.\u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31mAlle angegebenen Daten werden von einem Moderator auf ihre Richtigkeit geprüft. Sollten Werte offensichtlich gefälscht sein, können Strafen verhängt werden. Um Werte zu bearbeiten kannst du nochmals \u001B[4;31m/register\u001B[0m\u001B[2;31m in einem Channel eingeben. \u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31mEs macht Sinn Werte wie deine Activity oder deine K/D häufig zu ändern. Du kannst nur diese Werte spezifisch ändern, indem du \u001B[4;31m/register\u001B[0m\u001B[2;31m\u001B[4;31m \u001B[0m\u001B[2;31m\u001B[4;31m<wert>\u001B[0m\u001B[2;31m eingibst.\n" +
                        "\u001B[0m\n" +
                        "- \u001B[2;31mDer Squad-Leader hat das aller letzte Wort. Der Bot dient zur Hilfe und garantiert keinen Platz/keine Spielzeit in einem Squad. Squad-Leader können jeder Zeit Mitspieler beliebig nach ihrem belangen austauschen.\u001B[0m\n" +
                        "\n" +
                        "- \u001B[2;31m\u001B[1;31m\u001B[1;36mUm Hilfe mit den Commands zu erhalten, kannst du im CW Channel /cwhelp eingeben oder dem Bot HILFE als DM-Nachricht schreiben. Es wird empfohlen mindestens einmal den Befehl ausgeführt zu haben.\u001B[0m\u001B[1;31m\u001B[0m\u001B[2;31m\u001B[0m\n" +
                        "```");
                builder.setFooter("UID: "+member_object.getDiscord_id());
                event.getChannel().sendMessage("").setEmbeds(builder.build()).queue();
                event.getJDA().removeEventListener(this);
                break;
            default:
                break;
        }
    }
}

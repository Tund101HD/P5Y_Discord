package me.tund.commands.normal.register;

import me.tund.Main;
import me.tund.database.Database;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class register extends ListenerAdapter {

    private final Database db = new Database();
    private final Guild g = Main.bot.getGuildById(Main.GUILD_ID);
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("RegisterCommand");

    public register() {

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(!event.getName().equals("register")) return;
        event.deferReply().setEphemeral(true).queue();
        if (event.getUser().isBot()) {
            event.getHook().editOriginal("Es tut mir leid, aber nur echte Nutzer können diesen Command anwenden.").queue();
            return;
        }
        if (event.getOption("stat") != null) {
            logger.info("Stat is: {}", event.getOption("stat").getAsString().toLowerCase());
            switch (event.getOption("stat").getAsString().toLowerCase()) {


                /**
                    Updates the IGN for the given user
                    Sends a DM to the user and uses the first input.
                 **/
                case "ign":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {
                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                String name = event.getMessage().getContentRaw();
                                if(db.changeUserName(event.getMember().getId(), name)){
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("In-Game Name");
                                    eb.setDescription("Du hast deinen In-Game Name erfolgreich zu ``"+name+"`` geändert.");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }else{
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("In-Game Name");
                                    eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                            "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    eb.appendDescription("```ansi\n" +
                                            "\u001B[2;31mError DATABASE_UPDATERECORD_NAME at" +
                                            "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                            "```");
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }
                            }
                        });
                        return privateChannel.sendMessage("Bitte schreibe jetzt deinen In-Game Namen in den Chat. Falls du " +
                                "die Änderung abbrechen willst, schreibe einfach ``ABBRUCH`` in den Chat.");
                    }).queue();

                    break;


                /**
                    Updates the Preferred-Unit-Type for the given user
                    Sends an Embed-Message to the user's DMs with three
                    reactions and a button for submission.
                 **/
                case "präf_rolle":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {
                            @Override
                            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                                event.deferReply().setEphemeral(true).queue();
                                if(event.getComponentId().equalsIgnoreCase("okay_unit")){
                                    event.getChannel().getIterableHistory().takeAsync(10).thenAccept(messages -> {
                                        List<Message> messages1 =
                                                messages.stream().filter(m -> m.getAuthor().equals(event.getJDA().getSelfUser())).collect(Collectors.toList());
                                        int count = 0;
                                        String unit = "both";
                                        String rolle = "Beides/Egal";
                                        for (MessageReaction reaction : messages1.get(0).getReactions()) {
                                            if (reaction.getCount() > 1) {
                                                count++;
                                                String id = reaction.getEmoji().getName();
                                                switch (id) {
                                                    case '\u0030' + "\u20E3": // 0
                                                        unit = "air";
                                                        rolle = "Luft";
                                                        break;
                                                    case '\u0031' + "\u20E3": // 1
                                                        unit = "ground";
                                                        rolle = "Boden";
                                                        break;
                                                    case '\u0032' + "\u20E3": // 2
                                                        unit = "both";
                                                        rolle = "Beides/Egal";
                                                        break;
                                                    default:
                                                }
                                            }
                                        }
                                        if (count > 1 || count < 1) {
                                            unit = "both";
                                            rolle = "Beides/Egal";
                                        }
                                        if(db.changePreferredRole(event.getMember().getId(), unit)){
                                            EmbedBuilder eb = new EmbedBuilder();
                                            eb.setTitle("In-Game Name");
                                            eb.setDescription("Du hast deine präferierte Rolle zu ``"+rolle+"`` geändert.");
                                            eb.setFooter("ID: "+event.getMember().getId());
                                            event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                        }else{
                                            EmbedBuilder eb = new EmbedBuilder();
                                            eb.setTitle("Präferierte Rolle");
                                            eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                                    "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                            eb.setFooter("ID: "+event.getMember().getId());
                                            eb.appendDescription("```ansi\n" +
                                                    "\u001B[2;31mError DATABASE_UPDATERECORD_ROLE at" +
                                                    "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                                    "```");
                                            event.getHook().sendMessage("").setEmbeds(eb.build()).queue();
                                        }
                                        event.getMessage().delete().queue();
                                    });
                                }
                            }

                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                EmbedBuilder builder;
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
                                        .addActionRow(Button.success("okay_unit", "Ok")).queue(message -> {
                                            message.addReaction(Emoji.fromFormatted(0 + "\u20E3")).queue();
                                            message.addReaction(Emoji.fromFormatted(1 + "\u20E3")).queue();
                                            message.addReaction(Emoji.fromFormatted(2 + "\u20E3")).queue();
                                        });
                            }
                        });
                        return privateChannel.sendMessage("Bitte schreibe jetzt deinen In-Game Namen in den Chat. Falls du " +
                                "die Änderung abbrechen willst, schreibe einfach ``ABBRUCH`` in den Chat.");
                    }).queue();

                    break;

                /**
                    Updates only the Preferred-BR stat for the given user.
                    Listens to a Number in the DMs after the message, will retry
                    infinite times until a number can be parsed.
                 **/
                case "präf_br":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {
                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                int br = 13;
                                try {
                                    br = Integer.parseInt(event.getMessage().getContentRaw().replaceAll(",", "."));
                                    if(br > 13) br = 13;
                                    if(br < 4) br = 4;

                                }catch (Exception e){
                                    event.getChannel().sendMessage("Oh nein, die Zahl konnte nicht ausgelesen werden. Bitte stelle sicher dass eine Zahl angibst!").queue();
                                    return;
                                }
                                if(db.changePreferredBR(event.getMember().getId(), br)){
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Präferiertes Battle-Rating");
                                    eb.setDescription("Du hast dein präferiertes Battle-Rating erfolgreich zu ``"+br+"`` geändert.");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }else{
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Präferiertes Battle-Rating");
                                    eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                            "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    eb.appendDescription("```ansi\n" +
                                            "\u001B[2;31mError DATABASE_UPDATERECORD_PREFBR at" +
                                            "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                            "```");
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }
                            }
                        });
                        return privateChannel.sendMessage("Bitte schreibe jetzt dein präferiertes BR in den Chat. Falls du " +
                                "die Änderung abbrechen willst, schreibe einfach ``ABBRUCH`` in den Chat.");
                    }).queue();

                    break;
                case "k/d":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {
                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                float kd = 1f;
                                try {
                                    kd = Float.parseFloat(event.getMessage().getContentRaw().replaceAll(",", "."));
                                    if(kd > 4 || kd < 0){
                                        event.getChannel().sendMessage("Deine K/D erscheint mir unrealistisch. Bitte sei ehrlich bei deinen Angaben.");
                                        return;
                                    }
                                }catch (Exception e){
                                    event.getChannel().sendMessage("Oh nein, die Zahl konnte nicht ausgelesen werden. Bitte stelle sicher dass eine Zahl angibst!").queue();
                                    return;
                                }
                                if(db.changeKillRation(event.getMember().getId(), kd)){
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Kill-Ratio");
                                    eb.setDescription("Du hast deine K/D erfolgreich zu ``"+kd+"`` geändert.");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }else{
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Kill-Ratio");
                                    eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                            "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    eb.appendDescription("```ansi\n" +
                                            "\u001B[2;31mError DATABASE_UPDATERECORD_KDR at" +
                                            "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                            "```");
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }
                            }
                        });
                        return privateChannel.sendMessage("Bitte schreibe jetzt dein Kill-Death-Ratio in den Chat. Falls du " +
                                "die Änderung abbrechen willst, schreibe einfach ``ABBRUCH`` in den Chat.");
                    }).queue();
                    break;
                case "activity":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {
                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                int activity = 0;
                                try {
                                    activity = Integer.parseInt(event.getMessage().getContentRaw().replaceAll(",", "."));
                                    if(activity > 1800 || activity < 0){
                                        event.getChannel().sendMessage("Deine Activity erscheint mir unrealistisch. Bitte sei ehrlich bei deinen Angaben.");
                                        return;
                                    }
                                }catch (Exception e){
                                    event.getChannel().sendMessage("Oh nein, die Zahl konnte nicht ausgelesen werden. Bitte stelle sicher dass eine Zahl angibst!").queue();
                                    return;
                                }
                                if(db.changeActivity(event.getMember().getId(), activity)){
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Activity");
                                    eb.setDescription("Du hast deine Activity erfolgreich zu ``"+activity+"`` geändert.");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }else{
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Activity");
                                    eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                            "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    eb.appendDescription("```ansi\n" +
                                            "\u001B[2;31mError DATABASE_UPDATERECORD_ACTVTY at" +
                                            "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                            "```");
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }
                            }
                        });
                        return privateChannel.sendMessage("Bitte schreibe jetzt deine Activity in den Chat. Falls du " +
                                "die Änderung abbrechen willst, schreibe einfach ``ABBRUCH`` in den Chat.");
                    }).queue();
                    break;
                case "replace":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {

                            @Override
                            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                                if (event.getUser().isBot()){
                                    event.reply("Sorry, only humans allowed :) ").queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                event.deferReply().setEphemeral(true).queue();
                                if(event.getComponentId().equalsIgnoreCase("okay")){
                                    db.changeReplace(event.getUser().getId(), true);
                                    event.getHook().sendMessage("Du hast zugestimmt, bei bedarf ersetzt zu werden.").setEphemeral(true).queue();
                                }else{
                                    db.changeReplace(event.getUser().getId(), false);
                                    event.getHook().sendMessage("Du hast abgestimmt, dass du bei bedarf ungern ersetzt werden möchtest. Bitte " +
                                            "beachte, dass der Squad-Leader immer noch das letzte Wort hat.").setEphemeral(true).queue();
                                }
                            }

                            @Override
                            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                if (event.getAuthor().isBot()){
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                if(event.getMessage().getContentRaw().equalsIgnoreCase("ABBRUCH")) {
                                    event.getChannel().sendMessage("Die Registrierung wurde auf deinen Wunsch hin abgebrochen.").queue();
                                    event.getMessage().delete().queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                int activity = 0;
                                try {
                                    activity = Integer.parseInt(event.getMessage().getContentRaw().replaceAll(",", "."));
                                    if(activity > 1800 || activity < 0){
                                        event.getChannel().sendMessage("Deine Activity erscheint mir unrealistisch. Bitte sei ehrlich bei deinen Angaben.");
                                        return;
                                    }
                                }catch (Exception e){
                                    event.getChannel().sendMessage("Oh nein, die Zahl konnte nicht ausgelesen werden. Bitte stelle sicher dass eine Zahl angibst!").queue();
                                    return;
                                }
                                if(db.changeActivity(event.getMember().getId(), activity)){
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Activity");
                                    eb.setDescription("Du hast deine Activity erfolgreich zu ``"+activity+"`` geändert.");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }else{
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle("Activity");
                                    eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                            "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                    eb.setFooter("ID: "+event.getMember().getId());
                                    eb.appendDescription("```ansi\n" +
                                            "\u001B[2;31mError DATABASE_UPDATERECORD_ACTVTY at" +
                                            "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                            "```");
                                    event.getChannel().sendMessage("").setEmbeds(eb.build()).queue();
                                }
                            }
                        });
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Ersetzen");
                        builder.setDescription("Hättest du ein Problem damit, wenn du ggf. durch einen besser Spieler in der Queue ersetzt wirst? Dies ist " +
                                "hauptsächlich für Squad-1 relevant.");
                        builder.setFooter("ID: "+event.getMember().getId());
                        return privateChannel.sendMessage("").setEmbeds(builder.build()).addActionRow(
                                Button.success("okay", "Ja, das ist okay."),
                                Button.danger("no", "Nein, das möchte ich nicht.")
                        );
                    }).queue();
                    break;
                case "brs":
                    if(!db.isUserEntry(event.getMember().getId())){
                        event.getHook().editOriginal("Du musst dich zu erst registrieren bevor du diese Option anwenden kannst!");
                        return;
                    }
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new ListenerAdapter() {

                            @Override
                            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                                if (event.getUser().isBot()){
                                    event.reply("Sorry, only humans allowed :) ").queue();
                                    event.getJDA().removeEventListener(this);
                                    return;
                                }
                                event.deferReply().setEphemeral(true).queue();
                                if(event.getComponentId().equalsIgnoreCase("okay")){
                                    boolean[] brs = new boolean[10];
                                    for (MessageReaction reaction : event.getMessage().getReactions()) {
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
                                    if(db.changeBrs(event.getUser().getId(), brs)){
                                        event.getHook().sendMessage("Du hast die Liste deiner Battle-Ratings geändert. " +
                                                        "Du kannst alle deine Daten jeder Zeit mit /whoisme anzeigen lassen.")
                                                .setEphemeral(true).queue();
                                        event.getMessage().delete().queue();
                                    }else{
                                        EmbedBuilder eb = new EmbedBuilder();
                                        eb.setTitle("Activity");
                                        eb.setDescription("Oh nein! Es scheint so als wäre ein Fehler aufgetreten. Sollte der Fehler bestehen " +
                                                "bleiben, melde dich bitte mit diesen Details an Tund_101_HD: ");
                                        eb.setFooter("ID: "+event.getMember().getId());
                                        eb.appendDescription("```ansi\n" +
                                                "\u001B[2;31mError DATABASE_UPDATERECORD_MAXBR at" +
                                                "ID: "+event.getMember().getId()+ " Timestamp: "+ System.currentTimeMillis()+
                                                "```");
                                        event.getHook().sendMessage("").setEmbeds(eb.build()).queue();
                                    }

                                }

                            }
                        });
                        EmbedBuilder builder = new EmbedBuilder();
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
                        return privateChannel.sendMessage("").setEmbeds(builder.build()).addActionRow(
                                Button.success("okay", "Okay")
                        );
                    }).queue(message -> {
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
                default:
                    event.deferReply().setEphemeral(true).queue();
                    event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                        event.getJDA().addEventListener(new RegisterListener(privateChannel.getIdLong(),
                                event.getMember().getIdLong()));
                        return privateChannel.sendMessage("Bitte klicke auf 'Start' sobald du bereit bist mit der " +
                                "Registration fortzufahren." +
                                " Du wirst für die Registration Stats brauchen, also stelle sicher dass du dein " +
                                "Warthunder offen hast oder deine Stats" +
                                "anderst wo, z.B. auf https://thunderskill.com/en ausließt. \n Du kannst die " +
                                "Registration immer abbrechen, in dem du ``ABBRUCH`` schreibst.").addActionRow(
                                Button.success("button_ui_startregister", "Start"),
                                Button.danger("button_ui_stopregister", "Abbruch")
                        );
                    }).queue();
            }
            event.getHook().editOriginal("Bitte sieh in deine DMs um mit der Registration fortzufahren.").queue();
        } else {
            event.getHook().sendMessage("Bitte sieh in deine DMs").queue();
            logger.info("Register Command didn't contain any stat to change specifically.");
            event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> {
                event.getJDA().addEventListener(new RegisterListener(privateChannel.getIdLong(),
                        event.getMember().getIdLong()));
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Registration Start");
                builder.setDescription("Bitte klicke auf 'Start' sobald du bereit bist mit der " +
                        "Registration fortzufahren." +
                        " Du wirst für die Registration Stats brauchen, also stelle sicher dass du dein Warthunder " +
                        "offen hast oder deine Stats" +
                        "anderst wo, z.B. auf https://thunderskill.com/en ausließt. \n Du kannst die Registration " +
                        "immer abbrechen, in dem du ``ABBRUCH`` schreibst. \n" );
                builder.appendDescription("```ansi\n" +
                        "\u001B[2;31m\u001B[1;31mMit dieser Registration erlaubst du es uns, Daten über deine " +
                        "Aktivität im CW-Channel, der CW-Voicechats und deiner In-Game-Performance zu sammeln und zu " +
                        "verwerten.\u001B[0m\u001B[2;31m\u001B[0m\n" +
                        "```");
                builder.setFooter("Starte deine Registration");
                return privateChannel.sendMessage("").setEmbeds(builder.build()).addActionRow(
                        Button.success("button_ui_startregister", "Start"),
                        Button.danger("button_ui_stopregister", "Abbruch")
                );
            }).queue();
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("register")) {
            switch (event.getFocusedOption().getName()) {
                case "stat":
                    String[] words = new String[]{"ign", "rolle", "präf_rolle", "präf_br", "k/d", "activity", "replace", "brs"};
                    List<Command.Choice> options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                            .map(word -> new Command.Choice(word, word)) // map the words to choices
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
            }


        }
    }

}

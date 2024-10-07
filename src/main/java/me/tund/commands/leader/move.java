package me.tund.commands.leader;

import edu.stanford.nlp.util.StringUtils;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.Utilities;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class move extends ListenerAdapter {

    private Database db = new Database();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("MoveSessionCommand");
    private final SessionHandler handler;
    public move(SessionHandler handler) {
        this.handler = handler;
    }

    //TODO Rework for dynamic movement into own squad from no squad, movement into different squad, movement into own squad from other squad -- Verification in DMS?
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        if(!event.getName().equalsIgnoreCase("move")) return;
        event.deferReply().setEphemeral(true).queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }

        long session = (!event.getOptions().contains("session"))
                ? 1L : event.getOption("session").getAsLong(); // 1L for invalid session, 0L would be moving the user out of the session
        if(session == 1L){
            event.getHook().editOriginal("Bitte gib eine Session ein").queue();
            return;
        }

        String name = (!event.getOptions().contains("user"))
                ? "" : event.getOption("user").getAsString();
        if(name.isEmpty() || name == null){
            event.getHook().editOriginal("Sorry, aber das ist kein valider Nutzer.").queue();
            return;
        }
        long id = 0;
        if(StringUtils.isNumeric(name)){
            try {
                id = Long.parseLong(name);
            }catch (Exception e){
                event.getHook().editOriginal("Sorry, aber etwas ist schief gelaufen. Bitte versuche es erneut.").queue();
                return;
            }

        }else{
            id = event.getGuild().getMembersByEffectiveName(name, false).size() > 0?event.getGuild().getMembersByEffectiveName(name, false).get(0).getIdLong() : 0L;
        }

        if(id == 0){
            event.getHook().editOriginal("Sorry, aber das ist kein valider Nutzer.").queue();
            return;
        }

        if(handler.getSessionById(String.valueOf(session)) != null){
            Session inputSession = handler.getSessionById(String.valueOf(session));
            Session current = handler.getSessionByLeader(event.getMember().getIdLong());
            if(current.getSession_id().equals(inputSession.getSession_id())){
                if(current.getActive_participants().contains(id)){
                    event.getHook().editOriginal("Sorry, aber dieser Spieler ist bereits Teil deines Squads!").queue();
                    return;
                }
                if(handler.getSessionByUser(id) != null){
                    Session userSession = handler.getSessionByUser(id);
                    if(userSession.getLeader_id() == id){
                        event.getHook().editOriginal("Sorry, aber du kannst nicht den Leader eines anderen Squads klauen ;) ").queue();
                        return;
                    }
                    event.getHook().editOriginal("Der Nutzer ist Teil eines anderen Squads. Bitte warte auf die Bestätigung des anderen Leaders.").queue();
                    long finalId1 = id;
                    Main.bot.getUserById(userSession.getLeader_id()).openPrivateChannel().flatMap((channel) -> {
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
                                    MessageEmbed e = event.getMessage().getEmbeds().get(0);
                                    Session moveTo;
                                    for(MessageEmbed.Field f : e.getFields()){
                                        if(f.getName().equals("SessionId")){
                                            moveTo = handler.getSessionById(f.getValue());
                                            moveTo.setLocked(true);
                                            handler.updateSession(moveTo);

                                            if(moveTo.getActive_participants().contains(finalId1)){ // This shouldn't happen, can't trust shit today though.
                                                logger.info("User is already part of this session! Will not move. ({})", event.getMember().getEffectiveName());
                                                return;
                                            }
                                            if(moveTo.getActive_participants().size() > 7){
                                                logger.info("This squad is full! Won't move user. ({})", event.getMember().getEffectiveName());
                                                moveTo.addParticipant(finalId1);
                                                if(event.getGuild().getMemberById(finalId1).getVoiceState().inAudioChannel()){
                                                    event.getGuild().moveVoiceMember(event.getGuild().getMemberById(finalId1), event.getGuild().getVoiceChannelById(Main.WARTERAUM_ID)).queue();
                                                    event.getGuild().getMemberById(finalId1).getUser().openPrivateChannel().flatMap(pc ->pc.sendMessage("Du wurdest in die Warteschlange versetzt, da dein Leader dich in einen vollen Squad moven wollte.")).queue();
                                                    event.getGuild().getMemberById(userSession.getLeader_id()).getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Der Nutzer "+event.getGuild().getMemberById(finalId1).getEffectiveName()+" wurde in die Warteschlange versetzt, da der angegebene Squad voll war.")).queue();
                                                }
                                                moveTo.addActive_participant(finalId1);
                                                if (Utilities.isSquadOne(inputSession.getLeader_id())) { //TODO Maybe add preferred unit move because otherwise the function is useless you fucking retard :)))
                                                    Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(finalId1),
                                                            Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                                                } else {
                                                    Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(finalId1),
                                                            Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                                                }
                                                userSession.removeActive_participant(finalId1);
                                                moveTo.setLocked(false);
                                                handler.updateSession(moveTo);
                                                handler.updateSession(userSession);
                                            }
                                        }
                                    }
                                }else{
                                    event.getHook().deleteOriginal().queue();
                                    event.getChannel().sendMessage("Du hast den Wechsel abgelehnt.").queue();
                                    event.getGuild().getMemberById(current.getLeader_id()).getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Dem Wechsel des Nutzers "+ event.getGuild().getMemberById(finalId1).getEffectiveName()+" wurde nicht zugestimmt.")).queue();

                                    return;
                                }

                            }
                        });
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Wechsel Anfrage für den Nutzer "+ event.getGuild().getMemberById(finalId1).getEffectiveName());
                        builder.setDescription("Es gibt eine Anfrage, einen Nutzer aus deiner Session zu entfernen und zu einer anderen Session hinzuzufügen.");
                        builder.addField("Von: ", event.getGuild().getMemberById(current.getLeader_id()).getEffectiveName(), false);
                        builder.addField("SessionId: ", current.getSession_id(), false);
                        builder.setFooter("Deine Session: "+ userSession.getSession_id());
                        return channel.sendMessage("").setEmbeds(builder.build()).addActionRow(
                                Button.success("okay", "Schieb ihn rüber!"),
                                Button.danger("no", "Leck meine Eier") //TODO maybe change this
                        );
                    }).queue();
                }
                current.getParticipants().add(id);
                handler.updateSession(current);
                if(current.getActive_participants().size() > 7){
                    event.getHook().editOriginal("Sorry, aber dein Squad ist voll. Bitte mach Platz für den Nutzer!").queue();
                    return;
                }
                if(!event.getGuild().getMemberById(id).getVoiceState().inAudioChannel()){
                    event.getHook().editOriginal("Sorry, aber dieser Nutzer ist nicht in einem Sprachkanal!").queue();
                    return;
                }
                if (Utilities.isSquadOne(event.getMember().getIdLong())) {
                    Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(id),
                            Main.bot.getVoiceChannelById(Main.SQUAD1_GROUND)).queue();
                } else {
                    Main.bot.getGuildById(Main.GUILD_ID).moveVoiceMember(Main.bot.getGuildById(Main.GUILD_ID).getMemberById(id),
                            Main.bot.getVoiceChannelById(Main.SQUAD2_GROUND)).queue();
                }
                current.getActive_participants().add(id);
                handler.updateSession(current);
            }else{
                if(inputSession.getActive_participants().contains(id)){
                    event.getHook().editOriginal("Sorry, aber dieser Spieler ist bereits Teil dieses Squads!").queue();
                    return;
                }
                if(inputSession.getActive_participants().size() > 7){
                    event.getHook().editOriginal("Sorry, aber dieser Squad ist bereits voll!").queue();
                    return;
                }
                if(handler.getSessionById(String.valueOf(session)) == null){
                    event.getHook().editOriginal("Sorry, aber es scheint so als wäre diese Session abgelaufen!").queue();
                    return;
                }
                event.getHook().editOriginal("Der Nutzer ist Teil eines anderen Squads. Bitte warte auf die Bestätigung des anderen Leaders.").queue();
                long finalId1 = id;
                Main.bot.getUserById(inputSession.getLeader_id()).openPrivateChannel().flatMap((channel) -> {
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
                                MessageEmbed e = event.getMessage().getEmbeds().get(0);
                                Session moveTo;
                                for(MessageEmbed.Field f : e.getFields()){
                                    if(f.getName().equals("SessionId")){
                                        moveTo = handler.getSessionById(f.getValue());
                                    }
                                }
                            }else{

                            }

                        }
                    });
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Wechsel Anfrage für den Nutzer "+ event.getGuild().getMemberById(finalId1).getEffectiveName());
                    builder.setDescription("Es gibt eine Anfrage, einen Nutzer deiner Session hinzuzufügen.");
                    builder.addField("Von: ", event.getGuild().getMemberById(current.getLeader_id()).getEffectiveName(), false);
                    builder.addField("SessionId: ", inputSession.getSession_id(), false);
                    builder.setFooter("Deine Session: "+ inputSession.getSession_id());
                    return channel.sendMessage("").setEmbeds(builder.build()).addActionRow(
                            Button.success("okay", "Schieb ihn rüber!"),
                            Button.danger("no", "Leck meine Eier") //TODO maybe change this
                    );
                }).queue();

            }
        }else if(session == 0L){
            Session inputSession = handler.getSessionById(String.valueOf(session));
            Session current = handler.getSessionByLeader(event.getMember().getIdLong());

        }else{
            event.getHook().editOriginal("Bitte gib eine valide Session ein").queue();
            return;
        }

        if(session != 0L){
            if(handler.getSessionById(String.valueOf(session)) == null){

            }
        }

    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if(!event.getName().equalsIgnoreCase("move")) return;
        switch (event.getFocusedOption().getName()) {
            case "user":
                Session s = null;
                for(Session ss : handler.getSessions()){
                    if(ss.getLeader_id() == event.getMember().getIdLong()) s = ss;
                }
                if(s==null) return;
                String[] words = {};
                int i = 0;
                List<Long> users = s.getActive_participants();
                for(Long u : users){
                    words[i] = event.getGuild().getMemberById(u).getEffectiveName();
                    i++;
                }
                for(Member m : Main.bot.getGuildById(Main.GUILD_ID).getVoiceChannelById(Main.WARTERAUM_ID).getMembers()){
                    words[i] = m.getEffectiveName();
                    i++;
                }
                i=0;
                List<Command.Choice> options = Stream.of(words)
                        .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                        .map(word -> new Command.Choice(word, word)) // map the words to choices
                        .collect(Collectors.toList());
                event.replyChoices(options).queue();
                break;
            case "session":
                String[] words1 = {};
                i = 0;
                for(Session s1 : handler.getSessions()){
                    words1[i] = s1.getSession_id();
                }
                List<Command.Choice> options1 = Stream.of(words1)
                        .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                        .map(word -> new Command.Choice(word, word)) // map the words to choices
                        .collect(Collectors.toList());
                event.replyChoices(options1).queue();
                options1 = Stream.of(words1)
                        .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                        .map(word -> new Command.Choice(word, word))
                        .collect(Collectors.toList());
                event.replyChoices(options1).queue();
                break;
        }
    }
}

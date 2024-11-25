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
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-move-Command");
    private final SessionHandler handler;
    public move(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getUser().isBot()) return;
        if(!event.getName().equalsIgnoreCase("move")) return;
        event.deferReply().setEphemeral(true).queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        if(handler.getSessionByLeader(event.getMember().getIdLong()) == null){
            event.getHook().editOriginal("Sorry, aber du hast keinen Squad.").queue();
            return;
        }
        long session = (!event.getOptions().contains("session"))
                ? 0L : event.getOption("session").getAsLong();
        if(session == 0L){
            event.getHook().editOriginal("Bitte gib eine Session ein, in die der Nutzer gemoved werden soll").queue();
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

        if(handler.getSessionByLeader(event.getMember().getIdLong()).getSession_id().equals(session)){
            event.getHook().editOriginal("Sorry, aber das ist dein Squad. Bitte wähle einen anderen Squad aus.").queue();
            return;
        }
        Session inputSession = handler.getSessionById(String.valueOf(session));
        Session currentSession = handler.getSessionByLeader(event.getMember().getIdLong());

        if(inputSession == null || currentSession == null){
            event.getHook().editOriginal("Sorry, aber einer der Sessions ist nicht mehr valide.").queue();
            return;
        }

        if(inputSession.getActive_participants().size() > 7){
            event.getHook().editOriginal("Diese Session ist bereits voll, bist du sicher dass du den Nutzer verschieben möchtest? Der Nutzer wird auf die Warteliste gesetzt.").queue();
            //TODO Add to waiting list
            return;
        }
        SquadMember m = db.getSquadMemberById(id);
        if(m == null){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer ist kein CW-Teilnehmer.").queue();
            return;
        }
        if(m.getPriority() < inputSession.getMin_priority() || m.getActivity() < inputSession.getMin_acitivty() ||inputSession.getExclude_ids().contains(id)){
            event.getHook().editOriginal("Dieser Nutzer erreicht nicht die Anforderungen für den Squad. Warte auf Bestätigung des Squad-Leaders").queue();
            //TODO Add join request
            return;
        }
        if(inputSession.getActive_participants().size() < 8){
            currentSession.removeActive_participant(id);
            inputSession.addActive_participant(id);
            event.getGuild().getMemberById(id).getUser().openPrivateChannel().flatMap(privateChannel -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Verschiebung in Squad "+ inputSession.getSession_id());
                eb.setDescription("Du wurdest von deinem Squad-Leader in einen anderen Squad verschoben. Bitte verständige dich mit `"+event.getGuild().getMemberById(inputSession.getLeader_id()).getEffectiveName()+"` was du spiele sollst.");
                eb.setFooter("move_squad");
                return privateChannel.sendMessage("").addEmbeds(eb.build());
            }).queue();
            long finalId = id;
            event.getGuild().getMemberById(inputSession.getLeader_id()).getUser().openPrivateChannel().flatMap(privateChannel -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Verschiebung in Squad "+ inputSession.getSession_id());
                eb.setDescription("Der Spieler `"+event.getGuild().getMemberById(finalId).getEffectiveName()+"` wurde in deinen Squad geschoben und erfüllt die Anforderungen. Bitte weise den Spieler in seine neue Rolle ein.");
                eb.setFooter("move_squad");
                return privateChannel.sendMessage("").addEmbeds(eb.build());
            }).queue();
            event.getGuild().getMemberById(currentSession.getLeader_id()).getUser().openPrivateChannel().flatMap(privateChannel -> {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Verschiebung in Squad "+ inputSession.getSession_id());
                eb.setDescription("Der Spieler `"+event.getGuild().getMemberById(finalId).getEffectiveName()+"` wurde erfolgreich in den Squad `"+inputSession.getSession_id()+"` verschoben. Dein Squad ist nun für 1 Minute gesperrt. Entsperre ihn sofort wieder mit `/unlock`.");
                eb.setFooter("move_squad");
                return privateChannel.sendMessage("").addEmbeds(eb.build());
            }).queue();
            //TODO LOCK SESSION
        }else{
            event.getHook().editOriginal("Sorry, aber diese Session ist voll.").queue();
            logger.info("Session has filled up during move process! Will not proceed.");
            return;
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

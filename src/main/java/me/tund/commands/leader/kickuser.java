package me.tund.commands.leader;

import edu.stanford.nlp.util.StringUtils;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class kickuser extends ListenerAdapter {
    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-startsession-Command");

    public kickuser(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getUser().isBot()) return;
        if(!event.getName().equalsIgnoreCase("kickuser")) return;
        event.deferReply().queue();
        if (!event.getMember().getRoles().contains(Main.bot.getRoleById(Main.SL_ROLE))) {
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        Session s = handler.getSessionByLeader(event.getMember().getIdLong());
        if(s == null){
            event.getHook().editOriginal("Sorry, aber du bist kein Squad-Leader.").queue();
            return;
        }
        long user = (!event.getOptions().contains("user")) ? 0L : event.getOption("user").getAsLong();
        String reason = (!event.getOptions().contains("reason")) ? "OTHER" : event.getOption("reason").getAsString();
        boolean autofill = (!event.getOptions().contains("auto_fill")) ? true : event.getOption("auto_fill").getAsBoolean();
        boolean ban = (!event.getOptions().contains("ban_user")) ? false : event.getOption("ban_user").getAsBoolean();

        Member m;
        if(StringUtils.isNumeric(String.valueOf(user))){
            m =  event.getGuild().getMemberById(user);
        }else{
            m = event.getGuild().getMembersByEffectiveName(String.valueOf(user), true).get(0);
        }
        if( m == null){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer existiert nicht.").queue();
            return;
        }
        if(!s.getParticipants().contains(user)){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer ist nicht Teil deiner Session.").queue();
            return;
        }
        switch (reason){
            case "AFK":
                reason="Du wurdest aus deiner Session gekickt, da du zu lange AFK warst. Sollte die Session noch frei sein, kannst du wieder beitreten.";
                break;
            case "PERFORMANCE":
                reason="Du wurdest aus deiner Session gekickt, weil dein Leader nicht zufrieden mit deiner Performance war.";
                break;
            case "OTHER":
                reason="Du wurdest aus deiner Session gekickt. Sollte die Session noch frei sein, kannst du versuchen erneut beizutreten.";
                break;
        }
        s.removeActive_participant(m.getIdLong());
        s.removeParticipant(m.getIdLong());
        String finalReason = reason;
        m.getUser().openPrivateChannel().flatMap(privateChannel -> {
            EmbedBuilder b = new EmbedBuilder();
            b.setTitle("Session "+s.getSession_id()+ " Kick");
            b.setDescription(finalReason);
            b.setFooter("Zeitstempel: "+System.currentTimeMillis());
            return privateChannel.sendMessage("").setEmbeds(b.build());
        }).queue();
        s.setLocked(!autofill);
        if(ban){
            List<Long> l =  s.getExclude_ids();
            l.add(m.getIdLong());
            s.setExclude_ids(l);
        }
        logger.info("User {}({}) has been kicked from Session {} with reason: {}", m.getEffectiveName(), m.getId(), s.getSession_id(), reason);
        if(ban) logger.info("User {}({}) has been banned from Session {}", m.getEffectiveName(), m.getId(), s.getSession_id());
        if(autofill) logger.info("Autofilling Session {}",s.getSession_id());
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(event.getName().equalsIgnoreCase("kickuser")) {
            Session s = handler.getSessionByLeader(event.getMember().getIdLong());
            List<String> participants = new ArrayList<>();
            if(s != null){
                participants= s.getActive_participants().stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }
            switch (event.getFocusedOption().getName()) {
                case "user":
                    String[] words = participants.toArray(new String[participants.size()]);
                    List<Command.Choice> options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "reason":
                    words = new String[]{"AFK", "PERFORMANCE", "OTHER"}; //Automated Messages
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "auto_fill":
                    words = new String[]{"true", "false"};
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "ban_user":
                    words = new String[]{"true", "false"}; //Imagine
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
            }
        }
    }
}

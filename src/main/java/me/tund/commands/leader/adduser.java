package me.tund.commands.leader;

import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.sessions.Session;
import me.tund.utils.sessions.SessionHandler;
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

public class adduser extends ListenerAdapter {
    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-adduser-Command");

    public adduser(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getUser().isBot()) return;
        if(!event.getName().equalsIgnoreCase("adduser")) return;
        if(event.getOptions().isEmpty()) return;
        event.deferReply().setEphemeral(true).queue();
        if(handler.getSessionByLeader(event.getMember().getIdLong()) == null){
            event.getHook().editOriginal("Sorry, aber du bist kein Leader eines Squads.").queue();
            return;
        }
        Session session = handler.getSessionByLeader(event.getMember().getIdLong());
        long user = (!event.getOptions().contains("user")) ? 0L : event.getOption("user").getAsLong();
        if(session.getActive_participants().contains(user)){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer ist bereits Teil deines Squads.").queue();
            return;
        }
        if(handler.getSessionByUser(user) != null){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer ist bereits Teil eines anderen Squads").queue();
            return;
        }
        if(session.getActive_participants().size() > 7){
            event.getHook().editOriginal("Sorry, aber dein Squad ist bereits voll.").queue();
            return;
        }
        SquadMember m = db.getSquadMemberById(user);
        if(!handler.waiting.contains(m)){
            event.getHook().editOriginal("Sorry, aber dieser Nutzer steht nicht auf der Warteliste.").queue();
            return;
        }
        handler.waiting.remove(m);
        session.addActive_participant(user);
        session.addParticipant(user);
        handler.updateSession(session);

        event.getJDA().getUserById(user).openPrivateChannel().flatMap((channel) -> channel.sendMessage("Du wurdest von einem Squadleader in seinen Squad gezogen.")).queue();
        event.getHook().editOriginal("Du hast den Nutzer "+ m.getIn_game_name()+" zu deinem Squad hinzugefügt.").queue();
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if(!event.getName().equalsIgnoreCase("adduser")) return;
        List<String> participants = new ArrayList<>();
        if(handler.waiting != null){
            participants= handler.waiting.stream()
                    .map(squadMember -> String.valueOf(squadMember.getDiscord_id()))
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
        }
    }
}

package me.tund.commands.normal;

import me.tund.database.Database;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class waiting extends ListenerAdapter {
    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-waiting-Command");

    public waiting(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getUser().isBot()) return;
        if(!event.getName().equalsIgnoreCase("waiting")) return;
        event.deferReply().setEphemeral(true).queue();

        if(handler.getSessionByUser(event.getMember().getIdLong()) != null || handler.waiting.contains(db.getSquadMemberById(event.getMember().getIdLong()))) {
            event.getHook().editOriginal("Du bist bereits in einem Squad oder in der Warteschlange!").queue();
            return;
        }
        if(event.getMember().getVoiceState().isDeafened() || !event.getMember().getVoiceState().inAudioChannel()){
            event.getHook().editOriginal("Da du in keinem Sprachkanal bist, wurdest du nicht auf die Warteliste gesetzt. Bitte begib dich in einen Sprachkanal und führe den Befehl noch einmal aus, oder begib dich in den Warteraum um automatisch auf die Warteliste gesetzt zu werden. Du kannst danach in einen anderen Sprachkanal wechseln.!").queue();
            return;
        }
        handler.waiting.add(db.getSquadMemberById(event.getMember().getIdLong()));
        event.getHook().editOriginal("Du wurdest auf die Warteliste gesetzt.").queue();
        event.getMember().getUser().openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Du bist nun auf der Warteliste. Sobald ein Platz frei wird, für den du geeignet bist, wirst du automatisch in den richtigen Kanal gezogen. Bitte beachte, dass du in einem Sprachkanal bleiben musst und dich nicht Gehörstummen darfst.")).queue();
    }
}

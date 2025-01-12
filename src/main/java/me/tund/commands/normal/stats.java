package me.tund.commands.normal;

import edu.stanford.nlp.util.StringUtils;
import me.tund.database.Database;
import me.tund.database.SquadMember;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class stats extends ListenerAdapter {

    private Database db = new Database();
    private final SessionHandler handler;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-stats-Command");

    public stats(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getUser().isBot()) return;
        if(!event.getName().equalsIgnoreCase("stats")) return;
        event.deferReply().setEphemeral(true).queue();
        if(event.getOptions().isEmpty()) {
            SquadMember m = db.getSquadMemberById(event.getMember().getIdLong());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Stats für den Nutzer "+ event.getMember().getEffectiveName()+" ("+m.getIn_game_name()+")");
            eb.addField("Activity: ", String.valueOf(m.getActivity()), false);
            eb.addField("K/D: ", String.valueOf(m.getKd()), false);
            eb.addField("Priority: ",String.valueOf(m.getPriority()), false);
            eb.addField("Trainings: ", String.valueOf(m.getTrainings()), false);
            eb.addField("Unit: ", m.getPreferred_unit(), false);
            eb.addField("BR:", String.valueOf(m.getPreferred_br()), false);
            eb.addField("Ersetzen:", m.isReplace()?"Ja":"Nein", false);
            eb.setFooter(m.getIn_game_name()+ " "+m.getDiscord_id());
            event.getHook().sendMessageEmbeds(eb.build()).queue();
        }else {
            String user = (!event.getOptions().contains("user")) ? "0L" : event.getOption("user").getAsString();
            SquadMember m;
            if(StringUtils.isNumeric(user)) {
                m = db.getSquadMemberById(Long.parseLong(user));
            }else{
                m = db.getSquadMemberById(event.getGuild().getMembersByName(user, false).get(0).getIdLong());
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Stats für den Nutzer "+ event.getMember().getEffectiveName()+" ("+m.getIn_game_name()+")");
            eb.addField("Activity: ", String.valueOf(m.getActivity()), false);
            eb.addField("K/D: ", String.valueOf(m.getKd()), false);
            eb.addField("Priority: ",String.valueOf(m.getPriority()), false);
            eb.addField("Trainings: ", String.valueOf(m.getTrainings()), false);
            eb.addField("Unit: ", m.getPreferred_unit(), false);
            eb.addField("BR:", String.valueOf(m.getPreferred_br()), false);
            eb.addField("Ersetzen:", m.isReplace()?"Ja":"Nein", false);
            eb.setFooter(m.getIn_game_name(), String.valueOf(m.getDiscord_id()));
            event.getHook().sendMessageEmbeds(eb.build()).setEphemeral(false).queue();
        }

    }
}

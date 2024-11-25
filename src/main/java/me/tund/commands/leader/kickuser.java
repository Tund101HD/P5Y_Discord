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
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class kickuser extends ListenerAdapter {

}

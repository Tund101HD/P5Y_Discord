package me.tund;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.tund.commands.leader.endsession;
import me.tund.commands.leader.fillsession;
import me.tund.commands.leader.startsession;
import io.github.cdimascio.dotenv.Dotenv;
import me.tund.commands.normal.joinsession;
import me.tund.commands.normal.register.register;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import me.tund.utils.sessions.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;


public class Main {

    public static JDA bot;
    private static final Logger logger = LoggerFactory.getLogger("ClientMain");
    public static SessionHandler sessionHandler;
    public final static long GUILD_ID = 1168519713274470400L;
    public final static long WARTERAUM_ID = 1270850047386452010L;
    public final static long SQUAD1_GROUND = 1270849917526474793L;
    public final static long SQUAD2_GROUND = 1270850017548308611L;
    public final static long SQUAD1_AIR = 1270849826174795887L;
    public final static long SQUAD2_AIR = 1270849940792541246L;
    public final static long CW_ROLE = 1274145118999416944L;
    public final static long SL_ROLE = 1270850472936341534L;
    public final static long AFK_CHANNEL_ID = 1290761286459592746L;



    public static void main(String[] args) throws InterruptedException {
        Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
        logger.debug("Started loading the bot. Start time: {}", System.currentTimeMillis());
        bot = JDABuilder.create(dotenv.get("TOKEN"),
                        EnumSet.allOf(GatewayIntent.class))
                .setActivity(Activity.playing("Warthunder Lobby chillen mit den Boys")).setMemberCachePolicy(MemberCachePolicy.ALL)
                .build().awaitReady();
        logger.debug("Loading all commands and listeners.");
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix("cw/");
        builder.setOwnerId("1158495140969713815");
        CommandClient client = builder.build();
        sessionHandler = new SessionHandler();
        bot.addEventListener(client);
        bot.addEventListener(new startsession(sessionHandler));
        bot.addEventListener(new fillsession(sessionHandler));
        bot.addEventListener(new register());
        bot.addEventListener(new joinsession(sessionHandler));
        bot.addEventListener(new endsession(sessionHandler));
        bot.getGuildById(Main.GUILD_ID).updateCommands().addCommands(
                Commands.slash("register", "Registriere dich um bei CW mitzumachen.").addOptions(new OptionData(OptionType.STRING, "stat", "Welchen Wert du aktualisieren möchtest oder ob du von Vorne Anfangen möchtest.", false, true)),
                Commands.slash("startsession", "Starte eine CW-Session für Squad1/2. Du musst dich dafür in einem Channel befinden.")
                        .addOptions(
                                new OptionData(OptionType.STRING, "min-activity", "Die Mindestaktivität die ein Nutzer benötigt um automatisch gewählt zu werden.",false, true),
                                new OptionData(OptionType.STRING, "br", "Auf welchen BR die Session gespielt wird. Es werden nur Nutzer berücksichtigt die das BR haben!",false, true),
                                new OptionData(OptionType.STRING, "exclude_id", "Eine Liste and (Discord-)IDs die nicht Berücksichtigt werden soll, getrennt durch Kommas.",false, true),
                                new OptionData(OptionType.STRING, "min-priority", "Die Mindestpriorität, die ein Nutzer haben soll um automatisch gewählt zu werden.",false, true)
                        ),
                Commands.slash("fillsession", "Fülle deine Session mit neuen Leuten auf. Gib bei Bedarf neue Anforderungen an.")
                        .addOptions(
                                new OptionData(OptionType.STRING, "min-activity", "Die Mindestaktivität die ein Nutzer benötigt um automatisch gewählt zu werden.",false, true),
                                new OptionData(OptionType.STRING, "br", "Auf welchen BR die Session gespielt wird. Es werden nur Nutzer berücksichtigt die das BR haben!",false, true),
                                new OptionData(OptionType.STRING, "exclude_id", "Eine Liste and (Discord-)IDs die nicht Berücksichtigt werden soll, getrennt durch Kommas.",false, true),
                                new OptionData(OptionType.STRING, "min-priority", "Die Mindestpriorität, die ein Nutzer haben soll um automatisch gewählt zu werden.",false, true)
                        ),
                Commands.slash("endsession", "Beende deine Session"),
                Commands.slash("joinsession", "Trete einer Session bei oder lasse dich auf die Warteliste setzen.")).queue();

        logger.info("Finished loading bot. End time: {}", System.currentTimeMillis());
    }
}

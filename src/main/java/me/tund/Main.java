package me.tund;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.tund.commands.leader.startsession;
import io.github.cdimascio.dotenv.Dotenv;
import me.tund.commands.normal.register;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import me.tund.utils.sessions.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.logging.Level;


public class Main {

    public static JDA bot;
    private static final Logger logger = LoggerFactory.getLogger("ClientMain");
    public static SessionHandler sessionHandler;
    public final static long GUILD_ID = 831319290510442496L;
    public final static long WARTERAUM_ID = 1193295964526608514L;
    public final static long SQUAD1_GROUND = 1147607587961978900L;
    public final static long SQUAD2_GROUND = 979779381398487090L;
    public final static long SQUAD1_AIR = 1193297533473140827L;
    public final static long SQUAD2_AIR = 1193297587424469012L;


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
        bot.addEventListener(client);
        bot.addEventListener(new startsession());
        bot.addEventListener(new register());
        sessionHandler = new SessionHandler();
        logger.info("Finished loading bot. End time: {}", System.currentTimeMillis());
    }
}

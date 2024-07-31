import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static JDA bot;

    public static void main(String[] args) throws InterruptedException {
        Logger log = Logger.getLogger("Main");
        Dotenv dotenv = Dotenv.configure().directory("./utils").load();
        log.log(Level.INFO, "Loading bot. Start time: "+System.currentTimeMillis());
        bot = JDABuilder.create(dotenv.get("TOKEN"),
                        EnumSet.allOf(GatewayIntent.class))
                .setActivity(Activity.listening("cw/")).setMemberCachePolicy(MemberCachePolicy.ALL)
                .build().awaitReady();
        log.log(Level.INFO, "Perparing all commands and listeners.");
        CommandClientBuilder builder = new CommandClientBuilder();
        builder.setPrefix("x/");
        builder.setOwnerId("1158495140969713815");
        CommandClient client = builder.build();
        bot.addEventListener(client);
        log.log(Level.INFO, "Connecting to Database.");
        //FIXME Database

        log.log(Level.INFO, "Finished loading bot. End time: "+System.currentTimeMillis());

    }
}

package me.tund.utils;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.internal.handle.GuildMemberAddHandler;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utilities {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("UtilClient");
    public Utilities(){

    }


    public static void sendMessageAndDeleteAfter(String input, int seconds, TextChannel channel, Message original){
        channel.sendMessage(original.getAuthor().getAsMention()+" "+ input).queue(
                (success) -> {
                    success.delete().queueAfter(seconds, TimeUnit.SECONDS);
                }
        );
        original.delete().queue();
    }
    public static void sendMessageAndDeleteAfter(String input, int seconds, TextChannel channel, User author){
        channel.sendMessage(author.getAsMention() + " "+input).queue(
                (success) -> {
                    success.delete().queueAfter(seconds, TimeUnit.SECONDS);
                }
        );
    }
    public static Connection checkValidConnection(Connection con) {
        Dotenv dotenv = Dotenv.load();
        try{
            if(!con.isValid(1)){
                logger.debug("Resetting Database-Connection. Please wait.");
                con = DriverManager.getConnection(
                        dotenv.get("DB_URL"),
                        dotenv.get("DB_USER"), dotenv.get("DB_PWD")
                );
                return con;
            }
        }catch (Exception e){
            logger.error("Something went wrong trying to connect to the Database. Stacktrace: {}", e.getStackTrace());}
        return con;
    }

}

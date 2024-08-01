package commands.leader;

import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import utils.sessions.Session;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class startsession extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("startsession")) {
            event.deferReply().queue();


            String minact = (event.getOption("min-activity").getAsString()==null || event.getOption("min-activity").getAsString().isEmpty())
                        ?"0":event.getOption("min-activity").getAsString(); // min activity
            double br;
            try {
                br = (event.getOption("br").getAsString()==null || event.getOption("br").getAsString().isEmpty())
                        ?13.0d:event.getOption("br").getAsDouble(); //br
            }catch (Exception e){
                br = 13.0d;
            }
            String exlude_id = event.getOption("exlude_id").getAsString()==null?"":event.getOption("exlude_id").getAsString(); //CSV von IDs
            String min_priority = (event.getOption("min-priority").getAsString()==null || event.getOption("min-priority").getAsString().isEmpty())
                        ?"0":event.getOption("min-priority").getAsString(); //minprio

            Session session = new Session(new Date(new DateTime(System.currentTimeMillis(),
                    DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC+02:00"))).getMillis()), br, event.getMember().getIdLong());
            session.setExclude_ids(Lists.transform(Arrays.asList(exlude_id.split(";")), Longs.stringConverter()));
            try {
                session.setMin_acitivty(Integer.parseInt(minact));
            }catch (Exception e) {
                session.setMin_acitivty(0);
            }
            try {
                session.setMin_priority(Integer.parseInt(min_priority));
            }catch (Exception e) {
                session.setMin_priority(0);
            }
        }
    }


    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("startsession")) {
            switch (event.getFocusedOption().getName()){
                case "min-activity":
                    String[] words = new String[]{"500", "800", "1000", "1200", "0"};
                    List<Command.Choice> options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                            .map(word -> new Command.Choice(word, word)) // map the words to choices
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "br":
                    words = new String[]{"13.0", "12.0", "11.0", "10.0", "9.0", "8.0", "7.0", "6.0", "5.0", "4.0"};
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "exclude_id":
                    words = new String[]{"1146100281273233441"}; //FIXME Liste and gebannten Nutzern aus der Vergangenheit
                    options = Stream.of(words)
                            .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                            .map(word -> new Command.Choice(word, word))
                            .collect(Collectors.toList());
                    event.replyChoices(options).queue();
                    break;
                case "min-priority":
                    words = new String[]{"3", "2", "1"}; //Imagine
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

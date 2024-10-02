package me.tund.utils.sessions;

import me.tund.Main;
import me.tund.database.SquadMember;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SessionWaitingRefreshTask implements Runnable{

    private final Guild g = Main.bot.getGuildById(Main.GUILD_ID);
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("WaitingRefreshClient");
    @Override
    public void run() {
        List<SquadMember> waiting = Main.sessionHandler.waiting;
        for(SquadMember member : waiting){
          boolean still_waiting =  g.getMemberById(member.getDiscord_id()).getVoiceState().inAudioChannel() &&
                  !g.getMemberById(member.getDiscord_id()).getVoiceState().isDeafened() &&
                  !g.getMemberById(member.getDiscord_id()).getVoiceState().isMuted() &&
                  !(g.getMemberById(member.getDiscord_id()).getVoiceState().getChannel().getIdLong() == Main.AFK_CHANNEL_ID);
          if(!still_waiting){
              Main.sessionHandler.waiting.remove(member);
              Main.sessionHandler.waiting_sessions.remove(member);
              Main.bot.getUserById(member.getDiscord_id()).openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Du wurdest auf Grund von" +
                      " Inaktivität aus der Liste der wartenden Spieler entfernt. Bitte vergewissere dich, dass du unter keinen Umständen gevollstummt, in keinem" +
                      " Voicechannel oder dich im AFK-Bereich befindest. Falls du doch noch da bist, begib dich in einen der Channel und gibt ``/waiting <session_id>`` ein.")).queue();
          }
        }
    }
}

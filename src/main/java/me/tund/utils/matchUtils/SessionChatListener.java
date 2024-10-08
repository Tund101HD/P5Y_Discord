package me.tund.utils.matchUtils;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SessionChatListener extends ListenerAdapter{

    private final Database db = new Database();
    private final SessionHandler handler = Main.sessionHandler;
    private final Recognizer recognizer = new Recognizer();
    private static final Logger logger = LoggerFactory.getLogger("SessionChatClient");

    private long channelID = 0;
    public SessionChatListener(long channelID) {
        this.channelID = channelID;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getAttachments().isEmpty()) return;
        String s = "";
        Message.Attachment[] attachments = event.getMessage().getAttachments().toArray(new Message.Attachment[0]);
        Message.Attachment[] pictures = new Message.Attachment[10];
        if(attachments.length > 10) {
            logger.info("Too many attachments. ({})", attachments.length);
            return;
        }
        int i = 0;
        for(Message.Attachment attachment : attachments) {
            if(attachment.isImage()) pictures[i] = attachment;
            i++;
        }
        i=0;
        String[] responses = new String[10];
        for(Message.Attachment attachment : pictures) {
            if(attachment == null) continue;
            CompletableFuture<InputStream> stream = attachment.getProxy().download();
            try {
                InputStream inputStream = stream.get();
                byte[] bytes = {};
                try {
                    bytes = inputStream.readAllBytes();
                } catch (IOException e) {
                    logger.info("Couldn't read image. Exiting");
                }
                if(bytes == null || bytes.length == 0) continue;
                Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
                responses[i] = recognizer.extractStringFromMat(mat, "rus+deu+eng"); //FIXME Find Solution for OCR Precision
                i++;
            } catch (InterruptedException e) {
                logger.info("Download of attachment was interrupted. Exiting");
                continue;
            } catch (ExecutionException e) {
                logger.info("Couldn't download attachment. Exiting");
                continue;
            } catch (Exception e){
                logger.info("Couldn't download attachment for unknown reason. Exiting");
                continue;
            }
        }
        if(responses.length == 0) return;
        if (channelID == 0) return;
        for(String response : responses) {
            if(response == null) continue;
            event.getGuild().getTextChannelById(channelID).sendMessage("Extracted Text from Image is: \n"+response).queue();
        }
    }

}

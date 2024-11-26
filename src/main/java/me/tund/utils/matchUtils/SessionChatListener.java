package me.tund.utils.matchUtils;

import com.google.common.primitives.Ints;
import com.google.gson.JsonObject;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.matchUtils.imageUtils.YoloWrapper;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.FileUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SessionChatListener extends ListenerAdapter{

    private final Database db = new Database();
    private final SessionHandler handler = Main.sessionHandler;
    private final Recognizer recognizer = new Recognizer();
    private static final Logger logger = LoggerFactory.getLogger("P5Y-Sessionchat-Client");

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
        for(Message.Attachment attachment : pictures) {
            if(attachment == null) continue;
            CompletableFuture<InputStream> stream = attachment.getProxy().download();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream inputStream = stream.get();
                inputStream.transferTo(baos);
                InputStream firstClone = new ByteArrayInputStream(baos.toByteArray());
                InputStream secondClone = new ByteArrayInputStream(baos.toByteArray());
                JsonObject detections =  YoloWrapper.getDetections(firstClone);

                Mat[] mats = YoloWrapper.drawDetectionsOnImage(secondClone, detections);
                ArrayList<FileUpload> files = new ArrayList<>();
                for (int j = 0; j < mats.length; j++) {
                    BufferedImage gray = YoloWrapper.Mat2BufferedImage(mats[j]);
                    byte[] bytes =  YoloWrapper.toByteArray(gray, "jpg");
                    files.add(FileUpload.fromData(bytes, "test"+j+".jpg"));
                }
                event.getChannel().sendFiles(files).queue();
                event.getChannel().sendMessage(recognizer.recognizeFriendlyPlayerScores(mats[2], mats[1])).queue();
            } catch (InterruptedException e) {
                logger.error("Download of attachment was interrupted. Exiting");
                continue;
            } catch (ExecutionException e) {
                logger.error("Couldn't download attachment. Exiting");
                continue;
            } catch (Exception e){
                logger.error("Couldn't download attachment for unknown reason. Exiting");
                e.printStackTrace();
                continue;
            }
        }
    }

}

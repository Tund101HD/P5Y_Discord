package me.tund.utils.matchUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.matchUtils.imageUtils.YoloWrapper;
import me.tund.utils.sessions.SessionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static org.opencv.imgproc.Imgproc.*;

public class Recognizer {


    private final Database db = new Database();
    private final SessionHandler handler = Main.sessionHandler;
    private final Tesseract tesseract = new Tesseract();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("P5Y-OCR-Module");
    public Recognizer() {

    }

    /**
     * Performs a text recognition on a given mat for either german, english or russian. This will not filter out any
     * images that are not in the needed format (aka. cropped with only the statistics in frame)
     * @param input Image mat that needs to be processed
     * @param lang deu or eng depending on the needed language
     * @return A String of the detected text in the image
     */
    public String extractScoresFromMat(Mat input, String lang) throws Exception {
        String result = "";
        Mat grayMat = new Mat();
        cvtColor(input, grayMat, COLOR_RGB2GRAY);
        Imgproc.threshold(grayMat.clone(), grayMat,120, 255, THRESH_OTSU);
        BufferedImage image = mat2BufferedImage(grayMat.clone());
        File outputfile = new File("src/main/resources/test.jpg");
        ImageIO.write(image, "jpg", outputfile);
        try{
            tesseract.setOcrEngineMode(1);
            tesseract.setVariable("matcher_bad_match_pad", "0.13");
            tesseract.setVariable("tessedit_unrej_any_wd", "1");
            tesseract.setVariable("tessedit_override_permuter", "0");
            tesseract.setVariable("tessedit_char_whitelist", "0123456789 "); //Numbers only
            tesseract.setDatapath("src/main/resources/ocr");
            tesseract.setLanguage("num");
            result = tesseract.doOCR(mat2BufferedImage(grayMat));
        }catch (Exception e){
            logger.error("An exception occurred while extracting string from image {}", e.getMessage());
        }
        return result;
    }

    /**
     * @deprecated
     * Legacy tesseract api method for extracting text. (To be removed)
     * @param input Input Mat to apply OCR on
     * @param lang Language for the detection (Typically rus+eng+deu for text and eng for scores)
     * @return A String containing all detected text
     * @throws Exception
     */
    public String extractNamesFromMat(Mat input, String lang) throws Exception {
        String result = "";
        Mat grayMat = new Mat();
        cvtColor(input, grayMat, COLOR_RGB2GRAY);
        Imgproc.threshold(grayMat.clone(), grayMat,120, 255, THRESH_OTSU);
        BufferedImage image = mat2BufferedImage(grayMat.clone());
        File outputfile = new File("src/main/resources/test2.jpg");
        ImageIO.write(image, "jpg", outputfile);
        try{
            tesseract.setOcrEngineMode(1);
            tesseract.setVariable("matcher_bad_match_pad", "0.13");
            tesseract.setVariable("tessedit_unrej_any_wd", "1");
            tesseract.setVariable("tessedit_override_permuter", "0");
            tesseract.setDatapath("src/main/resources/ocr");
            tesseract.setVariable("tessedit_char_whitelist", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüßÄÖÜабвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ0123456789#_* ");
            tesseract.setLanguage(lang);
            result = tesseract.doOCR(mat2BufferedImage(grayMat));
        }catch (Exception e){
            logger.error("An exception occurred while extracting string from image {}", e.getMessage());
        }
        return result;
    }

    /**
     * Converts a Mat to a BufferedImage with type jpg.
     *
     * DO NOT USE WITH ANY RECOGNIZER MODEL AS THERE IS NO PREPROCESSING
     * @param matrix
     * @return BufferedImage (jpg)
     * @throws Exception
     */
    public static BufferedImage mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi= ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

    /**
     * Uses Vertex AI to convert the preprocessed images into text and combines the data
     * @param players Mat image containing friendly players
     * @param scores Mat image containing friendly scores
     * @return EmbedMessage containing PlayerStats
     * @throws Exception
     */
    public MessageEmbed recognizeFriendlyPlayerScores(Mat players, Mat scores) throws Exception {
        JsonObject score = Main.gemini.getScores(YoloWrapper.Mat2BufferedImage(scores));
        JsonObject names = Main.gemini.getPlayerNames(YoloWrapper.Mat2BufferedImage(players));
        if(names == null || scores == null){
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Error: Couldn't process image!");
            builder.setFooter("Null Items");
            return builder.build();
        }

        try{
            EmbedBuilder builder = new EmbedBuilder();
            StringBuilder strings = new StringBuilder();
            JsonArray scoreArray = score.getAsJsonArray("scores");
            JsonArray namesArray = names.get("players").getAsJsonArray();
            builder.setTitle("Stats für "+names.getAsJsonObject("clan").get("name").getAsString()+"| Runden-ID: "+System.currentTimeMillis());
            builder.addBlankField(false);
            for (int i = 0; i < scoreArray.size(); i++) {
                String[] s = scoreArray.get(i).getAsString().split(" ");
                strings.append("Deaths: ").append(s[0]).append("\n");
                strings.append("Points: ").append(s[1]).append("\n");
                strings.append("Assists: ").append(s[2]).append("\n");
                strings.append("Kills (Ground): ").append(s[3]).append("\n");
                strings.append("Kills (Air): ").append(s[4]).append("\n");
                strings.append("Total Score: ").append(s[5]).append("\n");


                for (int j = 0; j < namesArray.size(); j++) {}
                builder.addField(namesArray.get(i).getAsString(),
                        strings.toString(),
                        false
                );
                strings.setLength(0);
            }
            builder.addBlankField(false);
            builder.setFooter("Nice round!");
            return builder.build();
        }catch (Exception e){
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Error: Couldn't process image!");
            builder.setDescription(e.getMessage());
            builder.setFooter("Malformed JSON");
            return builder.build();
        }
    }

    /**
     * Uses Vertex AI to convert the preprocessed images into text and combines the data
     * @param players Mat image containing enemy players
     * @param scores Mat image containing enemy scores
     * @return EmbedMessage containing PlayerStats
     * @throws Exception
     */
    public MessageEmbed recognizeEnemyPlayerScores(Mat players, Mat scores, String roundID) throws Exception {
        JsonObject score = Main.gemini.getScores(YoloWrapper.Mat2BufferedImage(scores));
        JsonObject names = Main.gemini.getEnemyPlayerNames(YoloWrapper.Mat2BufferedImage(players));
        if(names == null || scores == null){
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Error: Couldn't process image!");
            builder.setFooter("Null Items");
            return builder.build();
        }


        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder strings = new StringBuilder();
        JsonArray scoreArray = score.getAsJsonArray("scores");
        JsonArray namesArray = names.get("players").getAsJsonArray();
        builder.setTitle("Stats für "+names.getAsJsonObject("clan").get("name").getAsString()+"| Runden-ID: "+roundID);
        builder.addBlankField(false);
        for (int i = 0; i < scoreArray.size(); i++) {
            String[] s = scoreArray.get(i).getAsString().split(" ");
            strings.append("Deaths: ").append(s[0]).append("\n");
            strings.append("Points: ").append(s[1]).append("\n");
            strings.append("Assists: ").append(s[2]).append("\n");
            strings.append("Kills (Ground): ").append(s[3]).append("\n");
            strings.append("Kills (Air): ").append(s[4]).append("\n");
            strings.append("Total Score: ").append(s[5]).append("\n");


            for (int j = 0; j < namesArray.size(); j++) {}
            builder.addField(namesArray.get(i).getAsString(),
                    strings.toString(),
                    false
            );
            strings.setLength(0);
        }
        builder.addBlankField(false);
        builder.setFooter("Nice round!");
        return builder.build();
    }


}

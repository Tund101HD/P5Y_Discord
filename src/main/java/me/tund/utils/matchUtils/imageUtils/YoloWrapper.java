package me.tund.utils.matchUtils.imageUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;
import me.tund.utils.matchUtils.Recognizer;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.opencv.imgproc.Imgproc.*;

public class YoloWrapper {

    public YoloWrapper(){

    }
    private static final Logger logger = LoggerFactory.getLogger("P5Y-YoloWrapper");
    public static JsonObject getDetections(InputStream fis) throws IOException {
        logger.info("Detecting objects...");
        //String filePath = System.getProperty("user.dir") + System.getProperty("file.separator") + "YOUR_IMAGE.jpg";
        Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
        File file = new File("src/main/resources/temp_image_"+System.currentTimeMillis()+".jpg");
        FileUtils.copyInputStreamToFile(fis, file);
        String encodedFile;
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);
        encodedFile = new String(Base64.getEncoder().encode(bytes), StandardCharsets.US_ASCII);
        fileInputStreamReader.close();

        String API_KEY = dotenv.get("ROBOFLOW_API");
        String MODEL_ENDPOINT = "p5y_discord/1";
        String uploadURL = "https://detect.roboflow.com/" + MODEL_ENDPOINT + "?api_key=" + API_KEY
                + "&name=YOUR_IMAGE.jpg";
        HttpURLConnection connection = null;
        try {
            URL url = new URL(uploadURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", Integer.toString(encodedFile.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");
            connection.setUseCaches(false);
            connection.setDoOutput(true);


            String json = getString(connection, encodedFile);
            GsonBuilder gsonBuilder = new GsonBuilder();
            if(connection != null) connection.disconnect();
            file.delete(); //FIXME Doesn't delete file
            return gsonBuilder.create().fromJson(json, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }
        return null;
    }

    /**
     * Return an array of Mat images for each detection class
     * 0: friendly_scores
     * 1: friendly_names
     * 2: enemy_scores
     * 3: enemy_names
     * 4: full_scoreboard @Empty Mat for speed reasons (No usage right now)
     * 5: friendly_vehicles @Empty Mat for speed reasons (No usage right now)
     * @param fis Inputstream of the full image
     * @param detections Roboflow response containing the detections
     * @return Mat[] of all detections
     */
    public static Mat[] detectionsToImage(InputStream fis, JsonObject detections) throws IOException {
        byte [] bytes = fis.readAllBytes();
        Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
        Mat[] standAloneImages = convertDetectionsToStandaloneMatImages(mat.clone(), detections.getAsJsonArray("predictions"));
        logger.info("Size of standalone images: {}", standAloneImages.length);
        if(fis != null) fis.close();
        return standAloneImages;
    }


    private static @NotNull String getString(HttpURLConnection connection, String encodedFile) throws IOException {
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(encodedFile);
        wr.close();


        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        String json = stringBuilder.toString();
        return json;
    }

    /**
     * Preprocesses the Mat to display a black and white image for maximum accuracy in image recognition
     * @param mat Input Mat of type RGB
     * @return A black and white BufferedImage
     * @throws IOException
     */
    public static BufferedImage Mat2BufferedImage(Mat mat) throws Exception {
        Mat grayMat = new Mat();
        cvtColor(mat, grayMat, COLOR_RGB2GRAY);
        Imgproc.threshold(grayMat.clone(), grayMat,120, 255, THRESH_OTSU);
        BufferedImage bufImage = Recognizer.mat2BufferedImage(grayMat);
        return bufImage;
    }

    public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    /**
     * Return an array of Mat images for each detection class
     * 0: friendly_scores
     * 1: friendly_names
     * 2: enemy_scores
     * 3: enemy_names
     * 4: full_scoreboard
     * 5: friendly_vehicles
     * @param src
     * @param detections
     * @return
     */
    private static Mat[] convertDetectionsToStandaloneMatImages(Mat src, JsonArray detections){ //FIXME This is incredibly slow fix this. Is src.clone() really needed?
        Mat friendly_scores = new Mat();
        Mat friendly_names = new Mat();
        Mat enemy_scores = new Mat();
        Mat enemy_names = new Mat();
        Mat full_scoreboard = new Mat();
        Mat friendly_vehicles = new Mat();
        for (int i = 0; i < detections.size(); i++) {
            JsonObject object = detections.get(i).getAsJsonObject();
            double x = object.get("x").getAsDouble();
            double y = object.get("y").getAsDouble();
            double width = object.get("width").getAsDouble();
            double height = object.get("height").getAsDouble();
            int x1 = (int)(x-3 - width / 2);
            int y1 = (int)(y-3 - height / 2);
            int x2 = (int)(x-3 + width / 2);
            int y2 = (int)(y-3 + height / 2);
            switch (object.get("class").getAsString()) {
                case "Enemy Score":
                    Rect rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    enemy_scores = new Mat(src.clone(), rect);
                    break;
                case "Friendly Score":
                    rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    friendly_scores = new Mat(src.clone(), rect);
                    break;
                case "Enemy Names":
                    rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    enemy_names = new Mat(src.clone(), rect);
                    break;
                case "Friendly Names":
                    rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    friendly_names = new Mat(src.clone(), rect);
                    break;
                case "Full Scoreboard":
                    rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    full_scoreboard = new Mat(src.clone(), rect);
                    break;
                case "Friendly Vehicles":
                    rect = new Rect(new Point(x1,y1), new Point(x2, y2));
                    friendly_vehicles = new Mat(src.clone(), rect);
                    break;
            }


        }
        return new Mat[]{friendly_scores, friendly_names,enemy_scores,enemy_names,full_scoreboard, friendly_vehicles};
    }




}
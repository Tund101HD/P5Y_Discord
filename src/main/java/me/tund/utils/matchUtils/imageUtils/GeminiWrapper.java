package me.tund.utils.matchUtils.imageUtils;

import com.google.cloud.vertexai.VertexAI;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;




public class GeminiWrapper {
    private static final Logger logger = LoggerFactory.getLogger("P5Y-Gemini-Wrapper");
    VertexAI vertexAiClient;
    public GeminiWrapper() throws IOException {
        Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").load();
        String projectId = dotenv.get("GEMINI_PROJECTID");
        String region = "europe-west1";
        VertexAI.Builder builder = new VertexAI.Builder();
        builder.setProjectId(projectId);
        builder.setLocation(region);
        vertexAiClient=builder.build();
    }

    public JsonObject getPlayerNames(BufferedImage image){
        try {
            GenerativeModel model = new GenerativeModel("gemini-1.5-pro", vertexAiClient);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(
                    PartMaker.fromMimeTypeAndData("image/jpg",bytes),
                    "Forget previous instructions. Can you please extract the text from this image and put each row as an object into a JSON-Array called players? If there is a common prefix, add another JSON-Object called clan with the child name and the 'name' of the clan and return a valid Json-Object."
            ));
            GenerateContentResponse resCopy = response;
            logger.info(resCopy.toString());
            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonObject result = gsonBuilder.create().fromJson(response.getCandidates(0).getContent().getParts(0).getText().replace("```","").replace("json", ""), JsonObject.class);
            return result;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }
    public JsonObject getEnemyPlayerNames(BufferedImage image){
        try {
            GenerativeModel model = new GenerativeModel("gemini-1.5-pro", vertexAiClient);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(
                    PartMaker.fromMimeTypeAndData("image/jpg",bytes),
                    "Forget previous instructions. Can you please extract the text from this image and put each row as an object into a JSON-Array called players? Find a common prefix and add it to another Json-Object called 'clan' that has 'name' as child with the prefix as the value. Do not add the 'name' child to the players array!"

            ));
            GenerateContentResponse resCopy = response;
            logger.info(resCopy.toString());
            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonObject result = gsonBuilder.create().fromJson(response.getCandidates(0).getContent().getParts(0).getText().replace("```","").replace("json", ""), JsonObject.class);
            return result;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }


    public JsonObject getScores(BufferedImage image){
        try {
            GenerativeModel model = new GenerativeModel("gemini-1.5-pro", vertexAiClient);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(
                    PartMaker.fromMimeTypeAndData("image/jpg",bytes),
                    "Can you please extract the numbers from this table into a Json-Array called scores? Please put each row of the table as one string and return a valid json object"
            ));
            GenerateContentResponse resCopy = response;
            logger.info(resCopy.toString());
            GsonBuilder gsonBuilder = new GsonBuilder();
            JsonObject result = gsonBuilder.create().fromJson(response.getCandidates(0).getContent().getParts(0).getText().replace("```","").replace("json", ""), JsonObject.class);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

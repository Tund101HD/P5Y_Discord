package me.tund.utils.matchUtils;

import me.tund.Main;
import me.tund.database.Database;
import me.tund.utils.sessions.SessionHandler;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;

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
    public String extractStringFromMat(Mat input, String lang){
        String result = "";
        Mat grayMat = new Mat();
        cvtColor(input, grayMat, COLOR_BGR2GRAY);
        try{
            tesseract.setOcrEngineMode(1);
            tesseract.setVariable("matcher_bad_match_pad", "0.13");
            tesseract.setVariable("tessedit_unrej_any_wd", "1");
            tesseract.setVariable("tessedit_override_permuter", "0");
            tesseract.setVariable("tessedit_char_whitelist", "0123456789");
            tesseract.setDatapath("src/main/resources/ocr");
            tesseract.setLanguage(lang);
            result = tesseract.doOCR(mat2BufferedImage(grayMat));
        }catch (Exception e){
            logger.error("An exception occurred while extracting string from image {}", e.getMessage());
        }
        return result;
    }

    static BufferedImage mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi= ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }


}

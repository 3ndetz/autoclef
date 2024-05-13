package adris.altoclef.util;

import adris.altoclef.Debug;
import net.minecraft.client.MinecraftClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageComparer {
    public static List<File> listFilesForFolder(final File folder,  List<File> resultFileList) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, resultFileList);
            } else {
                resultFileList.add(fileEntry);
                //System.out.println(fileEntry.getName());
            }
        }
        return resultFileList;
    }

    public static List<File> getCaptchaDatasetFiles(){
        List<File> resultFileList = new ArrayList<File>();
        File screens_folder = new File(MinecraftClient.getInstance().runDirectory,"map_screenshots");
        //File screens_folder_4 = new File(screens_folder, "classificated4");
        //File screens_folder_5 = new File(screens_folder, "classificated5");
        //File screens_folder_bad_samples = new File(screens_folder, "zchobabke");
        //listFilesForFolder(screens_folder_4, resultFileList);
        //listFilesForFolder(screens_folder_5, resultFileList);
        //listFilesForFolder(screens_folder_bad_samples, resultFileList);
        listFilesForFolder(screens_folder, resultFileList);
        return resultFileList;
    }


    public static BufferedImage byte2BufferedImage(byte[] pixels) throws IllegalArgumentException {
        //https://stackoverflow.com/questions/12705385/how-to-convert-a-byte-to-a-bufferedimage-in-java

        //TRY 3
        //BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        //img.setData(Raster.createRaster(img.getSampleModel(), new DataBufferByte(pixels, pixels.length), new Point() ) );
        //return img;


        //TRY 2 (not checked)
        ByteArrayInputStream stream = null;

        try {
            stream = new ByteArrayInputStream(pixels);
            return ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
            Debug.logWarning("[IMAGE BYTES2BUFF] ERROR WHEN READING BYTES IMG!"+e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    Debug.logWarning("[IMAGE BYTES2BUFF] ERROR closing image input stream: "+ex.getMessage(), ex);
                }
            }
        }

        //TRY 1
        //BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        //byte[] imgData = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        //System.arraycopy(pixels, 0, imgData, 0, pixels.length);
        return null;
    }
    public static String checkBytesImageInDataset(byte[] pixels){
        try{
            BufferedImage img_to_check = byte2BufferedImage(pixels);
            for(File sample : getCaptchaDatasetFiles()){
                if(compareImage(img_to_check,sample)){
                    Debug.logMessage("СОВПАЛО! Файл="+sample.getName());
                    return sample.getName();
                }
            }
        }
        catch (Exception e){
            System.out.println("Failed LOAD IMAGE ...");
        }
        return "";
    }
    public static boolean compareImage(BufferedImage biA, File fileB) {
        try {
            BufferedImage biB = ImageIO.read(fileB);
            return compareImage(biA, biB);
        } catch (Exception e) {
            System.out.println("Failed to compare image files [STAGE 0] ...");
            return false;
        }
    }

    public static boolean compareImage(File fileA, File fileB) {
        try {
            BufferedImage biA = ImageIO.read(fileA);

            BufferedImage biB = ImageIO.read(fileB);
            return compareImage(biA, biB);
        } catch (Exception e) {
            System.out.println("Failed to compare image files [STAGE 0] ...");
            return false;
        }
    }
    public static boolean compareImage(BufferedImage biA, BufferedImage biB) {
        try {
            // take buffer data from botm image files //

            DataBuffer dbA = biA.getData().getDataBuffer();

            int sizeA = dbA.getSize();

            DataBuffer dbB = biB.getData().getDataBuffer();
            int sizeB = dbB.getSize();
            // compare data-buffer objects //
            if (sizeA == sizeB) {
                for (int i = 0; i < sizeA; i++) {
                    if (dbA.getElem(i) != dbB.getElem(i)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("Failed to compare image files [STAGE 1] ...");
            return false;
        }
    }
}

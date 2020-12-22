package util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;


public class SerializableImage implements Serializable {
    private int width, height;
    private int[][] data;

    public SerializableImage() {}

    public SerializableImage(Image image){
        width = (int) image.getWidth();
        height = (int) image.getHeight();
        data = new int[width][height];

        PixelReader r = image.getPixelReader();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = r.getArgb(i, j);
            }
        }
    }

    public SerializableImage(String path){
        this(new Image(path));
    }

    public void setImage(Image image) {
        width = ((int) image.getWidth());
        height = ((int) image.getHeight());
        data = new int[width][height];

        PixelReader r = image.getPixelReader();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = r.getArgb(i, j);
            }
        }

    }

    public void loadImage(String path){
        Image image = new Image(path);
        setImage(image);
    }
/*
    public void saveImage(String fileName){
        Image img = getImage();
        int width = (int) img.getWidth();
        int height = (int) img.getHeight();
        PixelReader reader = img.getPixelReader();
        byte[] buffer = new byte[width * height * 4];
        WritablePixelFormat<ByteBuffer> format = PixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("test.data"));
            for(int count = 0; count < buffer.length; count += 4) {
                out.write(buffer[count + 2]);
                out.write(buffer[count + 1]);
                out.write(buffer[count]);
                out.write(buffer[count + 3]);
            }
            out.flush();
            out.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
*/
    public void saveImage(String fileName) throws Exception{
        Image image = getImage();
        File outputFile = new File(fileName);
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Image getImage() {
        WritableImage img = new WritableImage(width, height);

        PixelWriter w = img.getPixelWriter();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                w.setArgb(i, j, data[i][j]);
            }
        }

        return img;
    }

}
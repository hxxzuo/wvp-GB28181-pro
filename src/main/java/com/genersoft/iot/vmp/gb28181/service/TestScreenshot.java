package com.genersoft.iot.vmp.gb28181.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestScreenshot {

    public static void main(String[] args) {
        String videoFileName = "16-26.mp4";
        String resourcePath = TestScreenshot.class.getResource("/").getPath();
        String videoPath = resourcePath + videoFileName;
        String outputDir = resourcePath + "screenshots";

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();
            int frameNumber = 0;
            long frameInterval = 3 * 1000000L;
            long nextFrameTime = 0;
            Frame frame;
            Java2DFrameConverter converter = new Java2DFrameConverter();
            while ((frame = grabber.grabImage()) != null) {
                long currentTimestamp = grabber.getTimestamp();
                if (currentTimestamp >= nextFrameTime) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    String outputFilePath = String.format("%s/frame_%04d.jpg", outputDir, frameNumber);
                    saveImage(bufferedImage, outputFilePath);
                    frameNumber++;
                    nextFrameTime += frameInterval; // 更新下一帧时间
                }
            }
            grabber.stop(); // 停止视频读取
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveImage(BufferedImage image, String outputPath) {
        try {
            ImageIO.write(image, "jpg", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package VideoStream;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Stream {

    private ScheduledExecutorService timer;

    private boolean cameraActive = false;

    // private static int cameraId = 0;

    private VideoCapture camera = new VideoCapture();

    public Stream(ImageView video_stream_view) {
        if (!cameraActive) {
            String videoIp = "http://192.168.1.2:8080/video?dummy=param.mjpg";
            // String videoIp = "rtsp://192.168.1.103:8080/h264_ulaw.sdp";
            camera.open(videoIp);

            if (camera.isOpened()) {
                cameraActive = true;

                Runnable frameGrabber = () -> {
                    Mat currentFrame = grabFrame();
                    Image imgToView = mat2Image(currentFrame);
                    updateImageView(video_stream_view, imgToView);
                };

                timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            } else {
                JOptionPane.showMessageDialog(null, "Can't open the camera", "Error", JOptionPane.ERROR_MESSAGE);
                // log the error
                // System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            cameraActive = false;

            // stop the timer
            setClosed();
        }
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (camera.isOpened()) {
            try {
                camera.read(frame);
            } catch (Exception e) {
                System.err.println("Error during frame elaboration: " + e);
            }
        }

        return frame;
    }

    private void updateImageView(ImageView view, Image image) {
        onFXThread(view.imageProperty(), image);
    }

    private static Image mat2Image(Mat frame) {
        try {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        }
        catch (Exception e) {
            System.err.println("Cannot convert the Mat obejct: " + e);
            return null;
        }
    }

    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);

        if (original.channels() > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        }
        else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }

        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);

        return image;
    }

    private static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(() -> property.set(value));
    }

    private void stopAcquisition() {
        if (timer != null && !timer.isShutdown()) {
            try {
                // stop the timer
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (camera.isOpened()) {
            // release the camera
            camera.release();
        }
    }

    private void setClosed() {
        stopAcquisition();
    }
}

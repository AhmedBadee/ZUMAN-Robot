package UserInterface;

import SSH.SSHApplication;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutonomousMode {

    @FXML
    private Button camera_btn;

    @FXML
    private Button connect_btn;

    @FXML
    private Button back_btn;

    @FXML
    private Button location_btn;

    @FXML
    private Label location_lbl;

    private ObservableList<String> locations = FXCollections.observableArrayList(
            "Lab 5"
    );

    @FXML
    private ComboBox<String> locations_box;

    @FXML
    private ImageView frame;

    private SSHApplication sshApplication = null;

    private ScheduledExecutorService timer;

    private boolean cameraActive = false;

    private static int cameraId = 0;
    private static String videoIp = "http://192.168.43.221:8080/video?dummy=param.mjpg";

    private VideoCapture camera = new VideoCapture();

    public void initialize() {
        locations_box.setItems(locations);
        this.sshApplication = new SSHApplication();
        this.sshApplication.executeCommand("roscore &");
    }

    public void startCamera(ActionEvent actionEvent) {
        if (!this.cameraActive) {
            this.camera.open(videoIp);

            if (this.camera.isOpened()) {
                this.cameraActive = true;

                Runnable frameGrabber = () -> {
                    Mat currentFrame = grabFrame();
                    Image imgToView = mat2Image(currentFrame);
                    updateImageView(frame, imgToView);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                camera_btn.setText("Stop Camera");
            } else {
                JOptionPane.showMessageDialog(null, "Can't open the camera", "Error", JOptionPane.ERROR_MESSAGE);
                // log the error
                // System.err.println("Impossible to open the camera connection...");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            camera_btn.setText("Start Camera");
            // stop the timer
            setClosed();
        }
    }

    public void startConnection(ActionEvent actionEvent) {
        String btn_txt = this.connect_btn.getText();

        if (btn_txt.contains("Start")) {
            if (this.sshApplication.isConnected()) {
                commands();
            } else {
                this.sshApplication = new SSHApplication();
                commands();
            }
        } else if (btn_txt.contains("Stop")) {
            this.sshApplication.exit();

            this.connect_btn.setText("Start Connection");
        }
    }

    public void getLocation(ActionEvent actionEvent) {

    }

    public void chooseDestination(ActionEvent actionEvent) {

    }

    public void sendDestination(ActionEvent actionEvent) {
        String destination = locations_box.getValue();

        if (destination.contains("5"))
            this.sshApplication.executeCommand("rostopic pub hw_map zuman_msgs/Instruction \"map_way\" " + "3 " + "5");

    }

    public void back(ActionEvent actionEvent) throws Exception {
        assert this.sshApplication != null;
        this.sshApplication.exit();

        Stage stage;
        Parent root;

        stage = (Stage) back_btn.getScene().getWindow();
        root  = FXMLLoader.load(getClass().getResource("../Layouts/mode.fxml"));

        Scene scene = new Scene(root, 1000, 600);

        stage.setTitle("ZUMAN");
        stage.setScene(scene);
        stage.show();

        Stage finalStage = stage;
        finalStage.setOnCloseRequest(event1 -> finalStage.close());
    }

    private void commands() {

        // Add commands here
        this.sshApplication.executeCommand("sudo chown grad /dev/ttyUSB0 /dev/ttyUSB1");
        this.sshApplication.executeCommand("123456789");
        this.sshApplication.executeCommand("rosrun rosserial_python serial_node.py /dev/ttyUSB0 __name:=\"ard1\" &");
        this.sshApplication.executeCommand("rosrun rosserial_python serial_node.py /dev/ttyUSB1 __name:=\"ard2\" &");
        this.sshApplication.executeCommand("rosrun zuman_hw HW_node &");
        this.sshApplication.executeCommand("rostopic pub hw_gui zuman_msgs/Instruction -1 -- \"end_manned\" 0 0");
        this.sshApplication.executeCommand("rosrun explore landmark &");
        this.sshApplication.executeCommand("rosrun explore mapping &");

        this.sshApplication.executeCommand("rostopic pub hw_landmark zuman_msgs/Instruction -1 -- \"detect\" 0 0");


        this.connect_btn.setText("Stop Connection");
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.camera.isOpened()) {
            try {
                this.camera.read(frame);
                /* if (!frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                } */
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
            System.err.println("Cannot convert the Mat object: " + e);
            return null;
        }
    }

    private static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
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
        Platform.runLater(() -> {
            property.set(value);
        });
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.camera.isOpened()) {
            // release the camera
            this.camera.release();
        }
    }

    private void setClosed() {
        this.stopAcquisition();
    }
}

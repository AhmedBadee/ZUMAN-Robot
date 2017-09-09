package UserInterface;

import SSH.SSHApplication;
import VideoStream.Stream;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class EEDMode{

    @FXML
    private Label slider_value;

    @FXML
    private Button move_btn;

    @FXML
    private Button rotate_btn;

    @FXML
    private Button light_switch;

    @FXML
    private Button back_btn;

    @FXML
    private Button send_command_btn;

    @FXML
    private Slider slider_yaw;

    @FXML
    private Slider slider_pitch;

    @FXML
    private TextField command_txt;

    @FXML
    private TextField move_txt;

    @FXML
    private TextField rotate_txt;

    @FXML
    private ImageView video_stream_view;

    private SSHApplication sshApplication;

    public void initialize() {
        sshApplication = new SSHApplication();

        if (sshApplication.isConnected())
            sshApplication.executeCommand("roscore &");

        new Stream(video_stream_view);
    }

    public void move() {
        String moveValue = move_txt.getText();
        sshApplication.executeCommand("");
    }

    public void rotate() {
        String rotateValue = rotate_txt.getText();
        sshApplication.executeCommand("");
    }

    public void light() {
        String light_state = light_switch.getText();

        if (light_state.contains("On")) {
            sshApplication.executeCommand("");
            light_switch.setText("Light Off");
        } else {
            sshApplication.executeCommand("");
            light_switch.setText("Light On");
        }
    }

    public void sliderValue() {
        slider_yaw.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                slider_value.setText(String.valueOf(newValue));
            }
        });
    }

    public void sendSliderValue() {
        String sliderValue = slider_value.getText();
        sshApplication.executeCommand("");
    }

    public void sendCommand() {

    }

    public void back() throws IOException {
        sshApplication.exit();

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
}

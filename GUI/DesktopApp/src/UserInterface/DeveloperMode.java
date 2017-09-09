package UserInterface;

import SSH.SSHApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DeveloperMode {

    @FXML
    private Button back_btn;

    @FXML
    private Button send_btn;

    @FXML
    private Button echo_btn;

    @FXML
    private TextField command_txt;

    @FXML
    private TextField topic_txt;

    @FXML
    private TextArea subbed_txt;

    private String output;

    private SSHApplication sshApplication = null;

    public void initialize() {
        this.sshApplication = new SSHApplication();

        /* this.sshApplication.executeCommand("sudo chown grad /dev/ttyUSB0 /dev/ttyUSB1");
        this.sshApplication.executeCommand("123456789");
        this.sshApplication.executeCommand("roscore &");
        this.sshApplication.executeCommand("rosrun rosserial_python serial_node.py /dev/ttyUSB0 __name:=\"ard1\" &");
        this.sshApplication.executeCommand("rosrun rosserial_python serial_node.py /dev/ttyUSB1 __name:=\"ard2\" &");
        this.sshApplication.executeCommand("rosrun rosserial_python serial_node.py tcp __name:=\"stick\" &"); */
    }

    public void sendCommand(ActionEvent actionEvent) {
        String command = command_txt.getText();

        String topic = topic_txt.getText();

        this.sshApplication.executeCommand(command);

        this.sshApplication.executeCommand("rostopic echo " + topic);
        output = this.sshApplication.readOutput("rostopic echo " + topic);
    }

    public void echoTopic(ActionEvent actionEvent) {

        subbed_txt.setText(output);
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
}

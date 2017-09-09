package SSH;

import javax.swing.*;

public class SSHApplication {

    private String host     = "local_ip";
    private int port        = 22;
    private String username = "username";
    private String password = "password";
    private int timeout     = 30000;

    private boolean connectionState = false;

    private SSHConnection sshConnection;

    public SSHApplication() {
        sshConnection = new SSHConnection();

        this.connectionState = this.sshConnection.openConnection(
                this.host,
                this.port,
                this.username,
                this.password,
                this.timeout
        );

        if (this.connectionState) {
            System.out.println("Connected Successfully \n");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(this.sshConnection.receiveData());
        } else {
            System.out.println("Can't connect \n");
            JOptionPane.showMessageDialog(null, "Can't Connect!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConnected() {
        return this.connectionState;
    }

    public void executeCommand(String command) {

        String output;

        this.sshConnection.sendCommand(command + "\n");
        System.out.println(command + "\n");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        output = this.sshConnection.replaceCommand(this.sshConnection.receiveData(), command);

        System.out.println(output);
    }

    public String readOutput(String command) {

        String output;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        output = this.sshConnection.replaceCommand(this.sshConnection.receiveData(), command);

        return output;
    }

    public void exit() {
        this.sshConnection.close();
    }
}

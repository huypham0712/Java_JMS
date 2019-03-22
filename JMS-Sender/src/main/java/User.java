import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class User extends Application {
    private static List<String> questions;
    private static UserService userService;

    @FXML
    public Button btnSend;

    @FXML
    public TextField tfMessage;

    @FXML
    public ListView lvMessage;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("userUI.fxml"));
        stage.setTitle("Sender");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        btnSend = new Button();
        tfMessage = new TextField();
        lvMessage = new ListView();
        userService = new UserService(lvMessage);
        userService.getAnswer("myFirstDestination");
    }

    public static void main(String[] args){
        launch(args);
    }

    public void onButtonSendClick(javafx.event.ActionEvent actionEvent) {
        String message = tfMessage.getText();

        if (message.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter your question!!");
            alert.show();
            return;
        }

        if (userService.sendQuestion(message, "mySecondDestination")) {
            tfMessage.clear();
        } else {
            handleServiceError("Service Error", "Could not send the message tot the service");
        }
    }

    public void handleServiceError(String errorTitle, String errorText){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(errorTitle);
        error.setContentText(errorText);
    }
}

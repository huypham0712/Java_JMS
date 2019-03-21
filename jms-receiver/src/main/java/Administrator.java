import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Administrator extends Application {
    private static Scanner s = new Scanner(System.in);
    private static Connection connection; // to connect to the ActiveMQ
    private static Session session; // session for creating messages, producers and
    private static Destination sendDestination; // reference to a queue/topic destination
    private static MessageProducer producer; // for sending messages
    private static Properties props;
    private static Context jndiContext;
    private static ConnectionFactory connectionFactory;
    private static ListProperty<String> listProperty;
    private static List<String> messageList;

    @FXML
    private TextField tfMessage;

    @FXML
    public ListView lvMessage;

    @FXML
    public Button btnSend;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("administratorUI.fxml"));
//        tfMessage = (TextField)root.lookup("#tfMessage");
//        lvMessage = (ListView)root.lookup("#lvMessage");
        stage.setTitle("Administrator");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        stage.show();

        lvMessage = new ListView();
        tfMessage = new TextField();
        messageList = new ArrayList<>();
        listProperty = new SimpleListProperty<>();
        lvMessage.itemsProperty().bind(listProperty);
        listProperty.set(FXCollections.observableArrayList(messageList));
    }

    public static void main(String[] args) {
        launch();
    }

    public void onButtonSendClick(ActionEvent actionEvent) {
        String message = tfMessage.getText();

        if (message.equals("")){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter your message!!");
            alert.show();
            return;
        }

        messageList.add(message);
        sendMessageToServer(message);
        lvMessage.getItems().add(message);
        tfMessage.clear();
    }


    private static void sendMessageToServer(String message){
        try {
            props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("topic.myFirstDestination"), "myFirstDestination");

            jndiContext = new InitialContext(props);
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the sender destination
            sendDestination = (Destination) jndiContext.lookup("myFirstDestination");
            producer = session.createProducer(sendDestination);

            // create a text message
            Message msg = session.createTextMessage(message);
            // send the message
            producer.send(msg);
            System.out.println("Message sent!!");
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}
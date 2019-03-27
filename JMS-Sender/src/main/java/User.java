import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.*;

public class User extends Application {
    private ObservableList<String> observableList = FXCollections.observableArrayList(new ArrayList<>());
    private final String DESTINATION_TYPE = "queue";
    private final String RECEIVE_CHANNEL = "answerDestination";
    private final String SEND_CHANNEL = "askDestination";
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    private Session session;
    private Destination destination;
    private Connection connection;
    private Gson gson;

    private Map<String, String> hashMap = new HashMap<>();
    private Button btnSend;
    private TextField tfMessage;
    private ListView<String> lvMessage;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("userUI.fxml"));
        stage.setTitle("Sender");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        gson = new Gson();
        btnSend = (Button) root.lookup("#btnSend");
        tfMessage = (TextField) root.lookup("#tfMessage");
        lvMessage = (ListView<String>) root.lookup("#lvMessage");
        initService(RECEIVE_CHANNEL, DESTINATION_TYPE);

        // For listening to the message from the server
        try {
            connection.start();

            // for receiving messages
            messageConsumer = session.createConsumer(destination);
            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage){
                            updateList(message);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    lvMessage.getItems().clear();
                                    for (String s : observableList) {
                                        lvMessage.getItems().add(s);
                                    }
                                }
                            });
                        }
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            };

            messageConsumer.setMessageListener(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // For sending message to the server
        btnSend.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String answer = tfMessage.getText();

                if (answer.equals("")) {
                    handleServiceError("Error", "Please enter your message OR Select the message to reply!!");
                    return;
                }

                if (askQuestion(answer, SEND_CHANNEL)){
                    tfMessage.clear();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            lvMessage.getItems().clear();
                            for (String s : observableList) {
                                lvMessage.getItems().add(s);
                            }
                        }
                    });
                } else {
                    handleServiceError("Error", "Could not send the message to the server!!");
                }
            }
        });



        stage.show();
    }

    private void updateList(Message message) throws JMSException {
        String key = null;

        for (Map.Entry entry : hashMap.entrySet()){
            if (entry.getValue().equals(message.getJMSCorrelationID())){
                key = (String) entry.getKey();
            }
        }

        for (int i = 0; i < observableList.size(); i++){
            if (observableList.get(i).equals(key)){
                observableList.set(i, key + " | Answer: " + ((TextMessage)message).getText());
            }
        }
    }

    public static void main(String[] args){
        launch(args);
    }

    private void handleServiceError(String errorTitle, String errorText){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(errorTitle);
        error.setContentText(errorText);
        error.show();
    }

    private void initService(String targetDestination, String destinationType){
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put((destinationType + "." + targetDestination), targetDestination);
            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            // to connect to the JMS
            connection = connectionFactory.createConnection();
            // session for creating consumers
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            //reference to a queue/topic destination
            destination = (Destination) jndiContext.lookup(targetDestination);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    private boolean askQuestion(String message, String sendDestination) {
        try {

            initService(sendDestination, DESTINATION_TYPE);

            // for sending messages
            messageProducer = session.createProducer(null);

            // create a text message
            Message msg = session.createTextMessage(message);

            msg.setJMSReplyTo(destination);

            // send the message
            messageProducer.send(msg.getJMSReplyTo(), msg);

            //questionList.add(message);
            observableList.add("Question: " + message);
            hashMap.put("Question: " + message, msg.getJMSMessageID());
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
    }
}

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

public class Administrator extends Application {
    private ObservableList<String> observableList = FXCollections.observableArrayList(new ArrayList<>());
    private final String DESTINATION_TYPE = "queue";
    private final String RECEIVE_CHANNEL = "askDestination";
    private final String SEND_CHANNEL = "answerDestination";
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    private Session session;
    private Destination destination;
    private Connection connection;


    private TextField tfMessage;

    private ListView<String> lvMessage;

    private Button btnSend;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("administratorUI.fxml"));
        stage.setTitle("Administrator");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        lvMessage = (ListView) root.lookup("#lvMessage");
        tfMessage = (TextField) root.lookup("#tfMessage");
        initService(RECEIVE_CHANNEL, DESTINATION_TYPE);
        try {
            connection.start();

            // for receiving messages
            messageConsumer = session.createConsumer(destination);
            MessageListener listener = new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        observableList.add("Server: " + ((TextMessage)message).getText());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                lvMessage.getItems().clear();
                                for (String s : observableList) {
                                    lvMessage.getItems().add(s);
                                }
                            }
                        });
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            };

            messageConsumer.setMessageListener(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        btnSend = (Button) root.lookup("#btnSend");
        btnSend.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String message = tfMessage.getText();

                if (message.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please enter your message!!");
                    alert.show();
                    return;
                }

                if (replyToQuestion(message, SEND_CHANNEL)){
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

    @Override
    public void init() throws Exception {
        super.init();
    }

    public static void main(String[] args) {
        launch();
    }

    private void handleServiceError(String errorTitle, String errorText) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(errorTitle);
        error.setContentText(errorText);
    }

    private void initService(String targetDestination, String destinationType) {
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

    private boolean replyToQuestion(String message, String sendDestination) {
        try {
            initService(sendDestination, DESTINATION_TYPE);

            // for sending messages
            messageProducer = session.createProducer(destination);

            // create a text message
            Message msg = session.createTextMessage(message);

            msg.setJMSCorrelationID(UUID.randomUUID().toString());
            System.out.println(msg.getJMSCorrelationID());

            // send the message
            messageProducer.send(msg);

            observableList.add("You: " + message);
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
    }
}
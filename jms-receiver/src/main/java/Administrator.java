import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Administrator extends Application implements MessageListener{
    private static List<String> questionList;
    private final String DESTINATION_TYPE = "queue";
    private final String RECEIVE_CHANNEL = "myFirstDestination";
    private final String SEND_CHANNEL = "myFirstDestination";
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    private Session session;
    private Destination destination;
    private Connection connection;

    @FXML
    public TextField tfMessage;

    @FXML
    public ListView<String> lvMessage;

    @FXML
    public Button btnSend;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("administratorUI.fxml"));
        stage.setTitle("Administrator");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        lvMessage = new ListView<>();
        tfMessage = new TextField();
        questionList = new ArrayList<>();
        //initService(RECEIVE_CHANNEL, DESTINATION_TYPE);
        getMessage(RECEIVE_CHANNEL);
        updateLV();
    }

    public static void main(String[] args) {
        launch();
    }

    public void onButtonAnswerClick() {
        String message = tfMessage.getText();

        if (message.equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please enter your message!!");
            alert.show();
            return;
        }

        if (replyToQuestion(message, SEND_CHANNEL)) {
            tfMessage.clear();
        } else {
            handleServiceError("Service Error", "Could not send the message tot the service");
        }
    }

    private void handleServiceError(String errorTitle, String errorText){
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(errorTitle);
        error.setContentText(errorText);
    }

    private void updateLV(){
        lvMessage.getItems().clear();
        lvMessage.getItems().addAll(questionList);
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

    private boolean replyToQuestion(String message, String sendDestination) {
        try {
            initService(sendDestination, DESTINATION_TYPE);

            // for sending messages
            messageProducer = session.createProducer(destination);

            // create a text message
            Message msg = session.createTextMessage(message);

            // send the message
            messageProducer.send(msg);

            //questionList.add(message);
            updateLV();
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void getMessage(String receiveDestination) {
        try {
            initService(receiveDestination, DESTINATION_TYPE);

            // for receiving messages
            messageConsumer = session.createConsumer(destination);
            messageConsumer.setMessageListener(this);

            // this is needed to start receiving messages
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            questionList.add(((TextMessage)message).getText());
            updateLV();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
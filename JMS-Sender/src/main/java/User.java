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
    private ObservableList<OrderInformation> observableList = FXCollections.observableArrayList(new ArrayList<>());
    private final String DESTINATION_TYPE = "queue";
    private final String RECEIVE_CHANNEL = "answerDestination" + UUID.randomUUID().toString();
    private final String SEND_CHANNEL = "askDestination";
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    private Session session;
    private Destination destination;
    private Connection connection;
    private Gson gson;
    private Context jndiContext;

    private Map<String, OrderInformation> hashMap = new HashMap<>();
    private Button btnSend;
    private TextField tfMessage;
    private ListView<String> lvMessage;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("userUI.fxml"));
        stage.setTitle("Sender");
        stage.setScene(new Scene(root, 1028, 429));
        stage.setResizable(true);
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
                            String messageDetail = ((TextMessage) message).getText();
                            OrderInformation newData = gson.fromJson(messageDetail, OrderInformation.class);
                            updateList(message, newData);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    lvMessage.getItems().clear();
                                    for (OrderInformation s : observableList) {
                                        lvMessage.getItems().add(s.getMessageDetail());
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
                            for (OrderInformation s : observableList) {
                                lvMessage.getItems().add(s.getMessageDetail());
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

    private void updateList(Message message, OrderInformation newData) throws JMSException {
        OrderInformation orderInformation = null;

        for (Map.Entry entry : hashMap.entrySet()){
            if (((OrderInformation)entry.getValue()).getCorrelationId().equals(message.getJMSCorrelationID())){
                orderInformation = (OrderInformation) entry.getValue();
            }
        }

        for (int i = 0; i < observableList.size(); i++){
            if (observableList.get(i).getCorrelationId().equals(orderInformation.getCorrelationId())){
                //String tmp = newData.getMessageDetail() + " | Answer: " + ((TextMessage)message).getText();
                orderInformation.setMessageDetail(newData.getMessageDetail());
                observableList.set(i, orderInformation);
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
            jndiContext = new InitialContext(props);
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
            OrderInformation orderInformation = new OrderInformation(message, RECEIVE_CHANNEL);
            orderInformation.setMessageDetail("Question: " + message);
            String toBeSent = gson.toJson(orderInformation, OrderInformation.class);
            Message msg = session.createTextMessage(toBeSent);

            destination = (Destination) jndiContext.lookup(SEND_CHANNEL);
            msg.setJMSReplyTo(destination);

            // send the message
            messageProducer.send(msg.getJMSReplyTo(), msg);

            //questionList.add(message);
            orderInformation.setCorrelationId(msg.getJMSMessageID());
            observableList.add(orderInformation);
            hashMap.put("Question: " + orderInformation.getMessageDetail(), orderInformation);
            return true;
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
            return false;
        }
    }
}

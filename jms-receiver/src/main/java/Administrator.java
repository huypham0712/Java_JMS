import com.google.gson.Gson;
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
import java.util.*;

public class Administrator extends Application {
    private ObservableList<OrderInformation> observableList = FXCollections.observableArrayList(new ArrayList<>());
    private final String DESTINATION_TYPE = "queue";
    private final String RECEIVE_CHANNEL = "askDestination";
    //private final String SEND_CHANNEL = "answerDestination";
    private MessageConsumer messageConsumer;
    private MessageProducer messageProducer;
    private Session session;
    private Destination destination;
    private Connection connection;
    private Gson gson;
    private Map<String, OrderInformation> hashMap = new HashMap<>();
    private TextField tfMessage;

    private ListView<String> lvMessage;

    private Button btnSend;

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("administratorUI.fxml"));
        stage.setTitle("Administrator");
        stage.setScene(new Scene(root, 640, 480));
        stage.setResizable(false);
        gson = new Gson();
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
                        if(message instanceof TextMessage){
                            String messageDetail = ((TextMessage) message).getText();
                            OrderInformation newData = gson.fromJson(messageDetail, OrderInformation.class);
                            transformDataToListView(newData, message);
                            observableList.add(newData);
                            hashMap.put(newData.getMessageDetail(), newData);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    lvMessage.getItems().clear();
                                    for (OrderInformation orderInformation : observableList) {
                                        lvMessage.getItems().add(orderInformation.getMessageDetail());
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

        btnSend = (Button) root.lookup("#btnSend");
        btnSend.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String message = tfMessage.getText();
                String question = lvMessage.getSelectionModel().getSelectedItem();

                if (message.equals("") || question == null || question.equals("")) {
                    handleServiceError("Error", "Please enter your message OR select the question you want to answer!!");
                    return;
                }

                if (!hashMap.containsKey(question) || question.contains("| Answer: ")){
                    handleServiceError("Error", "Could not answer to this question!!");
                    return;
                }

                OrderInformation order = null;
                for(Map.Entry entry : hashMap.entrySet()){
                    if (entry.getKey().equals(question)){
                        order = (OrderInformation) entry.getValue();
                    }
                }

                if (replyToQuestion(message, order)){
                    tfMessage.clear();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            lvMessage.getItems().clear();
                            for (OrderInformation orderInformation : observableList) {
                                lvMessage.getItems().add(orderInformation.getMessageDetail());
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

    private void transformDataToListView(OrderInformation newData, Message message) {
        try {
            // Set CorrelationId
            newData.setCorrelationId(message.getJMSMessageID());

            // Transform message
            String tmp = newData.getMessageDetail().contains("Question: ") ? newData.getMessageDetail() : "Question: " + newData.getMessageDetail();
            newData.setMessageDetail(tmp);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private void handleServiceError(String errorTitle, String errorText) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(errorTitle);
        error.setContentText(errorText);
        error.show();
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

    private boolean replyToQuestion(String message, OrderInformation orderInformation) {
        try {
            initService(orderInformation.getUserReplyDestination(), DESTINATION_TYPE);

            // for sending messages
            messageProducer = session.createProducer(null);

            // create a text message
            String answer = orderInformation.getMessageDetail() + " | Answer: " + message;
            orderInformation.setMessageDetail(answer);
            String jsonReply = gson.toJson(orderInformation);
            Message msg = session.createTextMessage(jsonReply);

            msg.setJMSCorrelationID(orderInformation.getCorrelationId());
            msg.setJMSReplyTo(destination);

            // send the message
            messageProducer.send(msg.getJMSReplyTo(), msg);

            updateList(orderInformation.getCorrelationId(), answer);
            return true;
        } catch (JMSException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateList(String correlationId, String answer) {
        OrderInformation orderInformation = null;

        for (Map.Entry entry : hashMap.entrySet()){
            if (((OrderInformation)entry.getValue()).getCorrelationId().equals(correlationId)){
                orderInformation = (OrderInformation) entry.getValue();
            }
        }

        for (int i = 0; i < observableList.size(); i++){
            if (observableList.get(i).getCorrelationId().equals(orderInformation.getCorrelationId())){
                //String tmp = orderInformation.getMessageDetail() + " | Answer: " + answer;
                orderInformation.setMessageDetail(answer);
                observableList.set(i, orderInformation);
            }
        }
    }
}
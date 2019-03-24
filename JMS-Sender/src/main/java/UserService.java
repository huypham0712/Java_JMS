//import javafx.scene.control.ListView;
//import javax.jms.*;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//public class UserService {
//    private List<String> questionList;
//    private MessageConsumer messageConsumer;
//    private MessageProducer messageProducer;
//    private Session session;
//    private Destination destination;
//    private Connection connection;
//    private ListView listView;
//
//    public List<String> getQuestionList() {
//        return questionList;
//    }
//
//    public UserService(ListView listView) {
//        this.questionList = new ArrayList<>();
//        this.listView = listView;
//    }
//
//    private void initService(String targetDestination, String destinationType){
//        try {
//            Properties props = new Properties();
//            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
//                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
//            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//
//            // connect to the Destination called “myFirstChannel”
//            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
//            props.put((destinationType + "." + targetDestination), targetDestination);
//            Context jndiContext = new InitialContext(props);
//            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
//            // to connect to the JMS
//            connection = connectionFactory.createConnection();
//            // session for creating consumers
//            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            // connect to the receiver destination
//            //reference to a queue/topic destination
//            destination = (Destination) jndiContext.lookup(targetDestination);
//        } catch (NamingException | JMSException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public boolean sendQuestion(String message, String sendDestination) {
//        try {
//            initService(sendDestination, "queue");
//
//            // for sending messages
//            messageProducer = session.createProducer(destination);
//
//            // create a text message
//            Message msg = session.createTextMessage(message);
//
//            // send the message
//            messageProducer.send(msg);
//
//            questionList.add(message);
//            updateListView();
//            return true;
//        } catch (JMSException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    public void getAnswer(String receiveDestination) {
//        try {
//            initService(receiveDestination, "queue");
//
//            // for receiving messages
//            messageConsumer = session.createConsumer(destination);
//
//            messageConsumer.setMessageListener(msg -> {
//                try {
//                    questionList.add(((TextMessage)msg).getText());
//                    updateListView();
//                } catch (JMSException e) {
//                    System.out.println(e.getMessage());
//                }
//            });
//
//            // this is needed to start receiving messages
//            connection.start();
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void updateListView(){
//        listView.getItems().clear();
//        listView.getItems().addAll(questionList);
//    }
//}

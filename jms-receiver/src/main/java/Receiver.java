import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class Receiver {
    private static Connection connection; // to connect to the JMS
    private static Session session; // session for creating consumers
    private static Destination receiveDestination; //reference to a queue/topic destination
    private static MessageConsumer consumer; // for receiving messages
    private static Properties props;
    private static Context jndiContext;
    private static ConnectionFactory connectionFactory;

    public static void main(String[] args) {
        getMessage();
    }

    private static void getMessage() {
        try {
            props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            // connect to the Destination called “myFirstChannel”
            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
            props.put(("topic.myFirstDestination"), " myFirstDestination");
            jndiContext = new InitialContext(props);
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // connect to the receiver destination
            receiveDestination = (Destination) jndiContext.lookup("myFirstDestination");

            consumer = session.createConsumer(receiveDestination);
            consumer.setMessageListener(msg -> {
                try {
                    System.out.println("received: " + ((TextMessage)msg).getText());
                } catch (JMSException e) {
                    System.out.println(e.getMessage());
                }
            });

            connection.start(); // this is needed to start receiving messages
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }
}

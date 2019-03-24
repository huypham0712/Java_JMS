//import javax.jms.*;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import java.util.Properties;
//
//public class Receiver {
//
//    public static void main(String[] args) {
//        getMessage();
//    }
//
//    private static void getMessage() {
//        try {
//            Properties props = new Properties();
//            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
//                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
//            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
//
//            // connect to the Destination called “myFirstChannel”
//            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
//            props.put(("queue.myFirstDestination"), " myFirstDestination");
//            Context jndiContext = new InitialContext(props);
//            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
//            // to connect to the JMS
//            Connection connection = connectionFactory.createConnection();
//            // session for creating consumers
//            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//
//            // connect to the receiver destination
//            //reference to a queue/topic destination
//            Destination receiveDestination = (Destination) jndiContext.lookup("myFirstDestination");
//
//            // for receiving messages
//            MessageConsumer consumer = session.createConsumer(receiveDestination);
//            consumer.setMessageListener(msg -> {
//                try {
//                    System.out.println("received: " + ((TextMessage)msg).getText());
//                } catch (JMSException e) {
//                    System.out.println(e.getMessage());
//                }
//            });
//
//            connection.start(); // this is needed to start receiving messages
//        } catch (NamingException | JMSException e) {
//            e.printStackTrace();
//        }
//    }
//}

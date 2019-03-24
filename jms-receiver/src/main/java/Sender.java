//import javax.jms.*;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import java.util.Properties;
//import java.util.Scanner;
//
//public class Sender {
//
//
//    public static void main(String[] args){
//        System.out.print("Enter your message: ");
////        String message = s.nextLine();
////        sendMessageToServer(message);
//    }
//
////    private static void sendMessageToServer(String message){
////        try {
////            props = new Properties();
////            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
////            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
////
////            // connect to the Destination called “myFirstChannel”
////            // queue or topic: “queue.myFirstDestination” or “topic.myFirstDestination”
////            props.put(("topic.myFirstDestination"), "myFirstDestination");
////
////            jndiContext = new InitialContext(props);
////            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
////            connection = connectionFactory.createConnection();
////            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
////
////            // connect to the sender destination
////            sendDestination = (Destination) jndiContext.lookup("myFirstDestination");
////            producer = session.createProducer(sendDestination);
////
////            // create a text message
////            Message msg = session.createTextMessage(message);
////            // send the message
////            producer.send(msg);
////            System.out.println("Message sent!!");
////        } catch (NamingException | JMSException e) {
////            e.printStackTrace();
////        }
////    }
//}

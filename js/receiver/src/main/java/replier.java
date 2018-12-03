import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;


public class replier {
    static Connection connection;// this is to connect to activeMQ
    static Session session;// session for creating messages, producers?
    static Destination receiveDestination; //reference to a queue/topic destination
    static Destination sendDestination; //reference to a queue/topic destination
    static MessageProducer producer; //for sending messages
    static MessageConsumer consumer; //for sending messages
    static ArrayList<Message> messages = new ArrayList<Message>();
    static ArrayList<Message> replied = new ArrayList<Message>();

    static ArrayList<Message> extendMessages = new ArrayList<Message>();
    static ArrayList<Message> extendReplied = new ArrayList<Message>();

    static ArrayList<Message> repairReplies = new ArrayList<Message>();
    static ArrayList<Message> extendReplies = new ArrayList<Message>();


    public static void main(String[] args) {
//        ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
//        Runnable runnable=()->{
//            replyMessage();
//        };
        receiveRepairRequest();
        receiveExtend();
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Menu : ");
            System.out.println("1. Reply Repair Request ");
            System.out.println("2. Reply Extend Request ");
            System.out.println("3. View All Repair Messages");
//            System.out.println("4. View Replied Repair");
//            System.out.println("5. View Pending Repair");
            System.out.println("4. View All extend Messages");
//            System.out.println("7. View Replied Extend");
//            System.out.println("8. View Pending Extend");
            System.out.println("5. Exit");

            System.out.print("Select : ");
            String select = sc.nextLine();
            if (Integer.parseInt(select) == 1) {
                replyRepair();
            } else if (Integer.parseInt(select) == 2) {
                replyExtend();
            } else if (Integer.parseInt(select) == 3) {
                viewReplyHistory();
                viewPendingRequest();
            } else if (Integer.parseInt(select) == 4) {
                viewReplyExtend();
                viewPendingExtend();
            } else if (Integer.parseInt(select) == 5) {
                break;
            }
//            } else if (Integer.parseInt(select) == 6) {
//                viewPendingRequest();
//            } else if (Integer.parseInt(select) == 7) {
//                viewReplyExtend();
//            } else if (Integer.parseInt(select) == 8) {
//                viewPendingExtend();
//            } else if (Integer.parseInt(select) == 9) {
//                break;
//            }

        }
    }

    public static void receiveRepairRequest() {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the destination
            //you can pick queue or topic
            props.put(("queue.RepairRequest"), "RepairRequest");
            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //connect to the receiver destination it says
            receiveDestination = (Destination) jndiContext.lookup("RepairRequest");
            consumer = session.createConsumer(receiveDestination);
            consumer.setMessageListener(message -> {
//                try {
                messages.add(message);
//                    System.out.println("Received :" + ((TextMessage) message).getText());
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
            });
            connection.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }

    }

    public static void receiveExtend() {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the destination
            //you can pick queue or topic
            props.put(("queue.extendRequest"), "extendRequest");
            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //connect to the receiver destination it says
            receiveDestination = (Destination) jndiContext.lookup("extendRequest");
            consumer = session.createConsumer(receiveDestination);
            consumer.setMessageListener(message -> {
//                try {
                extendMessages.add(message);
//                    System.out.println("Received :" + ((TextMessage) message).getText());
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
            });
            connection.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }

    }

    public static void replyRepair() {

        if (viewPendingRequest()) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Messages size is :" + messages.size());

            System.out.println("Which one to reply?");
            String select = sc.nextLine();
            Message request = messages.get(Integer.parseInt(select) - 1);
            replied.add(request);
            messages.remove(Integer.parseInt(select) - 1);
            System.out.println("Your Message : ");
            String replyMessage = sc.nextLine();
            sendMessage(request, replyMessage);
        }
    }

    public static void replyExtend() {
        if (viewPendingExtend()) {
            Scanner sc = new Scanner(System.in);
//            System.out.println("Messages size is :" + extendMessages.size());
            System.out.println("Which one to reply?");
            String select = sc.nextLine();
            Message request = extendMessages.get(Integer.parseInt(select) - 1);
            extendReplied.add(request);
            extendMessages.remove(Integer.parseInt(select) - 1);

            System.out.println("Your Message : ");
            String replyMessage = sc.nextLine();
            sendMessage(request, replyMessage);
        }
    }

    public static void sendMessage(Message request, String replyMessage) {
        String requestId = "";
        try {
            requestId = request.getJMSMessageID();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            producer = session.createProducer(null);
            sendDestination = request.getJMSReplyTo();

            Message reply = session.createTextMessage(replyMessage);
            reply.setJMSCorrelationID(requestId);
            System.out.print(reply);
            producer.send(sendDestination, reply);

        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    public static void viewReplyHistory() {

        if (replied.size() > 0) {
            System.out.println("Replied Messages: ");
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < replied.size(); i++) {
                try {
//                    System.out.println((i + 1) + ". " + ((TextMessage) replied.get(i)).getText() + " ->  replied");
                    String repairJson = ((TextMessage) replied.get(i)).getText();
                    repairRequest repairObj = objectMapper.readValue(repairJson, repairRequest.class);
                    System.out.println((i + 1) + " " + repairObj.toString());
                } catch (JMSException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void viewReplyExtend() {

        if (extendReplied.size() > 0) {
            System.out.println("Extend Messages: ");
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < extendReplied.size(); i++) {
                try {
//                    System.out.println((i + 1) + ". " + ((TextMessage) replied.get(i)).getText() + " ->  replied");
                    String repairJson = ((TextMessage) extendReplied.get(i)).getText();
                    extendRequest extendObj = objectMapper.readValue(repairJson, extendRequest.class);
                    System.out.println((i + 1) + " " + extendObj.toString());
                } catch (JMSException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean viewPendingRequest() {
        if (messages.size() > 0) {
            System.out.println("Pending messages:");
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < messages.size(); i++) {
                try {
//                    System.out.println((i + 1) + ". " + ((TextMessage) replied.get(i)).getText() + " ->  replied");
                    String repairJson = ((TextMessage) messages.get(i)).getText();
                    repairRequest repairObj = objectMapper.readValue(repairJson, repairRequest.class);
                    System.out.println((i + 1) + " " + repairObj.toString());
                } catch (JMSException | IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            System.out.println("There is no request yet");
            return false;
        }
    }

    public static boolean viewPendingExtend() {
        if (extendMessages.size() > 0) {
            System.out.println("Pending messages:");
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < extendMessages.size(); i++) {
                try {
//                    System.out.println((i + 1) + ". " + ((TextMessage) replied.get(i)).getText() + " ->  replied");
                    String repairJson = ((TextMessage) extendMessages.get(i)).getText();
                    extendRequest extendObj = objectMapper.readValue(repairJson, extendRequest.class);
                    System.out.println((i + 1) + " " + extendObj.toString());
                } catch (JMSException | IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            System.out.println("There is no extend request yet");
            return false;
        }
    }

}


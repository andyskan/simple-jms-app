import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;


public class requestor {
    static Connection connection;// this is to connect to activeMQ
    static Session session;// session for creating messages, producers?
    static Destination receiveReplyDest; //reference to a queue/topic destination
    static Destination sendRequestDestination; //reference to a queue/topic destination
    static Destination receiverDest; //reference to a queue/topic destination
    static MessageProducer producer; //for sending messages
    static MessageConsumer consumer; //for sending messages
    static String requestorQueueName;
    static String extendQueueName;

    static ArrayList<Message> request = new ArrayList<Message>();
    static ArrayList<Message> replies = new ArrayList<Message>();


    static ArrayList<Message> extendRequest = new ArrayList<Message>();
    static ArrayList<Message> extendReplies = new ArrayList<Message>();

    public static void main(String[] args) {
        Random requestorId = new Random();
        int id = requestorId.nextInt();
        if (id < 0) id *= -1;
        requestorQueueName = "repairRequestor" + id;
        extendQueueName = "extendRequestor" + id;
        receiveExtend();
        receiveReply();
        mainMenu();
    }

    public static void mainMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome! please fill this data : ");
        System.out.println("Your Name : ");
        String name = sc.nextLine();
        System.out.println("Your Age : ");
        int age = Integer.parseInt(sc.nextLine());
        System.out.println("Your Address : ");
        String address = sc.nextLine();
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            System.out.println("Menu : ");
            System.out.println("1. Send Repair Request ");
            System.out.println("2. Send Room Extend Request ");
            System.out.println("3. View Repair Replies");
            System.out.println("4. View Extend Replies");
            System.out.println("5. Quit ");
            System.out.print("Select : ");
            String select = sc.nextLine();
            if (Integer.parseInt(select) == 1) {
                repairRequest permohonan = new repairRequest();
                System.out.println("Input your Message :");
                String message = sc.nextLine();
                permohonan.setName(name);
                permohonan.setAddress(address);
                permohonan.setProblem(message);
//                sendRepairRequest(permohonan);
                String body = "";
                try {
                    body = objectMapper.writeValueAsString(permohonan);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
//                sendExtendRequest(permohonan);
                sendRequest("RepairRequest", body, requestorQueueName,request);


            } else if (Integer.parseInt(select) == 2) {
                extendRequest permohonan = new extendRequest();
                System.out.println("When does your study end? ");
                String message = sc.nextLine();
                permohonan.setSenderName(name);
                permohonan.setAddress(address);
                permohonan.setStudentYearEnd(message);
                String body = "";
                try {
                    body = objectMapper.writeValueAsString(permohonan);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
//                sendExtendRequest(permohonan);
                sendRequest("extendRequest", body, extendQueueName,extendRequest);

            } else if (Integer.parseInt(select) == 3) {
                printAllRepair();
            } else if (Integer.parseInt(select) == 4) {
//                printAllRepair(extendRequest, extendReplies);
                printAllExtend();
            } else if (Integer.parseInt(select) == 5) {
                break;
            } else System.out.println("Pick another menu");
        }
    }


    public static void sendRequest(String queueDestination, String body, String queueReceive, ArrayList<Message> requestList) {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the destination
            //you can pick queue or topic
            //set destination
            props.put(("queue." + queueDestination), queueDestination);
            props.put(("queue." + queueReceive), queueReceive);
            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");

            ConnectionFactory receiveConnectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            connection = receiveConnectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            //this is to connect to sender destination
            sendRequestDestination = (Destination) jndiContext.lookup(queueDestination);
            receiverDest = (Destination) jndiContext.lookup(queueReceive);
            producer = session.createProducer(sendRequestDestination);
            Message requestMessage = session.createTextMessage(body);
            System.out.print(requestMessage);
            requestMessage.setJMSReplyTo(receiverDest);
            requestList.add(requestMessage);
            producer.send(requestMessage);

        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }

    }

    public static void receiveReply() {

        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
            // connect to the destination
            //you can pick queue or topic
            props.put(("queue." + requestorQueueName), requestorQueueName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //connect to the receiver destination it says
            receiveReplyDest = (Destination) jndiContext.lookup(requestorQueueName);
            consumer = session.createConsumer(receiveReplyDest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    replies.add(message);
                }
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
            props.put(("queue." + extendQueueName), extendQueueName);

            Context jndiContext = new InitialContext(props);
            ConnectionFactory connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");

            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //connect to the receiver destination it says
            receiveReplyDest = (Destination) jndiContext.lookup(extendQueueName);
            consumer = session.createConsumer(receiveReplyDest);
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    extendReplies.add(message);
                }
            });
            connection.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }


    public static void printAllRepair() {
        if (replies.size() > 0) {
            for (int i = 0; i < replies.size(); i++) {
                try {
                    for (int j = 0; j < request.size(); j++) {
                        if (request.get(j).getJMSMessageID().equals(replies.get(i).getJMSCorrelationID())) {
                            System.out.println("Request : " + ((TextMessage) request.get(j)).getText());
                            System.out.println("Reply : " + ((TextMessage) replies.get(i)).getText());
                        }
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        } else System.out.println("No Replies yet");
    }
    public static void printAllExtend() {
        if (extendReplies.size() > 0) {
            for (int i = 0; i < extendReplies.size(); i++) {
                try {
                    for (int j = 0; j < extendRequest.size(); j++) {
                        if (extendRequest.get(j).getJMSMessageID().equals(extendReplies.get(i).getJMSCorrelationID())) {
                            System.out.println("Request : " + ((TextMessage) extendRequest.get(j)).getText());
                            System.out.println("Reply : " + ((TextMessage) extendReplies.get(i)).getText());
                        }
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        } else System.out.println("No Replies yet");
    }

}

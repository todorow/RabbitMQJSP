import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class BusClient extends Thread {
    private final static String EXCHANGE_NAME = "topic_bus";
    private String busNumber;
    private int temperature;
    private double humidity;
    private Location location;

    public BusClient(String busNumber) {
        this.busNumber = busNumber;
    }

    public BusClient(String busNumber, int temperature, double humidity, Location location) {
        this.busNumber = busNumber;
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {

        BusClient client = new BusClient ("2", 23, 0.2, new Location (413123131231L, 31231231L));
        client.start ();
        BusClient client2 = new BusClient ("24", 23, 0.2, new Location (413123131231L, 31231231L));
        client2.start ();
    }

    public void generateNewNumbers() {
        ++location.lognitude;
        ++location.lagnitude;
        ++humidity;
        --temperature;

    }

    @Override
    public String toString() {
        return "BusClient{" +
                "busNumber='" + busNumber + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", location=" + location +
                '}';
    }

    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory ();
        Connection connection = null;
        try {
            connection = factory.newConnection ();
        } catch (IOException e) {
            e.printStackTrace ();
        } catch (TimeoutException e) {
            e.printStackTrace ();
        }
        Channel channel = null;
        try {
            channel = connection.createChannel ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        try {
            channel.exchangeDeclare (EXCHANGE_NAME, "topic");
        } catch (IOException e) {
            e.printStackTrace ();
        }
        String routingKey = busNumber;
        String queueName = null;
        try {
            queueName = channel.queueDeclare ().getQueue ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        try {
            channel.queueBind (queueName, EXCHANGE_NAME, routingKey);
        } catch (IOException e) {
            e.printStackTrace ();
        }
        System.out.println ("[x] i will send data from the bus for every 5 seconds ");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String (delivery.getBody (), StandardCharsets.UTF_8);
            System.out.println ("[x] Data successfully received form CentralServer " +
                    delivery.getEnvelope ().getRoutingKey () + "':'" + message + "'");

        };


        while (true) {
            try {
                channel.basicPublish (EXCHANGE_NAME, routingKey, null, this.toString ().getBytes ("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace ();
            }
            try {
                Thread.sleep (5000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            generateNewNumbers ();
        }
    }
}

class Location {
    Long lagnitude;
    Long lognitude;

    public Location(Long lagnitude, Long lognitude) {
        this.lagnitude = lagnitude;
        this.lognitude = lognitude;
    }

    @Override
    public String toString() {
        return "Location{" +
                "lagnitude=" + lagnitude +
                ", lognitude=" + lognitude +
                '}';
    }
}

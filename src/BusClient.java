import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("WeakerAccess")
public class BusClient extends Thread {
    private final static String EXCHANGE_NAME = "topic_bus";
    private final String busNumber;
    private int temperature;
    private double humidity;
    private Location location;

    public BusClient(String busNumber, int temperature, double humidity, Location location) {
        this.busNumber = busNumber;
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
    }

    public static void main(String[] args) {

        BusClient client = new BusClient ("2", 23, 0.2, new Location (413123131231L, 31231231L));
        client.start ();
        BusClient client2 = new BusClient ("24", 23, 0.2, new Location (413123131231L, 31231231L));
        client2.start ();
    }

    private void generateNewNumbers() {
        location.update ();
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
        Connection connection;
        Channel channel = null;
        String routingKey = busNumber;
        String queueName;
        try {
            connection = factory.newConnection ();
            channel = connection.createChannel ();
            channel.exchangeDeclare (EXCHANGE_NAME, "topic");
            queueName = channel.queueDeclare ().getQueue ();
            channel.queueBind (queueName, EXCHANGE_NAME, routingKey);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace ();
        }
        System.out.println ("[x] i will send data from the bus for every 5 seconds ");
        System.out.println ("[x] for shutting down the client use CTRL+C");

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                assert channel != null;
                channel.basicPublish (EXCHANGE_NAME, routingKey, null, this.toString ().getBytes (StandardCharsets.UTF_8));
                Thread.sleep (5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace ();
            }

            generateNewNumbers ();
        }
    }
}



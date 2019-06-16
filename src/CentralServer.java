import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;

public class CentralServer {
    private final static String EXCHANGE_NAME = "topic_bus";
    private final static String[] BUS_NUM = {"2", "24", "4"};
    private static LocalTime time;
    private static int num = 0;
    private static String name = "output0.txt";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory ();
        Connection connection = factory.newConnection ();
        Channel channel = connection.createChannel ();
        channel.exchangeDeclare (EXCHANGE_NAME, "topic");

        String queueName = channel.queueDeclare ().getQueue ();
        for (String bindingKey : BUS_NUM) {
            channel.queueBind (queueName, EXCHANGE_NAME, bindingKey);
        }


        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String message = new String (delivery.getBody (), StandardCharsets.UTF_8);

            if (ChronoUnit.MILLIS.between (time, LocalTime.now ()) >= 3600000) {
                time=LocalTime.now ();
                num++;
                name = "output" + num + ".txt";
            }
            System.out.println ("[x] Data successfully received form BUS CLIENT " +
                    delivery.getEnvelope ().getRoutingKey () + "':'" + message + "'");
            Writer outputStream = new FileWriter (new File (name), true);
            outputStream.write (message.concat ("\n"));
            outputStream.flush ();
            outputStream.close ();
        };
        time = LocalTime.now ();
        channel.basicConsume (queueName, true, EXCHANGE_NAME, deliverCallback, consumerTag -> {
        });


    }
}



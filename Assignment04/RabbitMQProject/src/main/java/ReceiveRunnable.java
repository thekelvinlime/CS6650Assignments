import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class ReceiveRunnable implements Runnable{
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String QUEUE_NAME = "ReviewQ";
    private final Connection connection;

    public ReceiveRunnable(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicQos(1);
            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
                System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message + "'");
            };
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQConnection {
    private static Connection connection;

    public static Connection createConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
//        factory.setUsername(username);
//        factory.setPassword(password);
//        factory.setVirtualHost(virtualHost);
            factory.setHost("localhost");
//        factory.setPort(portNumber);
            connection = factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }
}

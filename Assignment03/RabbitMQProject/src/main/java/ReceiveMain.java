import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class ReceiveMain {
    public static void main(String[] args) throws Exception {
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);

        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
        Thread[] threads = new Thread[numThreadGroups * threadGroupSize];
        for (int i = 0; i < numThreadGroups; i++) {
            threadGroups[i] = new ThreadGroup("Group" + i);
            for (int j = 0; j < threadGroupSize; j++) {
                ReceiveRunnable receiveRunnable = new ReceiveRunnable(connection);
                threads[i * threadGroupSize + j] = new Thread(threadGroups[i], receiveRunnable);
                threads[i * threadGroupSize + j].start();
            }
            Thread.sleep(delay);
        }
        waitForAllThreads(threads);
    }

    public static void waitForAllThreads(Thread[] threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

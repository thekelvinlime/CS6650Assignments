
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static AtomicInteger success, failure;
    private static ArrayList<Long> getLatencies;
    private static ArrayList<Long> postLatencies;

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);

        String urlJava = "http://34.216.137.204:8080/AlbumServlet_Web";
//        String urlJava = "http://52.11.56.34:8080/AlbumServlet_Web";
//        String urlJava = "http://ApplicationLoadBalancer-1040454443.us-west-2.elb.amazonaws.com/AlbumServlet_Web";
//        String urlJava = "http://localhost:8080/AlbumServlet_Web";

        success = new AtomicInteger(0);
        failure = new AtomicInteger(0);
        getLatencies = new ArrayList<>();
        postLatencies = new ArrayList<>();
        long totalNumberOfRequests;
        long wallTime;
        double throughput;

        long start = System.nanoTime();
        ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
        Thread[] threads = new Thread[numThreadGroups * threadGroupSize];

        for (int i = 0; i < numThreadGroups; i++) {
            threadGroups[i] = new ThreadGroup("Group" + i);
            for (int j = 0; j < threadGroupSize; j++) {
                RunningThread rt = new RunningThread(urlJava, success, failure, getLatencies, postLatencies);
                threads[i * threadGroupSize + j] = new Thread(threadGroups[i], rt);
                threads[i * threadGroupSize + j].start();
            }
            Thread.sleep(delay);
        }
        waitForAllThreads(threads);

        long finish = System.nanoTime();
        totalNumberOfRequests = success.get() + failure.get();
        wallTime = (finish - start) / 1000000000;
        throughput = (double) totalNumberOfRequests/wallTime;
        Collections.sort(getLatencies);

//        printResponseTimeCalculations(getLatencies, "GET");
//        printResponseTimeCalculations(postLatencies, "POST");
//        System.out.println("Successful: " + success.get());
//        System.out.println("Failed: " + failure.get());
        System.out.println("Total Requests: " + totalNumberOfRequests + " requests");
        System.out.println("Wall Time: " + wallTime + " s");
        System.out.println("Throughput: " + throughput + " requests/sec");
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

    public static void printResponseTimeCalculations(ArrayList<Long> list, String requestType) {
        OptionalDouble mean = list.stream().mapToDouble(a->a).average();
        int middle = list.size() / 2;
        double median = (double) list.get(middle) / 1000000000;
        double minimum = Double.MAX_VALUE;
        double maximum = Double.MIN_VALUE;
        int index = (int) Math.ceil(99/100.0 * list.size()) -1;
        double percentile99 = (double) list.get(index) / 1000000000;
        for (long latency : list) {
            minimum = Math.min(minimum, (double) latency /1000000000);
            maximum = Math.max(maximum, (double) latency /1000000000);
        }
        System.out.println(requestType + " Response Times: ");
        System.out.println("Mean: " + mean + " nanoseconds");
        System.out.println("Median: " + median + " s");
        System.out.println("99th Percentile: " + percentile99 + " s");
        System.out.println("Minimum: " + minimum + " s");
        System.out.println("Maximum: " + maximum + " s");
        System.out.println();
    }

}

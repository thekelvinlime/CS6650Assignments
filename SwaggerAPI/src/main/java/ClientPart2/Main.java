package ClientPart2;

import ClientPart2.RunningThread;
import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.DefaultApi;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static AtomicInteger success, failure;
    private static ArrayList<Long> getLatencies;
    private static ArrayList<Long> postLatencies;

    public static void waitForAllThreads(Thread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printResponseTimeCalculations(ArrayList<Long> list, String requestType) {
        OptionalDouble mean = list.stream().mapToDouble(a->a).average();
        int middle = list.size() / 2;
        long median = list.get(middle);
        long minimum = Long.MAX_VALUE;
        long maximum = Long.MIN_VALUE;
        int index = (int) Math.ceil(99/100.0 * list.size()) -1;
        long percentile99 = list.get(index);
        for (long latency : list) {
            minimum = Math.min(minimum,latency);
            maximum = Math.max(maximum,latency);
        }
        System.out.println(requestType + " Response Times: ");
        System.out.println("Mean: " + mean);
        System.out.println("Median: " + median);
        System.out.println("99th Percentile: " + percentile99);
        System.out.println("Minimum: " + minimum);
        System.out.println("Maximum: " + maximum);
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        int threadGroupSize = 10;
        int numThreadGroups = 30;
        int delay = 2000;

//        String urlGo = "http://localhost:8082";
        String urlGo = "http://34.219.6.97:8082";
//        String urlJava = "http://localhost:8080/AlbumApp_Web";
        String urlJava = "http://34.219.6.97:8080/AlbumApp_Web";

        String csv = "C:\\Users\\theke\\OneDrive\\Desktop\\CS6650_Scalable_Distributed_Systems\\Assignment01\\data.csv";


        getLatencies = new ArrayList<>();
        postLatencies = new ArrayList<>();
        success = new AtomicInteger(0);
        failure = new AtomicInteger(0);
        long totalNumberOfRequests = 10;
        long wallTime = 0;
        double throughput = 0;

        long start = System.nanoTime();
        ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
        Thread[] threads = new Thread[numThreadGroups * threadGroupSize];
        for (int i = 0; i < numThreadGroups; i++) {
            threadGroups[i] = new ThreadGroup("Group" + i);
            for (int j = 0; j < threadGroupSize; j++) {
                RunningThread rt = new RunningThread(urlJava, csv, getLatencies, postLatencies, success, failure);
//                RunningThread rt = new RunningThread(urlJava, success, failure);
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
        System.out.println(getLatencies.size());

        Collections.sort(getLatencies);
        printResponseTimeCalculations(getLatencies, "GET");
//        printResponseTimeCalculations(postLatencies, "POST");

        System.out.println("Successful: " + success.get());
        System.out.println("Failed: " + failure.get());
        System.out.println("Total Requests: " + totalNumberOfRequests + " requests");
        System.out.println("Wall Time: " + wallTime + " s");
        System.out.println("Throughput: " + throughput + " requests/sec");

    }

}

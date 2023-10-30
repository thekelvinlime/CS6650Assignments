package ClientPart1;

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

    public static void waitForAllThreads(Thread[] threads) {
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        int threadGroupSize = 10;
        int numThreadGroups = 10;
        int delay = 2000;

//        String urlGo = "http://localhost:8082";
        String urlGo = "http://54.202.149.227:8082";
        String urlJava = "http://54.202.149.227:8080/AlbumApp_Web";
//        String urlJava = "http://localhost:8080/AlbumApp_Web";


        success = new AtomicInteger(0);
        failure = new AtomicInteger(0);
        long totalNumberOfRequests = 0;
        long wallTime = 0;
        double throughput = 0;

        long start = System.nanoTime();
        ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
        Thread[] threads = new Thread[numThreadGroups * threadGroupSize];
        for (int i = 0; i < numThreadGroups; i++) {
            threadGroups[i] = new ThreadGroup("Group" + i);
            for (int j = 0; j < threadGroupSize; j++) {
                RunningThread rt = new RunningThread(urlJava, success, failure);
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

        System.out.println("Successful: " + success.get());
        System.out.println("Failed: " + failure.get());
        System.out.println("Total Requests: " + totalNumberOfRequests + " requests");
        System.out.println("Wall Time: " + wallTime + " s");
        System.out.println("Throughput: " + throughput + " requests/sec");

    }

}


import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static AtomicInteger success, failure;

    public static void main(String[] args) throws InterruptedException {
        final int threadGroupSize = 10;
        int numThreadGroups = 10;

//        String urlJava = "http://35.93.156.177:8080/AlbumServlet_Web";
        String urlJava = "http://ApplicationLoadBalancer-1040454443.us-west-2.elb.amazonaws.com/AlbumServlet_Web";
//        String urlJava = "http://localhost:8080/AlbumServlet_Web";

        success = new AtomicInteger(0);
        failure = new AtomicInteger(0);
        long totalNumberOfRequests;
        long wallTime;
        double throughput;

        long start = System.nanoTime();
        ThreadGroup[] threadGroups = new ThreadGroup[numThreadGroups];
        Thread[] threads = new Thread[numThreadGroups * threadGroupSize];
        int delay = 2000;
        for (int i = 0; i < numThreadGroups; i++) {
            threadGroups[i] = new ThreadGroup("Group" + i);
            for (int j = 0; j < threadGroupSize; j++) {
                RunningThread rt = new RunningThread(urlJava, success, failure);
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

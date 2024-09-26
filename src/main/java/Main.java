import RateLimiter.*;

import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new FixedWindowCounterRateLimiter(1,2);
        int t = 100;
        while(t-- > 0) {
            System.out.println(rateLimiter.allowRequest());
//            System.out.println(System.nanoTime()/ TimeUnit.SECONDS.toNanos(1));
            Thread.sleep(100);
        }

    }
}

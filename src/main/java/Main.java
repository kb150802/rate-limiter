import RateLimiter.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new SlidingWindowLogRateLimiter(1,5);
        int t = 100;
        Map<Long,Integer> count = new HashMap<>();
        while(t-- > 0) {
            Long x = System.nanoTime()/ TimeUnit.SECONDS.toNanos(1);
            if(rateLimiter.allowRequest()) {
                count.put(x, count.getOrDefault(x, 0) + 1);
            }
            Thread.sleep(10);
        }
        count.forEach((k,v)->{
            System.out.println(k + " " + v);
        });

    }
}

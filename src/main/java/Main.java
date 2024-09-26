import RateLimiter.RateLimiter;
import RateLimiter.LeakyBucketRateLimiter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new LeakyBucketRateLimiter(5,1);
        int t = 100;
        while(t-- > 0) {
            System.out.println(rateLimiter.allowRequest());
            Thread.sleep(100);
        }

    }
}

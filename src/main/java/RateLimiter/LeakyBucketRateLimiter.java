package RateLimiter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class LeakyBucketRateLimiter implements RateLimiter{
    private final Integer capacity;
    private final Integer leakRate;
    private final Queue<Long> requestQueue;
    private final AtomicLong lastLeakTime;
    private final AtomicLong currentQueueSize;
    private final Supplier<Long> timeSource;
    public LeakyBucketRateLimiter(Integer capacity, Integer leakRate) {
        this.timeSource = System::nanoTime;
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.requestQueue = new ConcurrentLinkedDeque<>();
        this.lastLeakTime = new AtomicLong(System.nanoTime());
        this.currentQueueSize = new AtomicLong(0);
    }
    public LeakyBucketRateLimiter(Integer capacity, Integer leakRate, Supplier<Long> timeSource) {
        this.timeSource = timeSource;
        this.capacity = capacity;
        this.leakRate = leakRate;
        this.requestQueue = new ConcurrentLinkedDeque<>();
        this.lastLeakTime = new AtomicLong(System.nanoTime());
        this.currentQueueSize = new AtomicLong(0);
    }
    @Override
    public boolean allowRequest() {
        performLeakage();
        if(currentQueueSize.get() < capacity) {
            if(currentQueueSize.incrementAndGet() <= capacity) {
                requestQueue.offer(timeSource.get());
                return true;
            }
            currentQueueSize.decrementAndGet();
        }
        return false;
    }
    private void performLeakage() {
        long currentTime = timeSource.get();
        long timeSinceLastLeak = currentTime - lastLeakTime.get();
        long numberOfRequestsToLeak = (timeSinceLastLeak * leakRate) / TimeUnit.SECONDS.toNanos(1);
        numberOfRequestsToLeak = Math.min(numberOfRequestsToLeak, currentQueueSize.get());
        if(numberOfRequestsToLeak > 0) {
            lastLeakTime.set(currentTime);
        }
        while(numberOfRequestsToLeak-->0) {
            requestQueue.poll();
            currentQueueSize.decrementAndGet();
        }
    }
}

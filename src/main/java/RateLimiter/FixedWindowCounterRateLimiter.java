package RateLimiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class FixedWindowCounterRateLimiter implements RateLimiter{
    private final Integer windowSize;
    private final Integer maxRequests;
    private final AtomicLong requestCount;
    private final AtomicLong currentWindow;
    private final Supplier<Long> timeSource;
    public FixedWindowCounterRateLimiter(Integer windowSize, Integer maxRequests){
        this.timeSource = System::nanoTime;
        this.windowSize = windowSize;
        this.maxRequests = maxRequests;
        Long currentTime = timeSource.get() / TimeUnit.SECONDS.toNanos(1);
        this.currentWindow = new AtomicLong(currentTime / windowSize);
        requestCount = new AtomicLong(0);
    }
    public FixedWindowCounterRateLimiter(Integer windowSize, Integer maxRequests, Supplier<Long> timeSource){
        this.timeSource = timeSource;
        this.windowSize = windowSize;
        this.maxRequests = maxRequests;
        Long currentTime = timeSource.get() / TimeUnit.SECONDS.toNanos(1);
        this.currentWindow = new AtomicLong(currentTime / windowSize);
        requestCount = new AtomicLong(0);
    }

    @Override
     public boolean allowRequest() {
        processWindow();
        if(requestCount.get() < maxRequests) {
            if(requestCount.incrementAndGet() <= maxRequests) {
                return true;
            }
            requestCount.decrementAndGet();
        }
        return false;
    }
     private void processWindow(){
        Long currentTime = timeSource.get() / TimeUnit.SECONDS.toNanos(1);
        Long window = currentTime / windowSize;
        synchronized (this) {
            if(!window.equals(currentWindow.get())) {
                currentWindow.set(window);
                requestCount.set(0);
            }
        }
    }
}

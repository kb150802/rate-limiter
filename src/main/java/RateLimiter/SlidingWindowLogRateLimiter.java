package RateLimiter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class SlidingWindowLogRateLimiter implements RateLimiter{

    private final Long windowSize;
    private final Integer maxRequests;
    private final Queue<Long> requestLog;
    private final AtomicLong requestLogSize;
    private final Supplier<Long> timeSource;
    public SlidingWindowLogRateLimiter(Integer windowSize, Integer maxRequests) {
        this.timeSource = System::nanoTime;
        this.windowSize = Long.valueOf(windowSize);
        this.maxRequests = maxRequests;
        this.requestLog = new ConcurrentLinkedDeque<>();
        this.requestLogSize = new AtomicLong(0);
    }

    public SlidingWindowLogRateLimiter(Integer windowSize, Integer maxRequests, Supplier<Long> timeSource) {
        this.timeSource = timeSource;
        this.windowSize = Long.valueOf(windowSize);
        this.maxRequests = maxRequests;
        this.requestLog = new ConcurrentLinkedDeque<>();
        this.requestLogSize = new AtomicLong(0);
    }
    @Override
    public boolean allowRequest() {
        processRequestLog();
        Long currentTime = timeSource.get()/ TimeUnit.SECONDS.toNanos(1);
        if(requestLog.size() < maxRequests) {
            if(requestLogSize.incrementAndGet() <= maxRequests) {
                requestLog.offer(currentTime);
                return true;
            }
            requestLogSize.decrementAndGet();
        }
        return false;
    }

    private void processRequestLog(){
        Long currentTime = timeSource.get()/ TimeUnit.SECONDS.toNanos(1);

        while(requestLogSize.get() > 0 && !requestLog.isEmpty() && currentTime - requestLog.peek() >= windowSize){
            if(requestLogSize.get() > 0) {
                synchronized (this) {
                    requestLog.poll();
                    requestLogSize.decrementAndGet();
                }
            }
        }
    }
}

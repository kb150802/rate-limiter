import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimeSource implements Supplier<Long> {
    private Long currentTime;
    public TimeSource(Long startTime) {
        this.currentTime = startTime;
    }
    @Override
    public Long get() {
        return currentTime;
    }
    public void advanceTime(Long timeInSeconds) {
        currentTime += timeInSeconds * TimeUnit.SECONDS.toNanos(timeInSeconds);
    }
}

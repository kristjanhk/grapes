package eu.kyngas.grapes.music.util;

import eu.kyngas.grapes.common.util.C;
import eu.kyngas.grapes.common.util.N;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
public class CircularQueue<T> {
  private final AtomicInteger currentSize = new AtomicInteger();
  private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
  private final int minSize;
  private final int maxSize;

  public CircularQueue(int minSize, int maxSize) {
    if (minSize < 0) {
      throw new IllegalArgumentException(String.format("minSize: %s (expected >= 0)", minSize));
    }
    if (maxSize <= 0) {
      throw new IllegalArgumentException(String.format("maxSize: %s (expected > 0)", maxSize));
    }
    if (minSize >= maxSize) {
      throw new IllegalArgumentException(String.format("minSize %s >= maxSize %s (expected minSize <= maxSize)",
                                                       minSize, maxSize));
    }
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  public T get() {
    if (currentSize.get() < minSize) {
      return null;
    }
    currentSize.getAndDecrement();
    return queue.remove();
  }

  public T get(Consumer<T> consumer) {
    return N.safe(get(), obj -> N.safe(consumer, c -> c.accept(obj)));
  }

  public T add(T obj) {
    C.check(currentSize.get() >= maxSize, queue::remove, currentSize::getAndIncrement);
    queue.add(obj);
    return obj;
  }

  public void clear() {
    queue.clear();
    currentSize.set(0);
  }
}

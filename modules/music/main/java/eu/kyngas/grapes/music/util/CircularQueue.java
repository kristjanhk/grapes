package eu.kyngas.grapes.music.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
@Slf4j
@RequiredArgsConstructor
public class CircularQueue<T> {
  private final ConcurrentLinkedQueue<T> queue;
  private final int maxSize;

  private AtomicInteger currentSize = new AtomicInteger();

  public T get() {
    if (currentSize.get() == 0) {
      return null;
    }
    currentSize.getAndDecrement();
    return queue.remove();
  }

  public T add(T obj) {
    int size = currentSize.get();
    if (size >= maxSize) {
      queue.remove();
    } else {
      currentSize.getAndIncrement();
    }
    queue.add(obj);
    return obj;
  }

  public void clear() {
    queue.clear();
    currentSize.set(0);
  }
}

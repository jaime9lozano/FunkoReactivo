package jaime.servicios;

import jaime.modelos.Funko;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FunkoCacheImp implements FunkoCache{
    private final int maxSize;
    private final Map<Long, Funko> cache;
    private final ScheduledExecutorService cleaner;
    public FunkoCacheImp(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<Long, Funko>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Funko> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public Mono<Void> put(Long key, Funko value) {
        return Mono.fromRunnable(() -> cache.put(key, value));
    }

    @Override
    public Mono<Funko> get(Long key) {
        return Mono.justOrEmpty(cache.get(key));
    }

    @Override
    public Mono<Void> remove(Long key) {
        return Mono.fromRunnable(() -> cache.remove(key));
    }

    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().fecha_cre().atStartOfDay().isBefore(LocalDateTime.now());
            if (shouldRemove) {
                System.out.println("Autoeliminando por caducidad alumno de cache con id: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}


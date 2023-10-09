package jaime.servicios;

import jaime.excepciones.FunkoNoEncontrado;
import jaime.modelos.Funko;
import jaime.modelos.Notificacion;
import jaime.repositorio.FunkoRepositorio;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FunkoServicioImp implements FunkoServicio{
    private static final int CACHE_SIZE = 15;
    private static FunkoServicioImp instance;
    private final FunkoCache cache;
    private final FunkoNotificacion notificacion;
    private final FunkoRepositorio funkosRepository;
    private FunkoServicioImp(FunkoRepositorio funkosRepository, FunkoNotificacion notification) {
        this.funkosRepository = funkosRepository;
        this.cache = new FunkoCacheImp(CACHE_SIZE);
        this.notificacion = notification;

    }


    public static FunkoServicioImp getInstance(FunkoRepositorio funkosRepository, FunkoNotificacion notification) {
        if (instance == null) {
            instance = new FunkoServicioImp(funkosRepository, notification);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        return funkosRepository.findAll();
    }

    @Override
    public Flux<Funko> findAllByNombre(String nombre) {
        return funkosRepository.findByNombre(nombre);
    }

    @Override
    public Mono<Funko> findById(long id) {
        return cache.get(id)
                .switchIfEmpty(funkosRepository.findById(id)
                        .flatMap(funko -> cache.put(funko.myID(), funko)
                                .then(Mono.just(funko)))
                        .switchIfEmpty(Mono.error(new FunkoNoEncontrado("Alumno con id " + id + " no encontrado"))));
    }

    private Mono<Funko> saveWithoutNotification(Funko funko) {
        // Hacemos esto para testar solo este método y no el save con notificaciones por los problemas que da el doOnSuccess
        // y porque nos falta "base" para testearlo

        return funkosRepository.save(funko)
                .flatMap(saved -> findById(saved.myID()));
    }
    @Override
    public Mono<Funko> save(Funko funko) {
        return saveWithoutNotification(funko)
                .doOnSuccess(saved -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.NEW, saved)));
    }

    private Mono<Funko> updateWithoutNotification(Funko funko) {
        // Hacemos esto para testar solo este método y no el update con notificaciones por los problemas que da el doOnSuccess
        // y porque nos falta "base" para testearlo

        return funkosRepository.findById(funko.myID())
                .switchIfEmpty(Mono.error(new FunkoNoEncontrado("Alumno con id " + funko.myID() + " no encontrado")))
                .flatMap(existing -> funkosRepository.update(funko)
                        .flatMap(updated -> cache.put(updated.myID(), updated)
                                .thenReturn(updated)));
    }
    @Override
    public Mono<Funko> update(Funko funko) {
        return updateWithoutNotification(funko)
                .doOnSuccess(updated -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.UPDATED, updated)));
    }

    private Mono<Funko> deleteByIdWithoutNotification(long id) {
        // Hacemos esto para testar solo este método y no el delete con notificaciones por los problemas que da el doOnSuccess
        // y porque nos falta "base" para testearlo

        return funkosRepository.findById(id)
                .switchIfEmpty(Mono.error(new FunkoNoEncontrado("Alumno con id " + id + " no encontrado")))
                .flatMap(funko -> cache.remove(funko.myID())
                        .then(funkosRepository.deleteById(funko.myID()))
                        .thenReturn(funko));
    }
    @Override
    public Mono<Funko> deleteById(long id) {
        return deleteByIdWithoutNotification(id)
                .doOnSuccess(deleted -> notificacion.notify(new Notificacion<>(Notificacion.Tipo.DELETED, deleted)));
    }

    @Override
    public Mono<Void> deleteAll() {
        cache.clear();
        return funkosRepository.deleteAll()
                .then(Mono.empty());
    }
    public Flux<Notificacion<Funko>> getNotifications() {
        return notificacion.getNotificationAsFlux();
    }
}

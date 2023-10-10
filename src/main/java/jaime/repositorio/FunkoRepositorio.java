package jaime.repositorio;

import jaime.modelos.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FunkoRepositorio extends CrudRepositorio<Funko, Long>{
    Flux<Funko> findByNombre(String nombre);
    Mono<Funko> funkoCaro();
    Mono<Double> mediaFunko();
}

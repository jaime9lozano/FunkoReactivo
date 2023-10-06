package jaime.repositorio;

import jaime.modelos.Funko;
import reactor.core.publisher.Flux;

public interface FunkoRepositorio extends CrudRepositorio<Funko, Long>{
    Flux<Funko> findByNombre(String nombre);
}

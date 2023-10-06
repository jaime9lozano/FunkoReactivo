package jaime.repositorio;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import jaime.servicios.DatabaseManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public class FunkoRepositorioImp implements FunkoRepositorio{
    private static FunkoRepositorioImp instance;
    private final ConnectionPool connectionFactory;
    private FunkoRepositorioImp(DatabaseManager databaseManager) {
        this.connectionFactory = databaseManager.getConnectionPool();
    }

    public static FunkoRepositorioImp getInstance(DatabaseManager db) {
        if (instance == null) {
            instance = new FunkoRepositorioImp(db);
        }
        return instance;
    }
    @Override
    public Flux<Funko> findAll() {
        String sql = "SELECT * FROM FUNKOS";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                Funko.builder()
                                        .cod(row.get("id", UUID.class))
                                        .nombre(row.get("nombre", String.class))
                                        .tipo(row.get("modelos", Tipos.class))
                                        .precio(row.get("precio", Double.class))
                                        .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                        .myID(row.get("MyID", Long.class))
                                        .build()
                        )),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> findById(Long id) {
        String sql = "SELECT * FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("id", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(row.get("modelos", Tipos.class))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                ))),
                Connection::close
        );
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        String sql = "INSERT INTO FUNKOS (cod,nombre,modelos,precio,fecha_lanzamiento,MyID,created_up,updated_up) VALUES (?, ?, ?, ?, ?,?,?,?)";
        LocalDate hoy = LocalDate.now();
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, funko.cod())
                        .bind(1, funko.nombre())
                        .bind(2, funko.tipo())
                        .bind(3, funko.precio())
                        .bind(4, funko.fecha_cre())
                        .bind(5,funko.myID())
                        .bind(6,funko.fecha_cre())
                        .bind(7,hoy)
                        .execute()
                ).then(Mono.just(funko)), // Aquí devolvemos el objeto 'alumno' después de la inserción
                Connection::close
        );
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        String query = "UPDATE FUNKOS SET nombre = ?, modelos = ?, precio = ?,updated_up = ? WHERE MyID = ?";
        LocalDate hoy = LocalDate.now();
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(query)
                        .bind(0, funko.nombre())
                        .bind(1, funko.tipo())
                        .bind(2, funko.precio())
                        .bind(3,hoy)
                        .bind(4,funko.myID())
                        .execute()
                ).then(Mono.just(funko)), // Aquí devolvemos el objeto 'alumno' después de la actualización
                Connection::close
        );
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        String sql = "DELETE FROM FUNKOS WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        ).flatMapMany(Result::getRowsUpdated)
                        .hasElements(),
                Connection::close
        );
    }

    @Override
    public Mono<Void> deleteAll() {
        String sql = "DELETE FROM FUNKOS";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .execute()
                ).then(),
                Connection::close
        );
    }

    @Override
    public Flux<Funko> findByNombre(String nombre) {
        String sql = "SELECT * FROM FUNKOS WHERE nombre LIKE ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, "%" + nombre + "%")
                        .execute()
                ).flatMap(result -> result.map((row, rowMetadata) ->
                        Funko.builder()
                                .cod(row.get("id", UUID.class))
                                .nombre(row.get("nombre", String.class))
                                .tipo(row.get("modelos", Tipos.class))
                                .precio(row.get("precio", Double.class))
                                .fecha_cre(row.get("fecha_lanzamiento", LocalDate.class))
                                .myID(row.get("MyID", Long.class))
                                .build()
                )),
                Connection::close
        );
    }
}

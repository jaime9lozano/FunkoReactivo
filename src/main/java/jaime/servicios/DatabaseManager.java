package jaime.servicios;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

import java.io.*;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private String databaseUsername = "sa"; // Fichero de configuración se lee en el constructor
    private String databasePassword = ""; // Fichero de configuración se lee en el constructor
    private boolean databaseInitTables = true; // Deberíamos inicializar las tablas? Fichero de configuración
    private String databaseUrl = "r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private DatabaseManager() {
        loadProperties();

        connectionFactory = ConnectionFactories.get(databaseUrl);
        // Configuramos el pool de conexiones
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000)) // Tiempo máximo de espera
                .maxSize(20) // Tamaño máximo del pool
                .build();

        pool = new ConnectionPool(configuration);
        // Por si hay que inicializar las tablas
        if (databaseInitTables) {
            initTables();
        }
    }
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    private synchronized void loadProperties() {
        try {
            var file = ClassLoader.getSystemResource("database.properties").getFile();
            var props = new Properties();
            props.load(new FileReader(file));
            // Establecemos la url de la base de datos
            databaseUrl = props.getProperty("database.url", "r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            databaseUsername = props.getProperty("database.username", "sa");
            databasePassword = props.getProperty("database.password", "");
            databaseInitTables = Boolean.parseBoolean(props.getProperty("database.initTables", "true"));
        } catch (IOException e) {

        }
    }
    public synchronized void initTables() {
        // Debes hacer un script por accion
        executeScript("remove.sql").block(); // Bloqueamos hasta que se ejecute (no nos interesa seguir hasta que se ejecute)
        executeScript("init.sql").block(); // Bloqueamos hasta que se ejecute (no nos interesa seguir hasta que se ejecute)
    }
    public Mono<Void> executeScript(String scriptSqlFile) {
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                 String scriptContent = null;
                 try{
                     try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptSqlFile)){
                         if (inputStream == null) {
                             return Mono.error(new IOException("No se ha encontrado el fichero de script de inicialización de la base de datos"));
                         }else{
                             try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                 scriptContent = reader.lines().collect(Collectors.joining("\n"));
                             }
                         }
                     }
                     Statement statement = connection.createStatement(scriptContent);
                     return Mono.from(statement.execute());
                 }catch(IOException e){
                     return Mono.error(e);
                 }
                },
                Connection::close
        ).then();
    }
    public ConnectionPool getConnectionPool() {
        return this.pool;
    }
}

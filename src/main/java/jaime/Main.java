package jaime;

import jaime.modelos.Funko;
import jaime.modelos.Tipos;
import jaime.repositorio.FunkoRepositorioImp;
import jaime.servicios.DatabaseManager;
import jaime.servicios.FunkoNotificacionImp;
import jaime.servicios.FunkoServicioImp;
import jaime.servicios.LeerCSV;

import java.time.LocalDate;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        LeerCSV l = LeerCSV.getInstance();
        FunkoServicioImp funkoServicio = FunkoServicioImp.getInstance(
                FunkoRepositorioImp.getInstance(DatabaseManager.getInstance()),
                FunkoNotificacionImp.getInstance()
        );
        funkoServicio.getNotifications().subscribe(
                notificacion -> {
                    switch (notificacion.getTipo()) {
                        case NEW:
                            System.out.println("🟢 Alumno insertado: " + notificacion.getContenido());
                            break;
                        case UPDATED:
                            System.out.println("🟠 Alumno actualizado: " + notificacion.getContenido());
                            break;
                        case DELETED:
                            System.out.println("🔴 Alumno eliminado: " + notificacion.getContenido());
                            break;
                    }
                },
                error -> System.err.println("Se ha producido un error: " + error),
                () -> System.out.println("Completado")
        );
        l.leerCsv().subscribe(funko -> funkoServicio.save(funko).subscribe());
        System.out.println("--------------------------------------------TODOS METIDOS---------------------------------------------");
        funkoServicio.findAll().subscribe(System.out::println);

        System.exit(0);
    }
}
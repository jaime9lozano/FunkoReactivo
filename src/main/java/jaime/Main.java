package jaime;

import jaime.repositorio.FunkoRepositorioImp;
import jaime.servicios.DatabaseManager;
import jaime.servicios.FunkoNotificacionImp;
import jaime.servicios.FunkoServicioImp;
import jaime.servicios.LeerCSV;

public class Main {
    public static void main(String[] args) {
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

        System.exit(0);
    }
}
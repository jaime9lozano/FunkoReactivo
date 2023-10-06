package jaime.modelos;

import java.time.LocalDate;
import java.util.UUID;

public record Funko(UUID cod, String nombre, Tipos tipo, Double precio, LocalDate fecha_cre, int myID) {
}

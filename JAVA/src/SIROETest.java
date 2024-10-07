import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SIROETest {
    private Profesor profesor;
    private Alumno alumno;
    private Asignatura asignatura;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        profesor = new Profesor();
        profesor.setNombre("Juan");
        profesor.setApellido("Pérez");

        alumno = new Alumno();
        alumno.setNombre("María");
        alumno.setApellido("García");

        asignatura = new Asignatura();
        asignatura.setNombre("Matemáticas");

        actividad = new Actividad();
        actividad.setNombre("Examen Parcial");
        actividad.setPuntosMaximos(100);
        actividad.setAsignatura(asignatura);
    }

    @Test
    void testAsignacionPuntosValida() {
        profesor.asignarPuntaje(alumno, actividad, 85, "Excelente desempeño");
        assertEquals(85, alumno.getPuntosAcumulados());
    }

    @Test
    void testAsignacionPuntosFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> {
            profesor.asignarPuntaje(alumno, actividad, 120, "Puntos fuera de rango");
        });
    }

    @Test
    void testVisualizacionPuntosAsignados() {
        profesor.asignarPuntaje(alumno, actividad, 75, "Buen trabajo");
        assertEquals(75, alumno.getPuntosAcumulados());
    }

    @Test
    void testAsignacionPuntosMultiplesCriterios() {
        CriterioAsignacionPuntos criterioContenido = new CriterioAsignacionPuntos();
        criterioContenido.setNombre("Contenido");
        criterioContenido.setPuntosAsignados(40);

        CriterioAsignacionPuntos criterioOrtografia = new CriterioAsignacionPuntos();
        criterioOrtografia.setNombre("Ortografía");
        criterioOrtografia.setPuntosAsignados(20);

        profesor.asignarPuntaje(alumno, actividad, criterioContenido.getPuntosAsignados(), "Buen contenido");
        profesor.asignarPuntaje(alumno, actividad, criterioOrtografia.getPuntosAsignados(), "Buena ortografía");

        assertEquals(60, alumno.getPuntosAcumulados());
    }
}
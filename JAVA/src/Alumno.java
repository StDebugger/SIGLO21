public class Alumno extends Usuario {
    private int puntosAcumulados;

    public void obtenerPuntos(AsignacionPuntos asignacion) {
        this.puntosAcumulados += asignacion.getPuntos();
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }
}
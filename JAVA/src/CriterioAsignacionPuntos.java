public class CriterioAsignacionPuntos {
    private int id;
    private String nombre;
    private String descripcion;
    private int puntosAsignados;

    // Constructor
    public CriterioAsignacionPuntos(int id, String nombre, String descripcion, int puntosAsignados) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosAsignados = puntosAsignados;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPuntosAsignados() {
        return puntosAsignados;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPuntosAsignados(int puntosAsignados) {
        this.puntosAsignados = puntosAsignados;
    }
}
public class Actividad {
    private int id;
    private String nombre;
    private String descripcion;
    private int puntosMaximos;
    private Asignatura asignatura;

    // Constructor
    public Actividad(int id, String nombre, String descripcion, int puntosMaximos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosMaximos = puntosMaximos;
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

    public int getPuntosMaximos() {
        return puntosMaximos;
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

    public void setPuntosMaximos(int puntosMaximos) {
        this.puntosMaximos = puntosMaximos;
    }
}
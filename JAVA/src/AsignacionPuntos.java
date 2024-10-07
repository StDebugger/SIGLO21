public class AsignacionPuntos {
    private int id;
    private Profesor profesor;
    private Alumno alumno;
    private Actividad actividad;
    private int puntos;
    private String comentario;
    private Date fecha;

    public AsignacionPuntos asignarPuntaje(Alumno alumno, Actividad actividad, CriterioAsignacionPuntos criterio, int puntos, String comentario) {

        // Validar que los puntos no excedan el máximo de la actividad
        if (puntos > actividad.getPuntosMaximos()) {
            throw new IllegalArgumentException("Los puntos asignados exceden el máximo permitido para esta actividad.");
        }

        // Validar que los puntos no excedan el máximo del criterio
        if (puntos > criterio.getPuntosAsignados()) {
            throw new IllegalArgumentException("Los puntos asignados exceden el máximo permitido para este criterio.");
        }

        // Crear una nueva AsignacionPuntos
        AsignacionPuntos asignacion = new AsignacionPuntos(
                0, // El id se asignará en la base de datos
                this.alumno,
                this.actividad,
                this.criterio,
                this.puntos,
                this.comentario
        );

        // Asignar los puntos al alumno
        alumno.obtenerPuntos(puntos);



        return asignacion;
    }

    // Método para verificar si el profesor puede asignar puntos a una actividad específica
    private boolean puedeAsignarPuntosEnActividad(Actividad actividad) {

        return true;
    }


    // Getters
    public int getId() {
        return id;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public CriterioAsignacionPuntos getCriterio() {
        return criterio;
    }

    public int getPuntos() {
        return puntos;
    }

    public String getComentario() {
        return comentario;
    }

    public Date getFecha() {
        return fecha;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public void setCriterio(CriterioAsignacionPuntos criterio) {
        this.criterio = criterio;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
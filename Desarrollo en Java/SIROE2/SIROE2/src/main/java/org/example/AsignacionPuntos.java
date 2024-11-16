package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AsignacionPuntos {
    private int id;
    private int profesorId;
    private int alumnoId;
    private int actividadId;
    private int criterioId;
    private int puntos;
    private String comentario;
    private LocalDateTime fecha;

    // Constructor vacío
    public AsignacionPuntos() {
        this.fecha = LocalDateTime.now();
    }

    // Constructor completo
    public AsignacionPuntos(int id, int profesorId, int alumnoId, int actividadId,
                            int criterioId, int puntos, String comentario) {
        this.id = id;
        this.profesorId = profesorId;
        this.alumnoId = alumnoId;
        this.actividadId = actividadId;
        this.criterioId = criterioId;
        this.puntos = puntos;
        this.comentario = comentario;
        this.fecha = LocalDateTime.now();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProfesorId() { return profesorId; }
    public void setProfesorId(int profesorId) { this.profesorId = profesorId; }
    public int getAlumnoId() { return alumnoId; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }
    public int getActividadId() { return actividadId; }
    public void setActividadId(int actividadId) { this.actividadId = actividadId; }
    public int getCriterioId() { return criterioId; }
    public void setCriterioId(int criterioId) { this.criterioId = criterioId; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    // Método para guardar la asignación de puntos
    public boolean guardar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Verificar si el alumno existe en la tabla ALUMNO
            String sqlVerificarAlumno = "SELECT id FROM ALUMNO WHERE id = ?";
            PreparedStatement stmtVerificarAlumno = conexion.prepareStatement(sqlVerificarAlumno);
            stmtVerificarAlumno.setInt(1, this.alumnoId);
            ResultSet rsAlumno = stmtVerificarAlumno.executeQuery();

            if (!rsAlumno.next()) {
                // Si el alumno no existe en la tabla ALUMNO, insertarlo
                String sqlInsertAlumno = "INSERT INTO ALUMNO (id, puntos) VALUES (?, 0)";
                PreparedStatement stmtInsertAlumno = conexion.prepareStatement(sqlInsertAlumno);
                stmtInsertAlumno.setInt(1, this.alumnoId);
                stmtInsertAlumno.executeUpdate();
            }

            // Verificar si el profesor existe en la tabla PROFESOR
            String sqlVerificarProfesor = "SELECT id FROM PROFESOR WHERE id = ?";
            PreparedStatement stmtVerificarProfesor = conexion.prepareStatement(sqlVerificarProfesor);
            stmtVerificarProfesor.setInt(1, this.profesorId);
            ResultSet rsProfesor = stmtVerificarProfesor.executeQuery();

            if (!rsProfesor.next()) {
                // Si el profesor no existe en la tabla PROFESOR, insertarlo
                String sqlInsertProfesor = "INSERT INTO PROFESOR (id) VALUES (?)";
                PreparedStatement stmtInsertProfesor = conexion.prepareStatement(sqlInsertProfesor);
                stmtInsertProfesor.setInt(1, this.profesorId);
                stmtInsertProfesor.executeUpdate();
            }

            // Iniciar transacción
            conexion.setAutoCommit(false);
            try {
                // Validar límites de puntos
                if (!validarPuntosMaximos()) {
                    throw new SQLException("Excede el límite de puntos para esta actividad");
                }

                // Insertar asignación de puntos
                String sql = "INSERT INTO ASIGNACION_PUNTOS (profesorID, alumnoID, actividadID, " +
                        "criterioID, puntos, comentario, fecha) VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, profesorId);
                stmt.setInt(2, alumnoId);
                stmt.setInt(3, actividadId);
                stmt.setInt(4, criterioId);
                stmt.setInt(5, puntos);
                stmt.setString(6, comentario);
                stmt.setTimestamp(7, Timestamp.valueOf(fecha));

                int filasAfectadas = stmt.executeUpdate();

                if (filasAfectadas > 0) {
                    // Obtener el ID generado
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        this.id = rs.getInt(1);
                    }

                    // Actualizar puntos del alumno
                    sql = "UPDATE ALUMNO SET puntos = puntos + ? WHERE id = ?";
                    stmt = conexion.prepareStatement(sql);
                    stmt.setInt(1, puntos);
                    stmt.setInt(2, alumnoId);
                    stmt.executeUpdate();

                    // Confirmar transacción
                    conexion.commit();
                    return true;
                }
                conexion.rollback();
                return false;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar asignación de puntos: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al guardar la asignación de puntos: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Método para validar que no se exceda el máximo de puntos
    private boolean validarPuntosMaximos() throws SQLException {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT puntosMaximos FROM ACTIVIDAD WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, actividadId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int puntosMaximos = rs.getInt("puntosMaximos");
                return this.puntos <= puntosMaximos;
            }
        }
        return false;
    }

    // Método para obtener asignaciones por alumno
    public static ArrayList<AsignacionPuntos> obtenerPorAlumno(int alumnoId) {
        ArrayList<AsignacionPuntos> asignaciones = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ASIGNACION_PUNTOS WHERE alumnoID = ? ORDER BY fecha DESC";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AsignacionPuntos asignacion = new AsignacionPuntos(
                        rs.getInt("id"),
                        rs.getInt("profesorID"),
                        rs.getInt("alumnoID"),
                        rs.getInt("actividadID"),
                        rs.getInt("criterioID"),
                        rs.getInt("puntos"),
                        rs.getString("comentario")
                );
                asignacion.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                asignaciones.add(asignacion);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asignaciones: " + e.getMessage());
        }
        return asignaciones;
    }

    // Método para mostrar el formulario de asignación de puntos
    public static void mostrarFormularioAsignacion(int profesorId) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Asignación de Puntos");
        dialog.setModal(true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Alumno
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Alumno:"), gbc);
        JComboBox<String> comboAlumnos = new JComboBox<>(obtenerAlumnos());
        gbc.gridx = 1;
        mainPanel.add(comboAlumnos, gbc);

        // Actividad
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Actividad:"), gbc);
        DefaultComboBoxModel<String> modelActividades = new DefaultComboBoxModel<>(obtenerActividades(profesorId));
        JComboBox<String> comboActividades = new JComboBox<>(modelActividades);
        gbc.gridx = 1;
        mainPanel.add(comboActividades, gbc);

        // Criterio
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Criterio:"), gbc);
        JComboBox<String> comboCriterios = new JComboBox<>(obtenerCriterios());
        gbc.gridx = 1;
        mainPanel.add(comboCriterios, gbc);

        // Puntos
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Puntos:"), gbc);
        JTextField txtPuntos = new JTextField(10);
        gbc.gridx = 1;
        mainPanel.add(txtPuntos, gbc);

        // Comentario
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Comentario:"), gbc);
        JTextArea txtComentario = new JTextArea(3, 20);
        txtComentario.setLineWrap(true);
        JScrollPane scrollComentario = new JScrollPane(txtComentario);
        gbc.gridx = 1;
        mainPanel.add(scrollComentario, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAsignar = new JButton("Asignar Puntos");
        JButton btnCancelar = new JButton("Cancelar");

        btnAsignar.addActionListener(e -> {
            try {
                if (validarDatos(txtPuntos.getText())) {
                    AsignacionPuntos asignacion = new AsignacionPuntos();
                    asignacion.setProfesorId(profesorId);
                    asignacion.setAlumnoId(obtenerIdAlumno((String)comboAlumnos.getSelectedItem()));
                    asignacion.setActividadId(obtenerIdActividad((String)comboActividades.getSelectedItem()));
                    asignacion.setCriterioId(obtenerIdCriterio((String)comboCriterios.getSelectedItem()));
                    asignacion.setPuntos(Integer.parseInt(txtPuntos.getText()));
                    asignacion.setComentario(txtComentario.getText());

                    if (asignacion.guardar()) {
                        JOptionPane.showMessageDialog(dialog, "Puntos asignados correctamente");
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Error al asignar puntos");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnAsignar);
        buttonPanel.add(btnCancelar);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Métodos auxiliares para obtener datos
    private static String[] obtenerAlumnos() {
        ArrayList<String> alumnos = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo " +
                    "FROM USUARIO WHERE rol = 'ALUMNO' ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                alumnos.add(rs.getString("nombre_completo"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener alumnos: " + e.getMessage());
        }
        return alumnos.toArray(new String[0]);
    }

    public static String[] obtenerActividades(int profesorId) {
        ArrayList<String> actividades = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.nombre FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON asig.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ? " +
                    "ORDER BY a.nombre";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, profesorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                actividades.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener actividades: " + e.getMessage());
        }
        return actividades.toArray(new String[0]);
    }

    private static String[] obtenerCriterios() {
        ArrayList<String> criterios = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre FROM CRITERIOS_PUNTOS ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                criterios.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener criterios: " + e.getMessage());
        }
        return criterios.toArray(new String[0]);
    }

    private static int obtenerIdAlumno(String nombreCompleto) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String[] nombres = nombreCompleto.split(" ", 2); // Dividir en nombre y apellido
            if (nombres.length < 2) {
                System.out.println("Formato de nombre incorrecto");
                return -1;
            }

            String sql = "SELECT u.id FROM USUARIO u " +
                    "WHERE u.nombre = ? AND u.apellido = ? AND u.rol = 'ALUMNO'";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombres[0]);
            stmt.setString(2, nombres[1]);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                System.out.println("No se encontró el alumno: " + nombreCompleto);
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de alumno: " + e.getMessage());
            return -1;
        }
    }

    private static int obtenerIdActividad(String nombreActividad) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT id FROM ACTIVIDAD WHERE nombre = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombreActividad);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de actividad: " + e.getMessage());
        }
        return -1;
    }

    private static int obtenerIdCriterio(String nombreCriterio) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT id FROM CRITERIOS_PUNTOS WHERE nombre = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombreCriterio);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de criterio: " + e.getMessage());
        }
        return -1;
    }

    private static boolean validarDatos(String puntosStr) {
        try {
            int puntos = Integer.parseInt(puntosStr);
            if (puntos <= 0) {
                JOptionPane.showMessageDialog(null,
                        "Los puntos deben ser un número mayor a 0");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Por favor ingrese un número válido para los puntos");
            return false;
        }
    }

    // Método para mostrar el historial de asignaciones
    public static void mostrarHistorialAsignaciones(int alumnoId) {
        JFrame frame = new JFrame("Historial de Asignaciones de Puntos");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Crear tabla con los datos
        String[] columnas = {"Fecha", "Actividad", "Profesor", "Puntos", "Criterio", "Comentario"};
        Object[][] datos = obtenerDatosHistorial(alumnoId);
        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Panel de filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filtrar por fecha:"));
        JTextField fechaDesde = new JTextField(10);
        JTextField fechaHasta = new JTextField(10);
        JButton btnFiltrar = new JButton("Filtrar");

        filterPanel.add(new JLabel("Desde:"));
        filterPanel.add(fechaDesde);
        filterPanel.add(new JLabel("Hasta:"));
        filterPanel.add(fechaHasta);
        filterPanel.add(btnFiltrar);

        // Evento del botón filtrar
        btnFiltrar.addActionListener(e -> {
            // Implementar lógica de filtrado
            actualizarTablaHistorial(tabla, alumnoId, fechaDesde.getText(), fechaHasta.getText());
        });

        frame.add(filterPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Panel de resumen
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        int totalPuntos = calcularTotalPuntos(datos);
        summaryPanel.add(new JLabel("Total de puntos: " + totalPuntos));
        frame.add(summaryPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static Object[][] obtenerDatosHistorial(int alumnoId) {
        ArrayList<Object[]> datos = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT ap.fecha, act.nombre as actividad, " +
                    "CONCAT(u.nombre, ' ', u.apellido) as profesor, " +
                    "ap.puntos, cp.nombre as criterio, ap.comentario " +
                    "FROM ASIGNACION_PUNTOS ap " +
                    "INNER JOIN ACTIVIDAD act ON ap.actividadID = act.id " +
                    "INNER JOIN USUARIO u ON ap.profesorID = u.id " +
                    "INNER JOIN CRITERIOS_PUNTOS cp ON ap.criterioID = cp.id " +
                    "WHERE ap.alumnoID = ? " +
                    "ORDER BY ap.fecha DESC";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                datos.add(new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getString("actividad"),
                        rs.getString("profesor"),
                        rs.getInt("puntos"),
                        rs.getString("criterio"),
                        rs.getString("comentario")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener historial: " + e.getMessage());
        }
        return datos.toArray(new Object[0][]);
    }

    private static void actualizarTablaHistorial(JTable tabla, int alumnoId,
                                                 String fechaDesde, String fechaHasta) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT ap.fecha, act.nombre as actividad, " +
                    "CONCAT(u.nombre, ' ', u.apellido) as profesor, " +
                    "ap.puntos, cp.nombre as criterio, ap.comentario " +
                    "FROM ASIGNACION_PUNTOS ap " +
                    "INNER JOIN ACTIVIDAD act ON ap.actividadID = act.id " +
                    "INNER JOIN USUARIO u ON ap.profesorID = u.id " +
                    "INNER JOIN CRITERIOS_PUNTOS cp ON ap.criterioID = cp.id " +
                    "WHERE ap.alumnoID = ? ";

            if (!fechaDesde.isEmpty() && !fechaHasta.isEmpty()) {
                sql += "AND ap.fecha BETWEEN ? AND ? ";
            }
            sql += "ORDER BY ap.fecha DESC";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, alumnoId);

            if (!fechaDesde.isEmpty() && !fechaHasta.isEmpty()) {
                stmt.setString(2, fechaDesde + " 00:00:00");
                stmt.setString(3, fechaHasta + " 23:59:59");
            }

            ResultSet rs = stmt.executeQuery();
            ArrayList<Object[]> datos = new ArrayList<>();

            while (rs.next()) {
                datos.add(new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getString("actividad"),
                        rs.getString("profesor"),
                        rs.getInt("puntos"),
                        rs.getString("criterio"),
                        rs.getString("comentario")
                });
            }

            DefaultTableModel model = (DefaultTableModel) tabla.getModel();
            model.setDataVector(datos.toArray(new Object[0][]),
                    new String[]{"Fecha", "Actividad", "Profesor", "Puntos", "Criterio", "Comentario"});

        } catch (SQLException e) {
            System.out.println("Error al actualizar historial: " + e.getMessage());
        }
    }

    private static int calcularTotalPuntos(Object[][] datos) {
        int total = 0;
        for (Object[] dato : datos) {
            total += (int) dato[3]; // La columna 3 contiene los puntos
        }
        return total;
    }

    @Override
    public String toString() {
        return "AsignacionPuntos{" +
                "fecha=" + fecha +
                ", actividad='" + obtenerNombreActividad(actividadId) + '\'' +
                ", puntos=" + puntos +
                ", profesor='" + obtenerNombreProfesor(profesorId) + '\'' +
                '}';
    }

    // Métodos auxiliares para obtener nombres
    private String obtenerNombreActividad(int actividadId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre FROM ACTIVIDAD WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, actividadId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener nombre de actividad: " + e.getMessage());
        }
        return "";
    }

    private String obtenerNombreProfesor(int profesorId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo " +
                    "FROM USUARIO WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, profesorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre_completo");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener nombre de profesor: " + e.getMessage());
        }
        return "";
    }
}
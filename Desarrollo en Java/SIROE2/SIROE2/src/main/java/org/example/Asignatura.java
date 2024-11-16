package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Asignatura {
    private int id;
    private String nombre;
    private String descripcion;
    private ArrayList<Actividad> actividades;

    // Constructor vacío
    public Asignatura() {
        this.actividades = new ArrayList<>();
    }

    // Constructor completo
    public Asignatura(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.actividades = new ArrayList<>();
        cargarActividades();
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public ArrayList<Actividad> getActividades() { return actividades; }
    public void setActividades(ArrayList<Actividad> actividades) { this.actividades = actividades; }

    // Método para cargar las actividades de la asignatura
    private void cargarActividades() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ACTIVIDAD WHERE asignaturaID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Actividad actividad = new Actividad(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos")
                );
                actividades.add(actividad);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar actividades: " + e.getMessage());
        }
    }

    // Método para guardar la asignatura en la base de datos
    public boolean guardar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql;
            if (this.id == 0) {
                sql = "INSERT INTO ASIGNATURA (nombre, descripcion) VALUES (?, ?)";
            } else {
                sql = "UPDATE ASIGNATURA SET nombre = ?, descripcion = ? WHERE id = ?";
            }

            PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, this.nombre);
            stmt.setString(2, this.descripcion);
            if (this.id != 0) {
                stmt.setInt(3, this.id);
            }

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0 && this.id == 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Error al guardar asignatura: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar la asignatura
    private boolean eliminar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {
            // Eliminar las actividades asociadas a la asignatura
            eliminarActividadesDeAsignatura(this.id, conexion);

            // Eliminar las asignaciones de profesores a la asignatura
            eliminarAsignacionesProfesoresDeAsignatura(this.id, conexion);

            // Eliminar la asignatura
            String sql = "DELETE FROM ASIGNATURA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Asignatura eliminada exitosamente");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar asignatura: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
            return false;
        }
    }

    // Método para buscar una asignatura por ID
    public static Asignatura buscarPorId(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ASIGNATURA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Asignatura(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar asignatura: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener todas las asignaturas
    public static ArrayList<Asignatura> obtenerTodas() {
        ArrayList<Asignatura> asignaturas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ASIGNATURA ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Asignatura asignatura = new Asignatura(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion")
                );
                asignaturas.add(asignatura);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asignaturas: " + e.getMessage());
        }
        return asignaturas;
    }

    // Método para mostrar el panel de gestión de asignaturas
    public static void mostrarGestionAsignaturas() {
        JFrame frame = new JFrame("Gestión de Asignaturas");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Asignatura");
        JButton btnEditar = new JButton("Editar Asignatura");
        JButton btnEliminar = new JButton("Eliminar Asignatura");

        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Tabla de asignaturas
        String[] columnas = {"ID", "Nombre", "Descripción", "Número de Actividades"};
        JTable tabla = new JTable(obtenerDatosTabla(), columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Eventos de botones
        btnNueva.addActionListener(e -> mostrarFormularioAsignatura(null));
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                mostrarFormularioAsignatura(buscarPorId(id));
                actualizarTabla(tabla);
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una asignatura");
            }
        });

        // Evento del botón "Eliminar Asignatura"
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                if (confirmarEliminacion(buscarPorId(id))) {
                    if (eliminarAsignatura(id)) {
                        actualizarTabla(tabla);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una asignatura");
            }
        });

        frame.setVisible(true);
    }

    // Método para eliminar una asignatura
    private static boolean eliminarAsignatura(int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {
            // Iniciar transacción
            conexion.setAutoCommit(false);

            try {
                // Eliminar las asignaciones de puntos relacionadas a las actividades de la asignatura
                eliminarAsignacionesPuntosDeActividades(asignaturaId, conexion);

                // Eliminar las actividades asociadas a la asignatura
                eliminarActividadesDeAsignatura(asignaturaId, conexion);

                // Eliminar las asignaciones de profesores a la asignatura
                eliminarAsignacionesProfesoresDeAsignatura(asignaturaId, conexion);

                // Eliminar la asignatura
                String sql = "DELETE FROM ASIGNATURA WHERE id = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, asignaturaId);

                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    conexion.commit();
                    JOptionPane.showMessageDialog(null, "Asignatura eliminada exitosamente");
                    return true;
                } else {
                    conexion.rollback();
                    JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
                    return false;
                }
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar asignatura: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
            return false;
        }
    }

    // Método para eliminar las actividades asociadas a una asignatura
    private static void eliminarActividadesDeAsignatura(int asignaturaId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM ACTIVIDAD WHERE asignaturaID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, asignaturaId);
        stmt.executeUpdate();
    }

    // Método para eliminar las asignaciones de profesores a una asignatura
    private static void eliminarAsignacionesProfesoresDeAsignatura(int asignaturaId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM PROFESOR_ASIGNATURA WHERE asignaturaID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, asignaturaId);
        stmt.executeUpdate();
    }

    private static void eliminarAsignacionesPuntosDeActividades(int asignaturaId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM ASIGNACION_PUNTOS WHERE actividadID IN (SELECT id FROM ACTIVIDAD WHERE asignaturaID = ?)";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, asignaturaId);
        stmt.executeUpdate();
    }

    // Método para mostrar el formulario de asignatura
    private static void mostrarFormularioAsignatura(Asignatura asignatura) {
        JDialog dialog = new JDialog();
        dialog.setTitle(asignatura == null ? "Nueva Asignatura" : "Editar Asignatura");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField txtNombre = new JTextField();
        JTextArea txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);

        if (asignatura != null) {
            txtNombre.setText(asignatura.getNombre());
            txtDescripcion.setText(asignatura.getDescripcion());
        }

        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(scrollDescripcion);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> {
            if (validarDatos(txtNombre.getText())) {
                Asignatura nuevaAsignatura = asignatura == null ? new Asignatura() : asignatura;
                nuevaAsignatura.setNombre(txtNombre.getText());
                nuevaAsignatura.setDescripcion(txtDescripcion.getText());

                if (nuevaAsignatura.guardar()) {
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar la asignatura");
                }
            }
        });

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(btnCancelar);
        dialog.add(btnGuardar);

        dialog.setVisible(true);
    }

    // Métodos auxiliares
    private static boolean validarDatos(String nombre) {
        if (nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío");
            return false;
        }
        return true;
    }

    private static boolean confirmarEliminacion(Asignatura asignatura) {
        return JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar la asignatura '" + asignatura.getNombre() + "'?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static Object[][] obtenerDatosTabla() {
        ArrayList<Asignatura> asignaturas = obtenerTodas();
        Object[][] datos = new Object[asignaturas.size()][4];

        for (int i = 0; i < asignaturas.size(); i++) {
            Asignatura asignatura = asignaturas.get(i);
            datos[i][0] = asignatura.getId();
            datos[i][1] = asignatura.getNombre();
            datos[i][2] = asignatura.getDescripcion();
            datos[i][3] = asignatura.getActividades().size();
        }

        return datos;
    }

    private static void actualizarTabla(JTable tabla) {
        Object[][] nuevosDatos = obtenerDatosTabla();
        ((DefaultTableModel) tabla.getModel()).setDataVector(nuevosDatos,
                new String[]{"ID", "Nombre", "Descripción", "Número de Actividades"});
    }

    // Clase interna para representar una actividad
    private class Actividad {
        private int id;
        private String nombre;
        private String descripcion;
        private int puntosMaximos;

        public Actividad(int id, String nombre, String descripcion, int puntosMaximos) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.puntosMaximos = puntosMaximos;
        }

        // Getters y setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public int getPuntosMaximos() { return puntosMaximos; }
        public void setPuntosMaximos(int puntosMaximos) { this.puntosMaximos = puntosMaximos; }
    }

    private static int calcularTotalPuntos(Object[][] datos) {
        int totalPuntos = 0;
        for (Object[] fila : datos) {
            int puntosMaximos = (int) fila[3]; // La columna 3 contiene los puntos máximos
            totalPuntos += puntosMaximos;
        }
        return totalPuntos;
    }

    public static Object[][] obtenerActividades(int profesorId) {
        ArrayList<Object[]> actividades = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.*, asig.nombre as nombre_asignatura " +
                    "FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON a.asignaturaID = pa.asignaturaID " +
                    "WHERE pa.profesorID = ? " +
                    "ORDER BY a.nombre";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, profesorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                actividades.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos"),
                        rs.getString("nombre_asignatura")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener actividades: " + e.getMessage());
        }
        return actividades.toArray(new Object[0][]);
    }
}
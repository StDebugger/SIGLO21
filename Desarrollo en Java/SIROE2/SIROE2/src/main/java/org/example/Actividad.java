package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Actividad {
    private int id;
    private int asignaturaId;
    private String nombre;
    private String descripcion;
    private int puntosMaximos;

    // Constructor vacío
    public Actividad() {}

    // Constructor completo
    public Actividad(int id, int asignaturaId, String nombre, String descripcion, int puntosMaximos) {
        this.id = id;
        this.asignaturaId = asignaturaId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosMaximos = puntosMaximos;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAsignaturaId() { return asignaturaId; }
    public void setAsignaturaId(int asignaturaId) { this.asignaturaId = asignaturaId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getPuntosMaximos() { return puntosMaximos; }
    public void setPuntosMaximos(int puntosMaximos) { this.puntosMaximos = puntosMaximos; }

    // Método para guardar la actividad en la base de datos
    public boolean guardar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql;
            if (this.id == 0) {
                sql = "INSERT INTO ACTIVIDAD (asignaturaID, nombre, descripcion, puntosMaximos) " +
                        "VALUES (?, ?, ?, ?)";
            } else {
                sql = "UPDATE ACTIVIDAD SET asignaturaID = ?, nombre = ?, descripcion = ?, " +
                        "puntosMaximos = ? WHERE id = ?";
            }

            PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, this.asignaturaId);
            stmt.setString(2, this.nombre);
            stmt.setString(3, this.descripcion);
            stmt.setInt(4, this.puntosMaximos);

            if (this.id != 0) {
                stmt.setInt(5, this.id);
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
            System.out.println("Error al guardar actividad: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar la actividad
    public boolean eliminar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "DELETE FROM ACTIVIDAD WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar actividad: " + e.getMessage());
            return false;
        }
    }

    // Método para buscar una actividad por ID
    public static Actividad buscarPorId(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ACTIVIDAD WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Actividad(
                        rs.getInt("id"),
                        rs.getInt("asignaturaID"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar actividad: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener todas las actividades
    public static ArrayList<Actividad> obtenerTodas() {
        ArrayList<Actividad> actividades = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ACTIVIDAD ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Actividad actividad = new Actividad(
                        rs.getInt("id"),
                        rs.getInt("asignaturaID"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos")
                );
                actividades.add(actividad);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener actividades: " + e.getMessage());
        }
        return actividades;
    }

    // Método para obtener actividades por asignatura
    public static ArrayList<Actividad> obtenerPorAsignatura(int asignaturaId) {
        ArrayList<Actividad> actividades = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM ACTIVIDAD WHERE asignaturaID = ? ORDER BY nombre";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, asignaturaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Actividad actividad = new Actividad(
                        rs.getInt("id"),
                        rs.getInt("asignaturaID"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos")
                );
                actividades.add(actividad);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener actividades por asignatura: " + e.getMessage());
        }
        return actividades;
    }

    // Método para mostrar el panel de gestión de actividades
    public static void mostrarGestionActividades() {
        JFrame frame = new JFrame("Gestión de Actividades");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Panel superior con filtro de asignatura
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Asignatura:"));
        JComboBox<String> comboAsignaturas = new JComboBox<>(obtenerAsignaturas());
        topPanel.add(comboAsignaturas);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Actividad");
        JButton btnEditar = new JButton("Editar Actividad");
        JButton btnEliminar = new JButton("Eliminar Actividad");

        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Panel superior combinado
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(topPanel, BorderLayout.NORTH);
        headerPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Tabla de actividades
        String[] columnas = {"ID", "Nombre", "Descripción", "Puntos Máximos", "Asignatura"};
        JTable tabla = new JTable(obtenerDatosTabla(), columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Eventos de botones
        btnNueva.addActionListener(e -> mostrarFormularioActividad(null));
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                mostrarFormularioActividad(buscarPorId(id));
                actualizarTabla(tabla);
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una actividad");
            }
        });
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                Actividad actividad = buscarPorId(id);
                if (actividad != null && confirmarEliminacion(actividad)) {
                    actividad.eliminar();
                    actualizarTabla(tabla);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una actividad");
            }
        });

        // Evento del combobox de asignaturas
        comboAsignaturas.addActionListener(e -> {
            String asignatura = (String) comboAsignaturas.getSelectedItem();
            actualizarTablaFiltrada(tabla, asignatura);
        });

        frame.setVisible(true);
    }

    // Método para mostrar el formulario de actividad
    private static void mostrarFormularioActividad(Actividad actividad) {
        JDialog dialog = new JDialog();
        dialog.setTitle(actividad == null ? "Nueva Actividad" : "Editar Actividad");
        dialog.setModal(true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        // Campos del formulario
        JComboBox<String> comboAsignaturas = new JComboBox<>(obtenerAsignaturas());
        JTextField txtNombre = new JTextField();
        JTextArea txtDescripcion = new JTextArea();
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        JTextField txtPuntosMaximos = new JTextField();

        // Si es edición, establecer valores actuales
        if (actividad != null) {
            comboAsignaturas.setSelectedItem(obtenerNombreAsignatura(actividad.getAsignaturaId()));
            txtNombre.setText(actividad.getNombre());
            txtDescripcion.setText(actividad.getDescripcion());
            txtPuntosMaximos.setText(String.valueOf(actividad.getPuntosMaximos()));
        }

        // Agregar componentes al diálogo
        dialog.add(new JLabel("Asignatura:"));
        dialog.add(comboAsignaturas);
        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Descripción:"));
        dialog.add(scrollDescripcion);
        dialog.add(new JLabel("Puntos Máximos:"));
        dialog.add(txtPuntosMaximos);

        // Botones
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> {
            if (validarDatos(txtNombre.getText(), txtPuntosMaximos.getText())) {
                Actividad nuevaActividad = actividad == null ? new Actividad() : actividad;
                nuevaActividad.setAsignaturaId(obtenerIdAsignatura((String)comboAsignaturas.getSelectedItem()));
                nuevaActividad.setNombre(txtNombre.getText());
                nuevaActividad.setDescripcion(txtDescripcion.getText());
                nuevaActividad.setPuntosMaximos(Integer.parseInt(txtPuntosMaximos.getText()));

                if (nuevaActividad.guardar()) {
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar la actividad");
                }
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.add(btnCancelar);
        dialog.add(btnGuardar);

        dialog.setVisible(true);
    }

    // Métodos auxiliares
    private static String[] obtenerAsignaturas() {
        ArrayList<String> asignaturas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre FROM ASIGNATURA ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                asignaturas.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asignaturas: " + e.getMessage());
        }
        return asignaturas.toArray(new String[0]);
    }

    private static String obtenerNombreAsignatura(int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre FROM ASIGNATURA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, asignaturaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener nombre de asignatura: " + e.getMessage());
        }
        return "";
    }

    private static int obtenerIdAsignatura(String nombreAsignatura) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT id FROM ASIGNATURA WHERE nombre = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombreAsignatura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de asignatura: " + e.getMessage());
        }
        return -1;
    }

    private static boolean validarDatos(String nombre, String puntosMaximos) {
        if (nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío");
            return false;
        }

        try {
            int puntos = Integer.parseInt(puntosMaximos);
            if (puntos <= 0) {
                JOptionPane.showMessageDialog(null, "Los puntos máximos deben ser mayores a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Los puntos máximos deben ser un número válido");
            return false;
        }

        return true;
    }

    static boolean confirmarEliminacion(Actividad actividad) {
        return JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar la actividad '" + actividad.getNombre() + "'?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static Object[][] obtenerDatosTabla() {
        ArrayList<Actividad> actividades = obtenerTodas();
        Object[][] datos = new Object[actividades.size()][5];

        for (int i = 0; i < actividades.size(); i++) {
            Actividad actividad = actividades.get(i);
            datos[i][0] = actividad.getId();
            datos[i][1] = actividad.getNombre();
            datos[i][2] = actividad.getDescripcion();
            datos[i][3] = actividad.getPuntosMaximos();
            datos[i][4] = obtenerNombreAsignatura(actividad.getAsignaturaId());
        }

        return datos;
    }

    private static void actualizarTabla(JTable tabla) {
        Object[][] nuevosDatos = obtenerDatosTabla();
        ((DefaultTableModel) tabla.getModel()).setDataVector(nuevosDatos,
                new String[]{"ID", "Nombre", "Descripción", "Puntos Máximos", "Asignatura"});
    }

    private static void actualizarTablaFiltrada(JTable tabla, String asignatura) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.*, asig.nombre as nombre_asignatura " +
                    "FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "WHERE asig.nombre = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, asignatura);
            ResultSet rs = stmt.executeQuery();

            ArrayList<Object[]> datos = new ArrayList<>();
            while (rs.next()) {
                datos.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos"),
                        rs.getString("nombre_asignatura")
                });
            }

            Object[][] nuevosDatos = datos.toArray(new Object[0][]);
            ((DefaultTableModel) tabla.getModel()).setDataVector(nuevosDatos,
                    new String[]{"ID", "Nombre", "Descripción", "Puntos Máximos", "Asignatura"});

        } catch (SQLException e) {
            System.out.println("Error al filtrar actividades: " + e.getMessage());
        }
    }

    // Método para verificar si una actividad existe
    public static boolean existeActividad(String nombre, int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT COUNT(*) FROM ACTIVIDAD WHERE nombre = ? AND asignaturaID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setInt(2, asignaturaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar existencia de actividad: " + e.getMessage());
        }
        return false;
    }

    // Método para obtener el total de puntos asignados a una actividad
    public int obtenerTotalPuntosAsignados() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT SUM(puntos) FROM ASIGNACION_PUNTOS WHERE actividadID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener total de puntos asignados: " + e.getMessage());
        }
        return 0;
    }

    // Método toString para representación en string de la actividad
    @Override
    public String toString() {
        return "Actividad{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", asignatura='" + obtenerNombreAsignatura(asignaturaId) + '\'' +
                ", puntosMaximos=" + puntosMaximos +
                '}';
    }
}
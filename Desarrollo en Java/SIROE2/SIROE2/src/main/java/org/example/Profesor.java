package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Profesor extends Usuario {
    private ArrayList<String> asignaturas;
    private JComboBox<String> comboAsignaturas; // Agregar esta línea

    // Constructor vacío
    public Profesor() {
        super();
        this.asignaturas = new ArrayList<>();
    }

    // Constructor completo
    public Profesor(int id, String nombre, String apellido, String correoElectronico,
                    String nombreUsuario, String contraseña) {
        super(id, nombre, apellido, correoElectronico, nombreUsuario, contraseña);
        this.asignaturas = new ArrayList<>();
        cargarAsignaturas();
    }

    private void cargarAsignaturas() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {
            String sql = "SELECT a.nombre FROM ASIGNATURA a " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON a.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                asignaturas.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar asignaturas: " + e.getMessage());
        }
    }

    // Método estático para buscar profesor por nombre de usuario
    public static Profesor findProfesorByNombreUsuario(String nombreUsuario) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT u.* FROM USUARIO u " +
                    "INNER JOIN PROFESOR p ON u.id = p.id " +
                    "WHERE u.nombreUsuario = ? AND u.rol = 'PROFESOR'";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Profesor(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correoElectronico"),
                        rs.getString("nombreUsuario"),
                        rs.getString("contrasena")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar profesor: " + e.getMessage());
        }
        return null;
    }

    // Método para mostrar el panel del profesor
    public void mostrarPanelProfesor() {
        JFrame frame = new JFrame("Panel del Profesor");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Titulo
        JLabel lblTitulo = new JLabel("Bienvenido, Prof. " + this.nombre);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(JLabel.CENTER);
        frame.add(lblTitulo, BorderLayout.NORTH);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnActividades = new JButton("Gestionar Actividades");
        JButton btnPuntos = new JButton("Asignar Puntos");
        JButton btnReportes = new JButton("Ver Reportes");
        JButton btnSalir = new JButton("Cerrar Sesión");

        buttonPanel.add(btnActividades);
        buttonPanel.add(btnPuntos);
        buttonPanel.add(btnReportes);
        buttonPanel.add(btnSalir);

        frame.add(buttonPanel, BorderLayout.WEST);

        // Eventos de botones
        btnActividades.addActionListener(e -> mostrarGestionActividades(mainPanel));
        btnPuntos.addActionListener(e -> AsignacionPuntos.mostrarFormularioAsignacion(this.id));
        btnReportes.addActionListener(e -> mostrarReportes(mainPanel));
        btnSalir.addActionListener(e -> frame.dispose());

        frame.setVisible(true);
    }

    // Método para eliminar un profesor
    private boolean eliminarProfesor(int profesorId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {
            // Iniciar transacción
            conexion.setAutoCommit(false);

            try {
                // Eliminar las asignaciones de puntos relacionadas al profesor
                eliminarAsignacionesPuntosDeProfesor(profesorId, conexion);

                // Eliminar las asignaciones de este profesor a las asignaturas
                eliminarAsignacionesProfesoresDeAsignatura(profesorId, conexion);

                // Eliminar el registro del profesor
                String sql = "DELETE FROM PROFESOR WHERE id = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, profesorId);

                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    // Eliminar el usuario de la tabla USUARIO
                    sql = "DELETE FROM USUARIO WHERE id = ?";
                    stmt = conexion.prepareStatement(sql);
                    stmt.setInt(1, profesorId);
                    stmt.executeUpdate();

                    conexion.commit();
                    JOptionPane.showMessageDialog(null, "Profesor eliminado exitosamente");
                    return true;
                } else {
                    conexion.rollback();
                    JOptionPane.showMessageDialog(null, "Error al eliminar el profesor");
                    return false;
                }
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar profesor: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar el profesor");
            return false;
        }
    }

    private void eliminarAsignacionesPuntosDeProfesor(int profesorId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM ASIGNACION_PUNTOS WHERE profesorID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, profesorId);
        stmt.executeUpdate();
    }

    // Método para eliminar las asignaciones de un profesor a las asignaturas
    private void eliminarAsignacionesProfesoresDeAsignatura(int profesorId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM PROFESOR_ASIGNATURA WHERE profesorID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, profesorId);
        stmt.executeUpdate();
    }

    // Método para mostrar formulario de actividad
    private void mostrarFormularioActividad(Object[] actividad, String asignatura, DefaultTableModel model) {
        JDialog dialog = new JDialog();
        dialog.setTitle(actividad == null ? "Nueva Actividad" : "Editar Actividad");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Configuración de la asignatura (no editable)
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Asignatura:"), gbc);

        JTextField txtAsignatura = new JTextField(asignatura);
        txtAsignatura.setEditable(false);
        gbc.gridx = 1;
        dialog.add(txtAsignatura, gbc);

        // Campo nombre
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Nombre:"), gbc);

        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        // Campo descripción
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Descripción:"), gbc);

        JTextArea txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        gbc.gridx = 1;
        dialog.add(scrollDescripcion, gbc);

        // Campo puntos máximos
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Puntos Máximos:"), gbc);

        JTextField txtPuntosMaximos = new JTextField(10);
        gbc.gridx = 1;
        dialog.add(txtPuntosMaximos, gbc);

        // Si estamos editando, rellenar los campos
        if (actividad != null) {
            txtNombre.setText((String)actividad[1]);
            txtDescripcion.setText((String)actividad[2]);
            txtPuntosMaximos.setText(String.valueOf(actividad[3]));
        }

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Acción del botón guardar
        btnGuardar.addActionListener(e -> {
            // Validar datos
            if (validarDatosActividad(txtNombre.getText(), txtPuntosMaximos.getText())) {
                try {
                    int puntosMax = Integer.parseInt(txtPuntosMaximos.getText());
                    // Si es una nueva actividad, id será 0
                    int id = actividad != null ? (int)actividad[0] : 0;

                    guardarActividad(
                            id,
                            txtNombre.getText(),
                            txtDescripcion.getText(),
                            puntosMax,
                            asignatura
                    );

                    // Actualizar la tabla
                    cargarActividades(model, asignatura);
                    dialog.dispose();

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Por favor ingrese un número válido para los puntos máximos",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción del botón cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        // Agregar panel de botones
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        dialog.add(buttonPanel, gbc);

        // Ajustar tamaño y mostrar
        dialog.pack();
        dialog.setVisible(true);
    }

    // Método para editar actividad
    private void editarActividad(JTable tabla, DefaultTableModel model, int fila) {
        try {
            if (comboAsignaturas.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(null,
                        "Por favor, seleccione una asignatura primero",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int id = (int) tabla.getValueAt(fila, 0);
            String nombre = (String) tabla.getValueAt(fila, 1);
            String descripcion = (String) tabla.getValueAt(fila, 2);
            int puntosMaximos = (int) tabla.getValueAt(fila, 3);
            String asignatura = (String) comboAsignaturas.getSelectedItem();

            mostrarFormularioActividad(
                    new Object[]{id, nombre, descripcion, puntosMaximos},
                    asignatura,
                    model
            );
        } catch (Exception e) {
            System.out.println("Error al editar actividad: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al editar la actividad. Por favor, inténtelo de nuevo.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // Método para mostrar el panel de gestión de actividades
    private void mostrarGestionActividades(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Combobox para mostrar las asignaturas asignadas al profesor
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Asignatura:"));
        comboAsignaturas = new JComboBox<>(asignaturas.toArray(new String[0]));
        topPanel.add(comboAsignaturas);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Actividad");
        JButton btnEditar = new JButton("Editar Actividad");
        JButton btnEliminar = new JButton("Eliminar Actividad");

        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Tabla de actividades
        String[] columnas = {"ID", "Nombre", "Descripción", "Puntos Máximos", "Asignatura"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Cargar actividades de la primera asignatura por defecto
        if (comboAsignaturas.getItemCount() > 0) {
            cargarActividades(model, comboAsignaturas.getSelectedItem().toString());
        }

        // Eventos
        comboAsignaturas.addActionListener(e -> {
            if (comboAsignaturas.getSelectedItem() != null) {
                cargarActividades(model, comboAsignaturas.getSelectedItem().toString());
            }
        });

        btnNueva.addActionListener(e -> {
            if (comboAsignaturas.getSelectedItem() != null) {
                mostrarFormularioActividad(null, comboAsignaturas.getSelectedItem().toString(), model);
            } else {
                JOptionPane.showMessageDialog(panel, "No tiene asignaturas asignadas");
            }
        });

        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                editarActividad(tabla, model, fila);
            } else {
                JOptionPane.showMessageDialog(panel, "Seleccione una actividad");
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                eliminarActividad(tabla, model, fila);
            } else {
                JOptionPane.showMessageDialog(panel, "Seleccione una actividad");
            }
        });

        // Agregar componentes al panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Métodos auxiliares para la gestión de actividades
    private void cargarActividades(DefaultTableModel model, String asignatura) {
        model.setRowCount(0);
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.* FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON asig.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ? AND asig.nombre = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            stmt.setString(2, asignatura);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar actividades: " + e.getMessage());
        }
    }

    private void cargarActividadesPorAsignatura(DefaultTableModel model, int profesorId, String asignatura) {
        model.setRowCount(0);
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.id, a.nombre, a.descripcion, a.puntosMaximos, asig.nombre as asignatura " +
                    "FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON asig.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ? AND asig.nombre = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, profesorId);
            stmt.setString(2, asignatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos"),
                        rs.getString("asignatura")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar actividades: " + e.getMessage());
        }
    }

    // Método auxiliar para validar los datos del formulario
    private boolean validarDatosActividad(String nombre, String puntosMaximos) {
        if (nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "El nombre de la actividad no puede estar vacío",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            int puntos = Integer.parseInt(puntosMaximos);
            if (puntos <= 0) {
                JOptionPane.showMessageDialog(null,
                        "Los puntos máximos deben ser un número positivo",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Los puntos máximos deben ser un número válido",
                    "Error de validación",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    // Método para guardar actividad (nueva o existente)
    private void guardarActividad(int id, String nombre, String descripcion, int puntosMaximos, String asignatura) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Obtener ID de la asignatura
            String sqlAsignatura = "SELECT id FROM ASIGNATURA WHERE nombre = ?";
            PreparedStatement stmtAsignatura = conexion.prepareStatement(sqlAsignatura);
            stmtAsignatura.setString(1, asignatura);
            ResultSet rs = stmtAsignatura.executeQuery();

            if (rs.next()) {
                int asignaturaId = rs.getInt("id");
                String sql;
                if (id == 0) {
                    sql = "INSERT INTO ACTIVIDAD (asignaturaID, nombre, descripcion, puntosMaximos) VALUES (?, ?, ?, ?)";
                } else {
                    sql = "UPDATE ACTIVIDAD SET asignaturaID = ?, nombre = ?, descripcion = ?, puntosMaximos = ? WHERE id = ?";
                }

                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, asignaturaId);
                stmt.setString(2, nombre);
                stmt.setString(3, descripcion);
                stmt.setInt(4, puntosMaximos);
                if (id != 0) {
                    stmt.setInt(5, id);
                }

                if (stmt.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(null, "Actividad guardada exitosamente");
                } else {
                    JOptionPane.showMessageDialog(null, "Error al guardar la actividad");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar actividad: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al guardar la actividad");
        }
    }

    // Método para eliminar actividad
    private void eliminarActividad(JTable tabla, DefaultTableModel model, int fila) {
        int id = (int) tabla.getValueAt(fila, 0);
        if (JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar esta actividad?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            if (eliminarActividad(id)) {
                model.removeRow(fila);
                JOptionPane.showMessageDialog(null, "Actividad eliminada con éxito");
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar la actividad");
            }
        }
    }

    private boolean eliminarActividad(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "DELETE FROM ACTIVIDAD WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar actividad: " + e.getMessage());
            return false;
        }
    }

    private Object[] obtenerActividad(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.*, asig.nombre as asignatura " +
                    "FROM ACTIVIDAD a " +
                    "INNER JOIN ASIGNATURA asig ON a.asignaturaID = asig.id " +
                    "WHERE a.id = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("puntosMaximos"),
                        rs.getString("asignatura")
                };
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener actividad: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener asignaturas disponibles
    private String[] obtenerAsignaturasDisponibles() {
        ArrayList<String> asignaturas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.nombre FROM ASIGNATURA a " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON a.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                asignaturas.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener asignaturas: " + e.getMessage());
        }
        return asignaturas.toArray(new String[0]);
    }

    // Método auxiliar para obtener ID de asignatura
    private int obtenerAsignaturaId(String nombreAsignatura) throws SQLException {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT id FROM ASIGNATURA WHERE nombre = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombreAsignatura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    // Método para mostrar la ventana de reportes
    private void mostrarReportes(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Aquí puedes agregar la lógica para mostrar los reportes en el panel
        // Por ejemplo, puedes crear componentes Swing y agregarlos al panel

        JLabel reporteLabel = new JLabel("Reportes del Profesor");
        reporteLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(reporteLabel, BorderLayout.NORTH);

        // Agrega más componentes y lógica para mostrar la información de los reportes

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}
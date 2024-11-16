package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Administrador extends Usuario {

    // Constructor vacío
    public Administrador() {
        super();
    }

    // Constructor completo
    public Administrador(int id, String nombre, String apellido, String correoElectronico,
                         String nombreUsuario, String contraseña) {
        super(id, nombre, apellido, correoElectronico, nombreUsuario, contraseña);
    }

    // Buscar administrador por nombre de usuario
    public static Administrador findAdminByNombreUsuario(String nombreUsuario) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        Administrador admin = null;

        try {
            Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword);
            String sql = "SELECT u.* FROM USUARIO u " +
                    "INNER JOIN ADMINISTRADOR a ON u.id = a.id " +
                    "WHERE u.nombreUsuario = ? AND u.rol = 'ADMINISTRADOR'";

            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombreUsuario);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                admin = new Administrador(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correoElectronico"),
                        rs.getString("nombreUsuario"),
                        rs.getString("contrasena")
                );
            }

            rs.close();
            statement.close();
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al buscar administrador: " + e.getMessage());
        }
        return admin;
    }

    private void mostrarFormularioNuevoUsuario() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Nuevo Usuario");
        dialog.setModal(true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos del formulario
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Apellido:"), gbc);
        JTextField txtApellido = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtApellido, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Email:"), gbc);
        JTextField txtEmail = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Usuario:"), gbc);
        JTextField txtUsuario = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Contraseña:"), gbc);
        JPasswordField txtPassword = new JPasswordField(20);
        gbc.gridx = 1;
        dialog.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Rol:"), gbc);
        JComboBox<String> comboRol = new JComboBox<>(new String[]{"ALUMNO", "PROFESOR", "ADMINISTRADOR"});
        gbc.gridx = 1;
        dialog.add(comboRol, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> {
            if (validarDatosUsuario(txtNombre.getText(), txtApellido.getText(), txtEmail.getText(),
                    txtUsuario.getText(), new String(txtPassword.getPassword()))) {
                guardarNuevoUsuario(
                        txtNombre.getText(),
                        txtApellido.getText(),
                        txtEmail.getText(),
                        txtUsuario.getText(),
                        new String(txtPassword.getPassword()),
                        (String)comboRol.getSelectedItem()
                );
                dialog.dispose();
                // Actualizar la tabla de usuarios
                SwingUtilities.invokeLater(() -> {
                    // Buscar el panel principal
                    Container parent = dialog.getParent();
                    while (parent != null && !(parent instanceof JPanel)) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof JPanel) {
                        mostrarGestionUsuarios((JPanel)parent);
                    }
                });
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // Para validación durante el registro de usuario
    private boolean validarDatosUsuario(String nombre, String apellido, String email, String usuario, String password) {
        // Validar que ningún campo esté vacío
        if (nombre.trim().isEmpty() || apellido.trim().isEmpty() || email.trim().isEmpty() ||
                usuario.trim().isEmpty() || password.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios");
            return false;
        }

        // Validar formato de email
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(null, "Email inválido");
            return false;
        }

        // Validar longitud mínima de usuario y contraseña
        if (usuario.length() < 4) {
            JOptionPane.showMessageDialog(null, "El nombre de usuario debe tener al menos 4 caracteres");
            return false;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(null, "La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }

    // Para validación de datos básicos
    private boolean validarDatosBasicos(String nombre, String apellido, String email, String rol) {
        if (nombre.trim().isEmpty() || apellido.trim().isEmpty() || email.trim().isEmpty() || rol.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(null, "Email inválido");
            return false;
        }
        return true;
    }

    private void actualizarTablaUsuarios(DefaultTableModel model) {
        model.setRowCount(0); // Limpiar la tabla
        Object[][] datos = obtenerUsuarios();
        for (Object[] fila : datos) {
            model.addRow(fila);
        }
    }

    private void guardarNuevoUsuario(String nombre, String apellido, String email,
                                     String usuario, String password, String rol) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Primero verificar si el usuario ya existe
            String sqlVerificar = "SELECT COUNT(*) FROM USUARIO WHERE nombreUsuario = ? OR correoElectronico = ?";
            PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
            stmtVerificar.setString(1, usuario);
            stmtVerificar.setString(2, email);
            ResultSet rs = stmtVerificar.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "Ya existe un usuario con ese nombre de usuario o correo electrónico",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            conexion.setAutoCommit(false);
            try {
                // Insertar en tabla USUARIO
                String sql = "INSERT INTO USUARIO (nombre, apellido, correoElectronico, nombreUsuario, contrasena, rol) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, nombre);
                stmt.setString(2, apellido);
                stmt.setString(3, email);
                stmt.setString(4, usuario);
                stmt.setString(5, password);
                stmt.setString(6, rol);

                stmt.executeUpdate();

                // Obtener el ID generado
                ResultSet rsId = stmt.getGeneratedKeys();
                if (rsId.next()) {
                    int userId = rsId.getInt(1);

                    // Insertar en la tabla correspondiente según el rol
                    String sqlRol;
                    switch (rol) {
                        case "ALUMNO":
                            sqlRol = "INSERT INTO ALUMNO (id, puntos) VALUES (?, 0)";
                            break;
                        case "PROFESOR":
                            sqlRol = "INSERT INTO PROFESOR (id) VALUES (?)";
                            break;
                        case "ADMINISTRADOR":
                            sqlRol = "INSERT INTO ADMINISTRADOR (id) VALUES (?)";
                            break;
                        default:
                            throw new SQLException("Rol no válido");
                    }

                    PreparedStatement stmtRol = conexion.prepareStatement(sqlRol);
                    stmtRol.setInt(1, userId);
                    stmtRol.executeUpdate();

                    conexion.commit();
                    JOptionPane.showMessageDialog(null, "Usuario guardado exitosamente");
                }
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al guardar el usuario: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarAsignaturasProfesor(int profesorId, DefaultListModel<String> modelDisponibles,
                                           DefaultListModel<String> modelAsignadas) {
        modelDisponibles.clear();
        modelAsignadas.clear();

        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Cargar asignaturas asignadas
            String sqlAsignadas = "SELECT a.nombre FROM ASIGNATURA a " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON a.id = pa.asignaturaID " +
                    "WHERE pa.profesorID = ?";
            PreparedStatement stmtAsignadas = conexion.prepareStatement(sqlAsignadas);
            stmtAsignadas.setInt(1, profesorId);
            ResultSet rsAsignadas = stmtAsignadas.executeQuery();

            while (rsAsignadas.next()) {
                modelAsignadas.addElement(rsAsignadas.getString("nombre"));
            }

            // Cargar asignaturas disponibles (no asignadas)
            String sqlDisponibles = "SELECT nombre FROM ASIGNATURA " +
                    "WHERE id NOT IN (SELECT asignaturaID FROM PROFESOR_ASIGNATURA WHERE profesorID = ?)";
            PreparedStatement stmtDisponibles = conexion.prepareStatement(sqlDisponibles);
            stmtDisponibles.setInt(1, profesorId);
            ResultSet rsDisponibles = stmtDisponibles.executeQuery();

            while (rsDisponibles.next()) {
                modelDisponibles.addElement(rsDisponibles.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar asignaturas: " + e.getMessage());
        }
    }

    private String[] obtenerAsignaturasDisponibles() {
        ArrayList<String> asignaturas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre FROM ASIGNATURA";
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

    private int obtenerIdProfesor(String nombreCompleto) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String[] nombres = nombreCompleto.split(" ", 2); // Dividir en nombre y apellido
            if (nombres.length < 2) {
                System.out.println("Formato de nombre incorrecto");
                return -1;
            }

            String sql = "SELECT u.id FROM USUARIO u " +
                    "WHERE u.nombre = ? AND u.apellido = ? AND u.rol = 'PROFESOR'";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombres[0]);
            stmt.setString(2, nombres[1]);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            } else {
                System.out.println("No se encontró el profesor: " + nombreCompleto);
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID de profesor: " + e.getMessage());
            return -1;
        }
    }

    private int obtenerIdAsignatura(String nombreAsignatura) {
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

    // Modificar la firma del método para usar ArrayList en lugar de List
    private void asignarAsignaturasAProfesor(int profesorId, ArrayList<String> asignaturasSeleccionadas) {
        for (String asignatura : asignaturasSeleccionadas) {
            int asignaturaId = obtenerIdAsignatura(asignatura);
            if (asignaturaId != -1) {
                asignarAsignaturaAProfesor(profesorId, asignaturaId);
            }
        }
    }



    private void mostrarGestionAsignaturas(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Asignatura");
        JButton btnEditar = new JButton("Editar Asignatura");
        JButton btnEliminar = new JButton("Eliminar Asignatura");
        JButton btnAsignarProfesor = new JButton("Asignar Profesor");
        JButton btnAsignarAsignaturas = new JButton("Asignar Asignaturas a Profesores");


        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnAsignarProfesor);

        // Tabla de asignaturas
        String[] columnas = {"ID", "Nombre", "Descripción", "Profesores Asignados"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Cargar datos en la tabla
        cargarAsignaturasEnTabla(model);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Eventos de botones
        btnAsignarAsignaturas.addActionListener(e -> mostrarGestionAsignacionAsignaturas(mainPanel));
        btnNueva.addActionListener(e -> mostrarFormularioAsignatura(null, model));
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                mostrarFormularioAsignatura(Asignatura.buscarPorId(id), model);
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione una asignatura");
            }
        });
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                if (confirmarEliminacion()) {
                    eliminarAsignatura(id);
                    cargarAsignaturasEnTabla(model);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione una asignatura");
            }
        });
        btnAsignarProfesor.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                mostrarFormularioAsignacionProfesor(id, model);
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione una asignatura");
            }
        });

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void mostrarGestionAsignacionAsignaturas(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel de selección de profesor
        JPanel profesorPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        profesorPanel.setBorder(BorderFactory.createTitledBorder("Seleccionar Profesor"));

        JLabel profesorLabel = new JLabel("Profesor:");
        JComboBox<String> comboProfesores = new JComboBox<>(obtenerProfesoresDisponibles());
        profesorPanel.add(profesorLabel);
        profesorPanel.add(comboProfesores);

        // Panel de selección de asignaturas
        JPanel asignaturasPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        asignaturasPanel.setBorder(BorderFactory.createTitledBorder("Seleccionar Asignaturas"));

        JLabel asignaturasLabel = new JLabel("Asignaturas:");
        JList<String> listaAsignaturas = new JList<>(obtenerAsignaturasDisponibles());
        listaAsignaturas.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaAsignaturas);
        asignaturasPanel.add(asignaturasLabel);
        asignaturasPanel.add(scrollPane);

        // Botón de asignación
        JButton btnAsignar = new JButton("Asignar Asignaturas");
        btnAsignar.addActionListener(e -> {
            if (comboProfesores.getSelectedItem() != null && !listaAsignaturas.isSelectionEmpty()) {
                int profesorId = obtenerIdProfesor(comboProfesores.getSelectedItem().toString());
                ArrayList<String> asignaturasSeleccionadas = new ArrayList<>(
                        listaAsignaturas.getSelectedValuesList()
                );
                asignarAsignaturasAProfesor(profesorId, asignaturasSeleccionadas);
                JOptionPane.showMessageDialog(panel, "Asignaturas asignadas correctamente");
                mostrarGestionAsignaturas(mainPanel);
            } else {
                JOptionPane.showMessageDialog(panel, "Seleccione un profesor y al menos una asignatura");
            }
        });

        panel.add(profesorPanel, BorderLayout.NORTH);
        panel.add(asignaturasPanel, BorderLayout.CENTER);
        panel.add(btnAsignar, BorderLayout.SOUTH);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Método para desasignar asignatura de profesor
    private void desasignarAsignaturaDeProfesor(int profesorId, String asignatura,
                                                DefaultListModel<String> modelDisponibles, DefaultListModel<String> modelAsignadas) {

        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Obtener ID de la asignatura
            int asignaturaId = obtenerIdAsignatura(asignatura);

            if (asignaturaId != -1) {
                String sql = "DELETE FROM PROFESOR_ASIGNATURA WHERE profesorID = ? AND asignaturaID = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, profesorId);
                stmt.setInt(2, asignaturaId);

                if (stmt.executeUpdate() > 0) {
                    // Actualizar los modelos
                    modelAsignadas.removeElement(asignatura);
                    modelDisponibles.addElement(asignatura);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al desasignar asignatura: " + e.getMessage());
        }
    }

    private void mostrarFormularioAsignatura(Asignatura asignatura, DefaultTableModel model) {
        JDialog dialog = new JDialog();
        dialog.setTitle(asignatura == null ? "Nueva Asignatura" : "Editar Asignatura");
        dialog.setModal(true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos básicos
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Descripción:"), gbc);
        JTextArea txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        gbc.gridx = 1;
        dialog.add(scrollDescripcion, gbc);

        // Selector de profesor
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Profesor:"), gbc);
        JComboBox<String> comboProfesores = new JComboBox<>(obtenerProfesoresDisponibles());
        gbc.gridx = 1;
        dialog.add(comboProfesores, gbc);

        if (asignatura != null) {
            txtNombre.setText(asignatura.getNombre());
            txtDescripcion.setText(asignatura.getDescripcion());
            // Seleccionar profesor actual si existe
            String profesorActual = obtenerProfesorAsignatura(asignatura.getId());
            if (profesorActual != null) {
                comboProfesores.setSelectedItem(profesorActual);
            }
        }

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> {
            if (validarDatosAsignatura(txtNombre.getText())) {
                Asignatura nuevaAsignatura = asignatura == null ? new Asignatura() : asignatura;
                nuevaAsignatura.setNombre(txtNombre.getText());
                nuevaAsignatura.setDescripcion(txtDescripcion.getText());

                if (nuevaAsignatura.guardar()) {
                    // Asignar profesor
                    if (comboProfesores.getSelectedItem() != null) {
                        asignarProfesorAAsignatura(
                                (String)comboProfesores.getSelectedItem(),
                                nuevaAsignatura.getId()
                        );
                    }
                    cargarAsignaturasEnTabla(model);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error al guardar la asignatura");
                }
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // Método para obtener el profesor asignado a una asignatura
    private String obtenerProfesorAsignatura(int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT CONCAT(u.nombre, ' ', u.apellido) as profesor " +
                    "FROM USUARIO u " +
                    "INNER JOIN PROFESOR_ASIGNATURA pa ON u.id = pa.profesorID " +
                    "WHERE pa.asignaturaID = ?";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, asignaturaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("profesor");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener profesor de asignatura: " + e.getMessage());
        }
        return null;
    }



    // Método para validar la asignación de profesor
    // Método para asignar profesor a asignatura
    private boolean asignarProfesorAAsignatura(String nombreProfesor, int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Primero verificar si el profesor existe
            int profesorId = obtenerIdProfesor(nombreProfesor);
            if (profesorId == -1) {
                JOptionPane.showMessageDialog(null,
                        "No se encontró el profesor en el sistema",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Verificar si existe en la tabla PROFESOR
            String sqlVerificar = "SELECT id FROM PROFESOR WHERE id = ?";
            PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
            stmtVerificar.setInt(1, profesorId);
            ResultSet rs = stmtVerificar.executeQuery();

            if (!rs.next()) {
                // Si no existe en la tabla PROFESOR, insertarlo
                String sqlInsertProfesor = "INSERT INTO PROFESOR (id) VALUES (?)";
                PreparedStatement stmtInsertProfesor = conexion.prepareStatement(sqlInsertProfesor);
                stmtInsertProfesor.setInt(1, profesorId);
                stmtInsertProfesor.executeUpdate();
            }

            // Iniciar transacción
            conexion.setAutoCommit(false);
            try {
                // Eliminar asignaciones existentes
                String sqlDelete = "DELETE FROM PROFESOR_ASIGNATURA WHERE asignaturaID = ?";
                PreparedStatement stmtDelete = conexion.prepareStatement(sqlDelete);
                stmtDelete.setInt(1, asignaturaId);
                stmtDelete.executeUpdate();

                // Crear la nueva asignación
                String sqlInsert = "INSERT INTO PROFESOR_ASIGNATURA (profesorID, asignaturaID) VALUES (?, ?)";
                PreparedStatement stmtInsert = conexion.prepareStatement(sqlInsert);
                stmtInsert.setInt(1, profesorId);
                stmtInsert.setInt(2, asignaturaId);

                boolean resultado = stmtInsert.executeUpdate() > 0;
                conexion.commit();
                return resultado;

            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al asignar profesor: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al asignar profesor a la asignatura: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void mostrarFormularioAsignacionProfesor(int asignaturaId, DefaultTableModel model) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Asignar Profesor");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(10, 10));

        // Panel para selección de profesor
        JPanel selectionPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JComboBox<String> comboProfesores = new JComboBox<>(obtenerProfesoresDisponibles());

        selectionPanel.add(new JLabel("Profesor:"));
        selectionPanel.add(comboProfesores);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAsignar = new JButton("Asignar");
        JButton btnCancelar = new JButton("Cancelar");

        btnAsignar.addActionListener(e -> {
            String nombreProfesor = (String) comboProfesores.getSelectedItem();
            if (asignarProfesorAAsignatura(nombreProfesor, asignaturaId)) {
                cargarAsignaturasEnTabla(model);
                dialog.dispose();
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnAsignar);
        buttonPanel.add(btnCancelar);

        dialog.add(selectionPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void cargarAsignaturasEnTabla(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT a.id, a.nombre, a.descripcion, " +
                    "GROUP_CONCAT(CONCAT(u.nombre, ' ', u.apellido) SEPARATOR ', ') as profesores " +
                    "FROM ASIGNATURA a " +
                    "LEFT JOIN PROFESOR_ASIGNATURA pa ON a.id = pa.asignaturaID " +
                    "LEFT JOIN PROFESOR p ON pa.profesorID = p.id " +
                    "LEFT JOIN USUARIO u ON p.id = u.id " +
                    "GROUP BY a.id, a.nombre, a.descripcion";

            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("profesores")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar asignaturas: " + e.getMessage());
        }
    }

    // Método para obtener profesores disponibles
    private String[] obtenerProfesoresDisponibles() {
        ArrayList<String> profesores = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT CONCAT(nombre, ' ', apellido) as nombre_completo " +
                    "FROM USUARIO WHERE rol = 'PROFESOR' ORDER BY nombre, apellido";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                profesores.add(rs.getString("nombre_completo"));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener lista de profesores: " + e.getMessage());
        }
        return profesores.toArray(new String[0]);
    }

    // Método para asignar asignatura a profesor (versión para la interfaz gráfica)
    private void asignarAsignaturaAProfesor(int profesorId, String asignatura,
                                            DefaultListModel<String> modelDisponibles, DefaultListModel<String> modelAsignadas) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            int asignaturaId = obtenerIdAsignatura(asignatura);
            if (asignaturaId != -1) {
                asignarAsignaturaAProfesor(profesorId, asignaturaId);
                // Actualizar los modelos
                modelDisponibles.removeElement(asignatura);
                modelAsignadas.addElement(asignatura);
            }
        } catch (SQLException e) {
            System.out.println("Error al asignar asignatura: " + e.getMessage());
        }
    }

    // Método base para asignar asignatura
    private boolean asignarAsignaturaAProfesor(int profesorId, int asignaturaId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "INSERT INTO PROFESOR_ASIGNATURA (profesorID, asignaturaID) VALUES (?, ?)";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, profesorId);
            stmt.setInt(2, asignaturaId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al asignar asignatura: " + e.getMessage());
            return false;
        }
    }

    private boolean validarDatosAsignatura(String nombre) {
        if (nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío");
            return false;
        }
        return true;
    }

    private boolean confirmarEliminacion() {
        return JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar esta asignatura?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void eliminarAsignatura(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "DELETE FROM ASIGNATURA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Asignatura eliminada exitosamente");
            } else {
                JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar asignatura: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al eliminar la asignatura");
        }
    }

    // Mostrar panel de administración
    public void mostrarPanelAdmin() {
        JFrame frame = new JFrame("SIROE - Panel de Administración");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Panel superior
        JPanel topPanel = new JPanel();
        JLabel titleLabel = new JLabel("Panel de Administración - " + this.nombre + " " + this.apellido);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel);

        // Panel de botones lateral
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(6, 1, 10, 10));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnUsuarios = new JButton("Gestionar Usuarios");
        JButton btnPuntos = new JButton("Gestionar Puntos");
        JButton btnRecompensas = new JButton("Gestionar Recompensas");
        JButton btnReportes = new JButton("Generar Reportes");
        JButton btnConfiguracion = new JButton("Configuración");
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        JButton btnAsignaturas = new JButton("Gestionar Asignaturas");

        sidePanel.add(btnAsignaturas);
        sidePanel.add(btnUsuarios);
        sidePanel.add(btnPuntos);
        sidePanel.add(btnRecompensas);
        sidePanel.add(btnReportes);
        sidePanel.add(btnConfiguracion);
        sidePanel.add(btnCerrarSesion);

        // Panel principal que cambiará según la opción seleccionada
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new CardLayout());

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(sidePanel, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);

        // Eventos de los botones
        btnUsuarios.addActionListener(e -> mostrarGestionUsuarios(mainPanel));
        btnPuntos.addActionListener(e -> mostrarGestionPuntos(mainPanel));
        btnRecompensas.addActionListener(e -> mostrarGestionRecompensas(mainPanel));
        btnReportes.addActionListener(e -> mostrarReportes(mainPanel));
        btnConfiguracion.addActionListener(e -> mostrarConfiguracion(mainPanel));
        btnAsignaturas.addActionListener(e -> mostrarGestionAsignaturas(mainPanel));
        btnCerrarSesion.addActionListener(e -> frame.dispose());

        frame.setVisible(true);
    }

    // Método para gestionar usuarios
    private DefaultTableModel modeloTablaUsuarios;

    private void mostrarGestionUsuarios(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel de botones superior
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevo = new JButton("Nuevo Usuario");
        JButton btnEditar = new JButton("Editar Usuario");
        JButton btnEliminar = new JButton("Eliminar Usuario");

        buttonPanel.add(btnNuevo);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Tabla de usuarios
        String[] columnas = {"ID", "Nombre", "Apellido", "Email", "Rol"};
        modeloTablaUsuarios = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(modeloTablaUsuarios);
        actualizarTablaUsuarios(modeloTablaUsuarios);
        JScrollPane scrollPane = new JScrollPane(tabla);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();

        // Eventos de botones
        btnNuevo.addActionListener(e -> mostrarFormularioNuevoUsuario());
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                editarUsuario((int)tabla.getValueAt(fila, 0));
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione un usuario");
            }
        });
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                eliminarUsuario((int)tabla.getValueAt(fila, 0));
                mostrarGestionUsuarios(mainPanel); // Recargar la tabla
            } else {
                JOptionPane.showMessageDialog(null, "Seleccione un usuario");
            }
        });
    }

    // Método para obtener usuarios de la BD
    private Object[][] obtenerUsuarios() {
        ArrayList<Object[]> usuarios = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT id, nombre, apellido, correoElectronico, rol FROM USUARIO";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                usuarios.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correoElectronico"),
                        rs.getString("rol")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios.toArray(new Object[0][]);
    }

    // Método para gestionar puntos
    private void mostrarGestionPuntos(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración de Puntos"));

        configPanel.add(new JLabel("Puntos máximos por actividad:"));
        JTextField txtMaxPuntos = new JTextField("100");
        configPanel.add(txtMaxPuntos);

        JButton btnGuardar = new JButton("Guardar Configuración");
        configPanel.add(btnGuardar);

        panel.add(configPanel, BorderLayout.NORTH);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Método para gestionar recompensas
    private void mostrarGestionRecompensas(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel de botones superior
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Recompensa");
        JButton btnEditar = new JButton("Editar Recompensa");
        JButton btnEliminar = new JButton("Eliminar Recompensa");

        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Tabla de recompensas
        String[] columnas = {"ID", "Nombre", "Descripción", "Costo (puntos)", "Stock"};
        modeloTablaRecompensas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Hace la tabla no editable
            }
        };

        JTable tabla = new JTable(modeloTablaRecompensas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Cargar datos iniciales
        actualizarTablaRecompensas(modeloTablaRecompensas);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();

        // Eventos de botones
        btnNueva.addActionListener(e -> mostrarFormularioNuevaRecompensa());

        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                editarRecompensa(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Por favor, seleccione una recompensa para editar");
            }
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                if (JOptionPane.showConfirmDialog(panel,
                        "¿Está seguro de que desea eliminar esta recompensa?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    eliminarRecompensa(id);
                    actualizarTablaRecompensas(modeloTablaRecompensas);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Por favor, seleccione una recompensa para eliminar");
            }
        });
    }

    // Método para obtener recompensas
    private Object[][] obtenerRecompensas() {
        ArrayList<Object[]> recompensas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM RECOMPENSA";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                recompensas.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("costoPuntos"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener recompensas: " + e.getMessage());
        }
        return recompensas.toArray(new Object[0][]);
    }



    private void eliminarRecompensa(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Primero verificar si la recompensa existe y no tiene canjes pendientes
            String sqlVerificar = "SELECT COUNT(*) FROM TRANSACCION WHERE recompensaID = ?";
            PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
            stmtVerificar.setInt(1, id);
            ResultSet rs = stmtVerificar.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "No se puede eliminar la recompensa porque tiene canjes asociados",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Si no hay canjes, proceder con la eliminación
            String sql = "DELETE FROM RECOMPENSA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(null,
                        "Recompensa eliminada exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se encontró la recompensa a eliminar",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar recompensa: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error al eliminar la recompensa: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para mostrar reportes
    private void mostrarReportes(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Reportes Disponibles"));

        JButton btnPuntajes = new JButton("Reporte de Puntajes");
        JButton btnRecompensas = new JButton("Reporte de Recompensas Canjeadas");
        JButton btnActividad = new JButton("Reporte de Actividad del Sistema");

        optionsPanel.add(btnPuntajes);
        optionsPanel.add(btnRecompensas);
        optionsPanel.add(btnActividad);

        panel.add(optionsPanel, BorderLayout.NORTH);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Método para mostrar configuración
    private void mostrarConfiguracion(JPanel mainPanel) {
        mainPanel.removeAll();
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel configPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración del Sistema"));

        // Añadir campos de configuración
        configPanel.add(new JLabel("Nombre de la Institución:"));
        configPanel.add(new JTextField());

        configPanel.add(new JLabel("Email de Contacto:"));
        configPanel.add(new JTextField());

        JButton btnGuardar = new JButton("Guardar Configuración");

        panel.add(configPanel, BorderLayout.NORTH);
        panel.add(btnGuardar, BorderLayout.SOUTH);

        mainPanel.add(panel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void editarUsuario(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM USUARIO WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JDialog dialog = new JDialog();
                dialog.setTitle("Editar Usuario");
                dialog.setModal(true);
                dialog.setSize(400, 500);
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // Campos del formulario
                gbc.gridx = 0; gbc.gridy = 0;
                dialog.add(new JLabel("Nombre:"), gbc);
                JTextField txtNombre = new JTextField(rs.getString("nombre"), 20);
                gbc.gridx = 1;
                dialog.add(txtNombre, gbc);

                gbc.gridx = 0; gbc.gridy = 1;
                dialog.add(new JLabel("Apellido:"), gbc);
                JTextField txtApellido = new JTextField(rs.getString("apellido"), 20);
                gbc.gridx = 1;
                dialog.add(txtApellido, gbc);

                gbc.gridx = 0; gbc.gridy = 2;
                dialog.add(new JLabel("Email:"), gbc);
                JTextField txtEmail = new JTextField(rs.getString("correoElectronico"), 20);
                gbc.gridx = 1;
                dialog.add(txtEmail, gbc);

                gbc.gridx = 0; gbc.gridy = 3;
                dialog.add(new JLabel("Rol:"), gbc);
                JComboBox<String> comboRol = new JComboBox<>(new String[]{"ALUMNO", "PROFESOR", "ADMINISTRADOR"});
                comboRol.setSelectedItem(rs.getString("rol"));
                gbc.gridx = 1;
                dialog.add(comboRol, gbc);

                // Botones
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                JButton btnGuardar = new JButton("Guardar");
                JButton btnCancelar = new JButton("Cancelar");

                btnGuardar.addActionListener(e -> {
                    String nombre = txtNombre.getText().trim();
                    String apellido = txtApellido.getText().trim();
                    String email = txtEmail.getText().trim();
                    String rol = (String) comboRol.getSelectedItem();

                    if (validarDatosBasicos(nombre, apellido, email, rol)) {
                        actualizarUsuario(
                                id,
                                nombre,
                                apellido,
                                email,
                                rol
                        );
                        dialog.dispose();

                        // Buscar el panel principal de forma segura
                        SwingUtilities.invokeLater(() -> {
                            Container parent = dialog.getParent();
                            while (parent != null && !(parent instanceof JPanel)) {
                                parent = parent.getParent();
                            }
                            if (parent instanceof JPanel) {
                                mostrarGestionUsuarios((JPanel)parent);
                            } else {
                                // Si no se encuentra el panel, actualizar solo la tabla
                                actualizarTablaUsuarios(modeloTablaUsuarios);
                            }
                        });
                    }
                });

                btnCancelar.addActionListener(e -> dialog.dispose());

                buttonPanel.add(btnGuardar);
                buttonPanel.add(btnCancelar);

                gbc.gridx = 0; gbc.gridy = 4;
                gbc.gridwidth = 2;
                dialog.add(buttonPanel, gbc);

                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            System.out.println("Error al editar usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al cargar datos del usuario");
        }
    }

    private void eliminarUsuario(int id) {
        if (JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar este usuario?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try (Connection conexion = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

                conexion.setAutoCommit(false);
                try {
                    // Primero obtener el rol del usuario
                    String sqlRol = "SELECT rol FROM USUARIO WHERE id = ?";
                    PreparedStatement stmtRol = conexion.prepareStatement(sqlRol);
                    stmtRol.setInt(1, id);
                    ResultSet rs = stmtRol.executeQuery();

                    if (rs.next()) {
                        String rol = rs.getString("rol");

                        // Eliminar las asignaciones de puntos relacionadas al usuario
                        if (rol.equals("PROFESOR")) {
                            eliminarAsignacionesPuntosDeProfesor(id, conexion);
                        } else if (rol.equals("ALUMNO")) {
                            eliminarAsignacionesPuntosDeAlumno(id, conexion);
                        }

                        // Eliminar las asignaciones del usuario a las asignaturas
                        eliminarAsignacionesUsuarioDeAsignatura(id, rol, conexion);

                        // Eliminar de la tabla específica según el rol
                        String sqlRolDelete = "";
                        switch (rol) {
                            case "ALUMNO":
                                sqlRolDelete = "DELETE FROM ALUMNO WHERE id = ?";
                                break;
                            case "PROFESOR":
                                sqlRolDelete = "DELETE FROM PROFESOR WHERE id = ?";
                                break;
                            case "ADMINISTRADOR":
                                sqlRolDelete = "DELETE FROM ADMINISTRADOR WHERE id = ?";
                                break;
                        }

                        PreparedStatement stmtRolDelete = conexion.prepareStatement(sqlRolDelete);
                        stmtRolDelete.setInt(1, id);
                        stmtRolDelete.executeUpdate();

                        // Finalmente eliminar de la tabla USUARIO
                        String sqlUser = "DELETE FROM USUARIO WHERE id = ?";
                        PreparedStatement stmtUser = conexion.prepareStatement(sqlUser);
                        stmtUser.setInt(1, id);
                        stmtUser.executeUpdate();

                        conexion.commit();
                        JOptionPane.showMessageDialog(null, "Usuario eliminado exitosamente");
                    }
                } catch (SQLException e) {
                    conexion.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                System.out.println("Error al eliminar usuario: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error al eliminar el usuario");
            }
        }
    }

    private void eliminarAsignacionesPuntosDeProfesor(int profesorId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM ASIGNACION_PUNTOS WHERE profesorID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, profesorId);
        stmt.executeUpdate();
    }

    private void eliminarAsignacionesPuntosDeAlumno(int alumnoId, Connection conexion) throws SQLException {
        String sql = "DELETE FROM ASIGNACION_PUNTOS WHERE alumnoID = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, alumnoId);
        stmt.executeUpdate();
    }

    private void eliminarAsignacionesUsuarioDeAsignatura(int userId, String rol, Connection conexion) throws SQLException {
        String sqlDelete = "";
        if (rol.equals("PROFESOR")) {
            sqlDelete = "DELETE FROM PROFESOR_ASIGNATURA WHERE profesorID = ?";
        } else if (rol.equals("ALUMNO")) {
            sqlDelete = "DELETE FROM ASIGNACION_PUNTOS WHERE alumnoID = ?";
        }

        PreparedStatement stmt = conexion.prepareStatement(sqlDelete);
        stmt.setInt(1, userId);
        stmt.executeUpdate();
    }

    private void actualizarUsuario(int id, String nombre, String apellido, String email, String rol) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            conexion.setAutoCommit(false);
            try {
                // Actualizar datos básicos del usuario
                String sql = "UPDATE USUARIO SET nombre = ?, apellido = ?, correoElectronico = ?, rol = ? WHERE id = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setString(1, nombre);
                stmt.setString(2, apellido);
                stmt.setString(3, email);
                stmt.setString(4, rol);
                stmt.setInt(5, id);

                stmt.executeUpdate();

                // Si cambió el rol, actualizar las tablas correspondientes
                String sqlRol = "SELECT rol FROM USUARIO WHERE id = ?";
                PreparedStatement stmtRol = conexion.prepareStatement(sqlRol);
                stmtRol.setInt(1, id);
                ResultSet rs = stmtRol.executeQuery();

                if (rs.next() && !rs.getString("rol").equals(rol)) {
                    // Eliminar de la tabla anterior
                    String sqlDelete = "";
                    switch (rs.getString("rol")) {
                        case "ALUMNO":
                            sqlDelete = "DELETE FROM ALUMNO WHERE id = ?";
                            break;
                        case "PROFESOR":
                            sqlDelete = "DELETE FROM PROFESOR WHERE id = ?";
                            break;
                        case "ADMINISTRADOR":
                            sqlDelete = "DELETE FROM ADMINISTRADOR WHERE id = ?";
                            break;
                    }

                    PreparedStatement stmtDelete = conexion.prepareStatement(sqlDelete);
                    stmtDelete.setInt(1, id);
                    stmtDelete.executeUpdate();

                    // Insertar en la nueva tabla
                    String sqlInsert = "";
                    switch (rol) {
                        case "ALUMNO":
                            sqlInsert = "INSERT INTO ALUMNO (id, puntos) VALUES (?, 0)";
                            break;
                        case "PROFESOR":
                            sqlInsert = "INSERT INTO PROFESOR (id) VALUES (?)";
                            break;
                        case "ADMINISTRADOR":
                            sqlInsert = "INSERT INTO ADMINISTRADOR (id) VALUES (?)";
                            break;
                    }

                    PreparedStatement stmtInsert = conexion.prepareStatement(sqlInsert);
                    stmtInsert.setInt(1, id);
                    stmtInsert.executeUpdate();
                }

                conexion.commit();
                JOptionPane.showMessageDialog(null, "Usuario actualizado exitosamente");
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al actualizar el usuario");
        }
    }

    private DefaultTableModel modeloTablaRecompensas;

    private void mostrarFormularioNuevaRecompensa() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Nueva Recompensa");
        dialog.setModal(true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos del formulario
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Nombre:"), gbc);
        JTextField txtNombre = new JTextField(20);
        gbc.gridx = 1;
        dialog.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Descripción:"), gbc);
        JTextArea txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setLineWrap(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        gbc.gridx = 1;
        dialog.add(scrollDescripcion, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Costo (puntos):"), gbc);
        JTextField txtCosto = new JTextField(10);
        gbc.gridx = 1;
        dialog.add(txtCosto, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Stock:"), gbc);
        JTextField txtStock = new JTextField(10);
        gbc.gridx = 1;
        dialog.add(txtStock, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.addActionListener(e -> {
            try {
                if (validarDatosRecompensa(txtNombre.getText(), txtCosto.getText(), txtStock.getText())) {
                    guardarRecompensa(
                            txtNombre.getText(),
                            txtDescripcion.getText(),
                            Integer.parseInt(txtCosto.getText()),
                            Integer.parseInt(txtStock.getText())
                    );
                    dialog.dispose();

                    // Actualizar la tabla de recompensas en el panel principal
                    SwingUtilities.invokeLater(() -> {
                        JPanel mainPanel = (JPanel) SwingUtilities.getAncestorOfClass(JPanel.class, dialog.getParent());
                        if (mainPanel != null) {
                            mostrarGestionRecompensas(mainPanel);
                        }
                    });
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Los valores de costo y stock deben ser números válidos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    // Método para actualizar la tabla de recompensas
    private void actualizarTablaRecompensas(DefaultTableModel model) {
        model.setRowCount(0);
        Object[][] datos = obtenerRecompensas();
        for (Object[] fila : datos) {
            model.addRow(fila);
        }
    }



    private boolean validarDatosRecompensa(String nombre, String costo, String stock) {
        if (nombre.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El nombre es obligatorio");
            return false;
        }
        try {
            int costoInt = Integer.parseInt(costo);
            int stockInt = Integer.parseInt(stock);
            if (costoInt <= 0 || stockInt < 0) {
                JOptionPane.showMessageDialog(null, "El costo debe ser mayor a 0 y el stock no puede ser negativo");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Costo y stock deben ser números válidos");
            return false;
        }
        return true;
    }

    private void guardarRecompensa(String nombre, String descripcion, int costo, int stock) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "INSERT INTO RECOMPENSA (nombre, descripcion, costoPuntos, stock) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setInt(3, costo);
            stmt.setInt(4, stock);

            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(null, "Recompensa guardada exitosamente");
            } else {
                JOptionPane.showMessageDialog(null, "Error al guardar la recompensa");
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar recompensa: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al guardar la recompensa");
        }
    }

    // Método para editar recompensa
    private void editarRecompensa(int id) {
        try {
            // Obtener la recompensa actual
            Recompensa recompensa = Recompensa.buscarPorId(id);
            if (recompensa == null) {
                JOptionPane.showMessageDialog(null, "No se encontró la recompensa");
                return;
            }

            JDialog dialog = new JDialog();
            dialog.setTitle("Editar Recompensa");
            dialog.setModal(true);
            dialog.setSize(400, 350);
            dialog.setLocationRelativeTo(null);
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Campos del formulario
            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Nombre:"), gbc);
            JTextField txtNombre = new JTextField(recompensa.getNombre(), 20);
            gbc.gridx = 1;
            dialog.add(txtNombre, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("Descripción:"), gbc);
            JTextArea txtDescripcion = new JTextArea(recompensa.getDescripcion(), 4, 20);
            txtDescripcion.setLineWrap(true);
            JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
            gbc.gridx = 1;
            dialog.add(scrollDescripcion, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            dialog.add(new JLabel("Costo (puntos):"), gbc);
            JTextField txtCosto = new JTextField(String.valueOf(recompensa.getCostoPuntos()), 10);
            gbc.gridx = 1;
            dialog.add(txtCosto, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            dialog.add(new JLabel("Stock:"), gbc);
            JTextField txtStock = new JTextField(String.valueOf(recompensa.getStock()), 10);
            gbc.gridx = 1;
            dialog.add(txtStock, gbc);

            // Botones
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton btnGuardar = new JButton("Guardar");
            JButton btnCancelar = new JButton("Cancelar");

            btnGuardar.addActionListener(e -> {
                try {
                    if (validarDatosRecompensa(txtNombre.getText(), txtCosto.getText(), txtStock.getText())) {
                        recompensa.setNombre(txtNombre.getText());
                        recompensa.setDescripcion(txtDescripcion.getText());
                        recompensa.setCostoPuntos(Integer.parseInt(txtCosto.getText()));
                        recompensa.setStock(Integer.parseInt(txtStock.getText()));

                        if (recompensa.guardar()) {
                            dialog.dispose();
                            actualizarTablaRecompensas(modeloTablaRecompensas);
                            JOptionPane.showMessageDialog(null, "Recompensa actualizada exitosamente");
                        } else {
                            JOptionPane.showMessageDialog(null, "Error al actualizar la recompensa");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Los valores de costo y stock deben ser números válidos");
                }
            });

            btnCancelar.addActionListener(e -> dialog.dispose());

            buttonPanel.add(btnGuardar);
            buttonPanel.add(btnCancelar);

            gbc.gridx = 0; gbc.gridy = 4;
            gbc.gridwidth = 2;
            dialog.add(buttonPanel, gbc);

            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la recompensa: " + e.getMessage());
        }
    }


}

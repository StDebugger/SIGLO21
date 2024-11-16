/*realizo todos los importos para poder utilizar una interfaz grafica de los formularios
de registro y de inicio de sesion*/
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Usuario {

    public static void main(String[] args) {
        Usuario usuario = new Usuario();
        usuario.Inicio();
    }

    int id;
    String nombre;
    String apellido;
    String correoElectronico;
    String nombreUsuario;
    String contraseña;

    // Constructor con parámetros
    public Usuario () {} //constructor vacío
    public Usuario (
            int id,
            String nombre,
            String apellido,
            String correoElectronico,
            String nombreUsuario,
            String contraseña
            ) {

        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correoElectronico = correoElectronico;
        this.nombreUsuario = nombreUsuario;
        this.contraseña = contraseña;
    }

    public Usuario findUserByNombreUsuario(String nombreUsuario) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        Usuario usuario = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver JDBC: " + e.getMessage());
            return null;
        }

        try {
            System.out.println("Intentando conectar a la base de datos..."); // Debug
            Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword);
            System.out.println("Conexión establecida exitosamente"); // Debug

            String sql = "SELECT id, nombre, apellido, correoElectronico, nombreUsuario, contrasena, rol FROM USUARIO WHERE nombreUsuario = ?";

            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombreUsuario);
            System.out.println("Buscando usuario con nombreUsuario: " + nombreUsuario); // Debug

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                usuario = new Usuario(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("correoElectronico"),
                        resultSet.getString("nombreUsuario"),
                        resultSet.getString("contrasena")
                );
                System.out.println("Usuario encontrado: " + usuario.nombre + " " + usuario.apellido); // Debug
                System.out.println("Rol del usuario: " + resultSet.getString("rol")); // Debug
            } else {
                System.out.println("No se encontró ningún usuario con ese nombre de usuario"); // Debug
            }

            resultSet.close();
            statement.close();
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error de SQL: " + e.getMessage());
            e.printStackTrace();
        }
        return usuario;
    }

    //verificar coincidencia de usuario y su clave
    public boolean checkPassword(Usuario usuario, String password) {
        System.out.println("Verificando contraseña..."); // Debug
        System.out.println("Contraseña ingresada: " + password); // Debug
        System.out.println("Contraseña almacenada: " + usuario.contraseña); // Debug
        return usuario.contraseña.equals(password);
    }

//    public Usuario registerUser(String nombreUsuario){
//
//        // Parámetros de conexión
//        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
//        String dbUser = "root";
//        String dbPassword = "123456";
//
//        Usuario usuario = null;
//
//        // Cargar el driver JDBC
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            System.out.println("Error al cargar el driver JDBC: " + e.getMessage());
//        }
//
//
//        return usuario;
//    }

    public void Inicio() {
        // Ventana principal
        JFrame frame = new JFrame("SIROE - Sistema de Control de Registros");
        frame.setSize(500, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);  // Centrar ventana

        // Etiqueta de bienvenida con fuente más grande y negrita
        JLabel welcomeLabel = new JLabel("¡BIENVENIDO A SIROE!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBounds(100, 40, 300, 35);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(welcomeLabel);

        // Panel para los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(50, 120, 400, 150);
        buttonPanel.setLayout(null);

        // Botón de inicio de sesión
        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setBounds(210, 20, 170, 35);
        loginButton.setFont(new Font("Arial", Font.PLAIN, 14));
        buttonPanel.add(loginButton);

        // Botón de registro
        JButton singUpButton = new JButton("Registrarse");
        singUpButton.setBounds(20, 20, 170, 35);
        singUpButton.setFont(new Font("Arial", Font.PLAIN, 14));
        buttonPanel.add(singUpButton);

        frame.add(buttonPanel);

        // Eventos de los botones
        loginButton.addActionListener(e -> inicioDeSesion());
        singUpButton.addActionListener(e -> RegistrarUsuario());

        frame.setVisible(true);
    }

    public void RegistrarUsuario() {
        // Ventana de registro
        JFrame frame = new JFrame("SICORE - Registro de Usuario");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Título del formulario
        JLabel titleLabel = new JLabel("Registro de Usuario");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(100, 20, 200, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(titleLabel);

        // Array de etiquetas para crear campos uniformemente
        String[] labels = {"Nombre:", "Apellido:", "E-mail:", "Nombre de Usuario:", "Contraseña:"};
        JTextField[] textFields = new JTextField[5];
        int startY = 80;
        int spacing = 60;

        for (int i = 0; i < labels.length; i++) {
            // Crear y añadir etiqueta
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            label.setBounds(40, startY + (i * spacing), 120, 25);
            frame.add(label);

            // Crear y añadir campo de texto
            textFields[i] = (i == 4) ? new JPasswordField(20) : new JTextField(20);
            textFields[i].setBounds(160, startY + (i * spacing), 180, 25);
            frame.add(textFields[i]);
        }

        // Botón de registro
        JButton registerButton = new JButton("Registrar Usuario");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setBounds(100, 380, 200, 35);
        frame.add(registerButton);

        // Evento del botón de registro
        registerButton.addActionListener(e -> {
            // Verificación de campos vacíos
            for (JTextField field : textFields) {
                if (field.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame,
                            "Por favor, complete todos los campos",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Verificar email
            if (!textFields[2].getText().contains("@") || !textFields[2].getText().contains(".")) {
                JOptionPane.showMessageDialog(frame,
                        "Por favor, ingrese un email válido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtener los valores de los campos
            String nombre = textFields[0].getText().trim();
            String apellido = textFields[1].getText().trim();
            String email = textFields[2].getText().trim();
            String nombreUsuario = textFields[3].getText().trim();
            String password = textFields[4].getText().trim();

            // Verificar si el usuario ya existe
            if (usuarioExiste(nombreUsuario, email)) {
                JOptionPane.showMessageDialog(frame,
                        "El nombre de usuario o email ya está registrado",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Si todo está correcto, intentar registrar el usuario
            if (registrarNuevoUsuario(nombre, apellido, email, nombreUsuario, password)) {
                JOptionPane.showMessageDialog(frame,
                        "Registro exitoso",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);

                // Limpiar campos
                for (JTextField field : textFields) {
                    field.setText("");
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Error al registrar el usuario. Por favor, intente nuevamente.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    // Verificar si el usuario ya existe
    private boolean usuarioExiste(String nombreUsuario, String email) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "SELECT COUNT(*) FROM USUARIO WHERE nombreUsuario = ? OR correoElectronico = ?";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombreUsuario);
            statement.setString(2, email);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar usuario existente: " + e.getMessage());
        }
        return false;
    }

    // Registrar un nuevo usuario
    private boolean registrarNuevoUsuario(String nombre, String apellido, String email,
                                          String nombreUsuario, String password) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "INSERT INTO USUARIO (nombre, apellido, correoElectronico, nombreUsuario, contrasena, rol) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, email);
            statement.setString(4, nombreUsuario);
            statement.setString(5, password);
            statement.setString(6, "ALUMNO"); // Por defecto, registramos como ALUMNO

            int filasAfectadas = statement.executeUpdate();
            return filasAfectadas > 0;


        } catch (SQLException e) {
            System.out.println("Error al registrar nuevo usuario: " + e.getMessage());
            return false;
        }
    }

    public void inicioDeSesion() {

        // Ventana de inicio de sesión
        JFrame frame = new JFrame("SIROE - Inicio de Sesión");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);

        // Título
        JLabel titleLabel = new JLabel("Inicio de Sesión");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(100, 20, 200, 30);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(titleLabel);

        // Campo de usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setBounds(50, 80, 100, 25);
        frame.add(userLabel);

        JTextField userText = new JTextField();
        userText.setBounds(150, 80, 180, 25);
        frame.add(userText);

        // Campo de contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setBounds(50, 120, 100, 25);
        frame.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField();
        passwordText.setBounds(150, 120, 180, 25);
        frame.add(passwordText);

        // Botón de inicio de sesión
        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBounds(100, 180, 200, 35);
        frame.add(loginButton);

        // Evento del botón de inicio de sesión
        loginButton.addActionListener(e -> {
            String user = userText.getText();
            String password = new String(passwordText.getPassword());

            System.out.println("Intentando login con usuario: " + user); // Debug
            Usuario dbUser = findUserByNombreUsuario(user);

            if (dbUser != null && dbUser.checkPassword(dbUser, password)) {
                frame.dispose();

                try (Connection conexion = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

                    String sql = "SELECT rol FROM USUARIO WHERE nombreUsuario = ?";
                    PreparedStatement stmt = conexion.prepareStatement(sql);
                    stmt.setString(1, user);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String rol = rs.getString("rol");
                        System.out.println("Rol del usuario: " + rol); // Debug

                        switch (rol) {
                            case "ADMINISTRADOR":
                                System.out.println("Intentando cargar panel de administrador"); // Debug
                                Administrador admin = Administrador.findAdminByNombreUsuario(user);
                                if (admin != null) {
                                    System.out.println("Administrador encontrado, mostrando panel"); // Debug
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            admin.mostrarPanelAdmin();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            JOptionPane.showMessageDialog(null,
                                                    "Error al mostrar panel de administrador: " + ex.getMessage());
                                        }
                                    });
                                } else {
                                    System.out.println("No se encontró el administrador en la tabla ADMINISTRADOR"); // Debug
                                    JOptionPane.showMessageDialog(null, "Error: No se encontró el registro de administrador");
                                }
                                break;

                            case "ALUMNO":
                                System.out.println("Intentando cargar panel de alumno"); // Debug
                                Alumno alumno = Alumno.findAlumnoByNombreUsuario(user);
                                if (alumno != null) {
                                    System.out.println("Alumno encontrado, mostrando panel"); // Debug
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            alumno.mostrarPanelAlumno();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            JOptionPane.showMessageDialog(null,
                                                    "Error al mostrar panel de alumno: " + ex.getMessage());
                                        }
                                    });
                                } else {
                                    System.out.println("No se encontró el alumno en la tabla ALUMNO"); // Debug
                                    JOptionPane.showMessageDialog(null, "Error: No se encontró el registro de alumno");
                                }
                                break;

                            case "PROFESOR":
                                System.out.println("Intentando cargar panel de profesor"); // Debug
                                Profesor profesor = Profesor.findProfesorByNombreUsuario(user);
                                if (profesor != null) {
                                    System.out.println("Profesor encontrado, mostrando panel"); // Debug
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            profesor.mostrarPanelProfesor();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            JOptionPane.showMessageDialog(null,
                                                    "Error al mostrar panel de profesor: " + ex.getMessage());
                                        }
                                    });
                                } else {
                                    System.out.println("No se encontró el profesor en la tabla PROFESOR"); // Debug
                                    JOptionPane.showMessageDialog(null, "Error: No se encontró el registro de profesor");
                                }
                                break;
                        }
                    }
                } catch (SQLException ex) {
                    System.out.println("Error SQL: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al cargar el panel de usuario");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Credenciales incorrectas");
            }
        });
        frame.setVisible(true);
    }
}

/*realizo todos los importos para poder utilizar una interfaz grafica de los formularios
de registro y de inicio de sesion*/

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Usuario {

    int id;
    String nombre;
    String apellido;
    String CorreoElectronico;
    String nombreUsuario;
    String contraseña;


    public void Inicio() {

        // Crear el marco (ventana)
        JFrame frame = new JFrame("Inicio");
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);  // Desactivar layout automático

        // Etiqueta de bienvenida
        JLabel welcomeLabel = new JLabel("BIENVENIDO A SICORE!");
        welcomeLabel.setBounds(175, 10, 200, 25);
        frame.add(welcomeLabel);


        // Botón de inicio de sesión
        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setBounds(265, 110, 150, 25);
        frame.add(loginButton);

        // Acción del botón de inicio de sesión
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inicioDeSesion();
            }
        });
        frame.setVisible(true);
    }

    public void inicioDeSesion() {

        // Crear el marco (ventana)
        JFrame frame = new JFrame("Formulario de Inicio de Sesión");
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);  // Desactivar layout automático

        // Etiqueta para el nombre de usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setBounds(30, 30, 100, 25);
        frame.add(userLabel);

        // Campo de texto para el nombre de usuario
        JTextField userText = new JTextField(20);
        userText.setBounds(140, 30, 150, 25);
        frame.add(userText);

        // Etiqueta para la contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setBounds(30, 70, 100, 25);
        frame.add(passwordLabel);

        // Campo de contraseña
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(140, 70, 150, 25);
        frame.add(passwordText);

        // Botón de inicio de sesión
        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setBounds(140, 110, 150, 25);
        frame.add(loginButton);

        // Acción del botón de inicio de sesión
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = userText.getText();
                String password = new String(passwordText.getPassword());

                // Verificar credenciales
                if (user.equals("admin") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(null, "Benvenido Administrador");
                } else {
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas");
                }
                if (user.equals("teacher1") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(null, "Bienvenido Profesor 1");
                } else {
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas");
                }
                if (user.equals("student1") && password.equals("1234")) {
                    JOptionPane.showMessageDialog(null, "Bienvenido Estudiante 1");
                } else {
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas");
                }
            }
        });

        // Hacer visible el marco
        frame.setVisible(true);
    }

    public void cerrarSesion() {
    }

    public void recuperarContraseña() {
    }
}

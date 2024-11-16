package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Alumno extends Usuario {
    private int puntos;
    private ArrayList<String> historialPuntos;

    // Constructor vacío
    public Alumno() {
        super();
        this.puntos = 0;
        this.historialPuntos = new ArrayList<>();
    }

    // Constructor completo
    public Alumno(int id, String nombre, String apellido, String correoElectronico,
                  String nombreUsuario, String contraseña, int puntos) {
        super(id, nombre, apellido, correoElectronico, nombreUsuario, contraseña);
        this.puntos = puntos;
        this.historialPuntos = new ArrayList<>();
    }

    // Buscar alumno por nombre de usuario
    public static Alumno findAlumnoByNombreUsuario(String nombreUsuario) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        Alumno alumno = null;

        try {
            Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword);
            String sql = "SELECT u.*, a.puntos FROM USUARIO u " +
                    "INNER JOIN ALUMNO a ON u.id = a.id " +
                    "WHERE u.nombreUsuario = ? AND u.rol = 'ALUMNO'";

            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombreUsuario);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                alumno = new Alumno(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("correoElectronico"),
                        rs.getString("nombreUsuario"),
                        rs.getString("contrasena"),
                        rs.getInt("puntos")
                );
            }

            rs.close();
            statement.close();
            conexion.close();
        } catch (SQLException e) {
            System.out.println("Error al buscar alumno: " + e.getMessage());
        }
        return alumno;
    }

    // Mostrar panel del alumno
    public void mostrarPanelAlumno() {
        JFrame frame = new JFrame("SIROE - Panel de Alumno");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Panel superior
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JLabel lblNombre = new JLabel("Alumno: " + this.nombre + " " + this.apellido);
        JLabel lblPuntos = new JLabel("Puntos: " + this.puntos);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblPuntos.setFont(new Font("Arial", Font.BOLD, 14));

        topPanel.add(lblNombre);
        topPanel.add(lblPuntos);

        // Panel central con botones
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnVerHistorial = new JButton("Ver Historial de Puntos");
        JButton btnMercado = new JButton("Mercado de Puntos");
        JButton btnMisRecompensas = new JButton("Mis Canjes");
        JButton btnCerrarSesion = new JButton("Cerrar Sesión");

        centerPanel.add(btnVerHistorial);
        centerPanel.add(btnMercado);
        centerPanel.add(btnMisRecompensas);
        centerPanel.add(btnCerrarSesion);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Eventos de botones
        btnVerHistorial.addActionListener(e -> mostrarHistorialPuntos());
        btnMercado.addActionListener(e -> mostrarVentanaCanje());
        btnMisRecompensas.addActionListener(e -> mostrarMisCanjes());
        btnCerrarSesion.addActionListener(e -> frame.dispose());

        frame.setVisible(true);
    }

    // Método para mostrar las recompensas del alumno
    private void mostrarMisCanjes() {
        JFrame frame = new JFrame("Mis Canjes");
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel superior con información del alumno
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("Historial de Canjes Realizados - Alumno: " + this.nombre + " " + this.apellido);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(lblInfo);

        // Crear la tabla con las columnas necesarias
        String[] columnas = {"Fecha", "Recompensa", "Puntos Canjeados", "Descripción"};
        DefaultTableModel model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Debug: Mostrar información del alumno actual
        System.out.println("=== Debug Info ===");
        System.out.println("ID del alumno: " + this.id);
        System.out.println("Nombre del alumno: " + this.nombre + " " + this.apellido);

        // Cargar los datos de las transacciones
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Primero verificar si hay transacciones
            String sqlVerificar = "SELECT COUNT(*) as total FROM TRANSACCION WHERE alumnoID = ?";
            PreparedStatement stmtVerificar = conexion.prepareStatement(sqlVerificar);
            stmtVerificar.setInt(1, this.id);
            ResultSet rsVerificar = stmtVerificar.executeQuery();

            if (rsVerificar.next()) {
                int totalTransacciones = rsVerificar.getInt("total");
                System.out.println("Total de transacciones encontradas: " + totalTransacciones);
            }

            // Obtener los canjes
            String sql = "SELECT t.fecha, r.nombre as recompensa, r.costoPuntos, t.descripcion " +
                    "FROM TRANSACCION t " +
                    "INNER JOIN RECOMPENSA r ON t.recompensaID = r.id " +
                    "WHERE t.alumnoID = ? " +
                    "ORDER BY t.fecha DESC";

            System.out.println("Ejecutando consulta SQL: " + sql);

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            int contadorRegistros = 0;
            while (rs.next()) {
                contadorRegistros++;
                model.addRow(new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getString("recompensa"),
                        rs.getInt("costoPuntos"),
                        rs.getString("descripcion")
                });
            }

            System.out.println("Registros agregados a la tabla: " + contadorRegistros);

            if (contadorRegistros == 0) {
                model.addRow(new Object[]{"No hay canjes realizados", "", "", ""});
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar canjes: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Error al cargar el historial de canjes: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        // Panel de resumen
        JPanel resumenPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        resumenPanel.setBorder(BorderFactory.createTitledBorder("Resumen de Canjes"));

        // Obtener estadísticas
        int totalCanjes = obtenerTotalCanjes();
        int puntosGastados = obtenerTotalPuntosGastados();

        resumenPanel.add(new JLabel("Total de Canjes Realizados: " + totalCanjes));
        resumenPanel.add(new JLabel("Total de Puntos Canjeados: " + puntosGastados));

        // Agregar los componentes al frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(resumenPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Método para cargar las transacciones del alumno
    private void cargarTransaccionesRecompensas(DefaultTableModel model) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT t.fecha, r.nombre as recompensa, r.costoPuntos, " +
                    "t.descripcion, t.tipo " +
                    "FROM TRANSACCION t " +
                    "INNER JOIN RECOMPENSA r ON t.recompensaID = r.id " +
                    "WHERE t.alumnoID = ? " +
                    "ORDER BY t.fecha DESC";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getString("recompensa"),
                        rs.getInt("costoPuntos"),
                        rs.getString("descripcion"),
                        rs.getString("tipo")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar transacciones: " + e.getMessage());
        }
    }

    // Método para obtener el total de canjes
    private int obtenerTotalCanjes() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT COUNT(*) as total FROM TRANSACCION WHERE alumnoID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener total de canjes: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Método para obtener el total de puntos gastados
    private int obtenerTotalPuntosGastados() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT SUM(r.costoPuntos) as total " +
                    "FROM TRANSACCION t " +
                    "INNER JOIN RECOMPENSA r ON t.recompensaID = r.id " +
                    "WHERE t.alumnoID = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener total de puntos gastados: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // Método para mostrar el historial de puntos
    private void mostrarHistorialPuntos() {
        JFrame frame = new JFrame("Historial de Puntos");
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        String[] columnas = {"Fecha", "Puntos", "Descripción", "Profesor"};
        Object[][] datos = obtenerHistorialPuntos();

        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        frame.add(scrollPane);
        frame.setVisible(true);
    }

    // Método para obtener el historial de puntos de la BD
    private Object[][] obtenerHistorialPuntos() {
        ArrayList<Object[]> historial = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "SELECT ap.fecha, ap.puntos, ap.comentario, " +
                    "CONCAT(u.nombre, ' ', u.apellido) as profesor " +
                    "FROM ASIGNACION_PUNTOS ap " +
                    "INNER JOIN PROFESOR p ON ap.profesorID = p.id " +
                    "INNER JOIN USUARIO u ON p.id = u.id " +
                    "WHERE ap.alumnoID = ? " +
                    "ORDER BY ap.fecha DESC";

            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                historial.add(new Object[]{
                        rs.getTimestamp("fecha"),
                        rs.getInt("puntos"),
                        rs.getString("comentario"),
                        rs.getString("profesor")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener historial: " + e.getMessage());
        }

        return historial.toArray(new Object[0][]);
    }

    // Método para mostrar ventana de canje
    private void mostrarVentanaCanje() {
        JFrame frame = new JFrame("Canjear Puntos");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(10, 10));

        JLabel lblPuntos = new JLabel("Puntos disponibles: " + this.puntos);
        lblPuntos.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(lblPuntos, BorderLayout.NORTH);

        // Tabla de recompensas
        String[] columnas = {"Recompensa", "Costo en Puntos", "Stock"};
        Object[][] datos = obtenerRecompensasDisponibles();
        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton btnCanjear = new JButton("Canjear Seleccionado");
        frame.add(btnCanjear, BorderLayout.SOUTH);

        btnCanjear.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                realizarCanje(datos[fila]);
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Seleccione una recompensa");
            }
        });

        frame.setVisible(true);
    }

    // Método para obtener las recompensas disponibles
    private Object[][] obtenerRecompensasDisponibles() {
        ArrayList<Object[]> recompensas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT nombre, costoPuntos, stock FROM RECOMPENSA WHERE stock > 0";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                recompensas.add(new Object[]{
                        rs.getString("nombre"),
                        rs.getInt("costoPuntos"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener recompensas: " + e.getMessage());
        }
        return recompensas.toArray(new Object[0][]);
    }

    // Método para realizar el canje
    private void realizarCanje(Object[] recompensa) {
        String nombreRecompensa = (String) recompensa[0];
        int costo = (int) recompensa[1];

        if (this.puntos >= costo) {
            try (Connection conexion = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

                // Iniciar transacción
                conexion.setAutoCommit(false);
                try {
                    // Obtener el ID de la recompensa
                    String sqlRecompensa = "SELECT id FROM RECOMPENSA WHERE nombre = ?";
                    PreparedStatement stmtRecompensa = conexion.prepareStatement(sqlRecompensa);
                    stmtRecompensa.setString(1, nombreRecompensa);
                    ResultSet rsRecompensa = stmtRecompensa.executeQuery();

                    if (!rsRecompensa.next()) {
                        throw new SQLException("No se encontró la recompensa");
                    }
                    int recompensaId = rsRecompensa.getInt("id");

                    System.out.println("Iniciando canje para:");
                    System.out.println("AlumnoID: " + this.id);
                    System.out.println("RecompensaID: " + recompensaId);
                    System.out.println("Costo: " + costo);

                    // Actualizar puntos del alumno
                    String sqlPuntos = "UPDATE ALUMNO SET puntos = puntos - ? WHERE id = ?";
                    PreparedStatement stmtPuntos = conexion.prepareStatement(sqlPuntos);
                    stmtPuntos.setInt(1, costo);
                    stmtPuntos.setInt(2, this.id);
                    stmtPuntos.executeUpdate();

                    // Actualizar stock de recompensa
                    String sqlStock = "UPDATE RECOMPENSA SET stock = stock - 1 WHERE id = ?";
                    PreparedStatement stmtStock = conexion.prepareStatement(sqlStock);
                    stmtStock.setInt(1, recompensaId);
                    stmtStock.executeUpdate();

                    // Registrar la transacción
                    String sqlTransaccion = "INSERT INTO TRANSACCION (alumnoID, recompensaID, fecha, tipo, descripcion) " +
                            "VALUES (?, ?, NOW(), 'CANJE', ?)";
                    PreparedStatement stmtTransaccion = conexion.prepareStatement(sqlTransaccion);
                    stmtTransaccion.setInt(1, this.id);
                    stmtTransaccion.setInt(2, recompensaId);
                    stmtTransaccion.setString(3, "Canje de recompensa: " + nombreRecompensa);
                    stmtTransaccion.executeUpdate();

                    conexion.commit();
                    this.puntos -= costo;
                    JOptionPane.showMessageDialog(null, "Canje realizado con éxito");

                    System.out.println("Canje completado exitosamente");
                } catch (SQLException e) {
                    conexion.rollback();
                    System.out.println("Error durante el canje: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Error al realizar el canje: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                System.out.println("Error en la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Puntos insuficientes");
        }
    }

    // Método para mostrar recompensas disponibles
    private void mostrarRecompensasDisponibles() {
        JFrame frame = new JFrame("Recompensas Disponibles");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        String[] columnas = {"Recompensa", "Costo en Puntos", "Stock"};
        Object[][] datos = obtenerRecompensasDisponibles();
        JTable tabla = new JTable(datos, columnas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        frame.add(scrollPane);
        frame.setVisible(true);
    }

    // Getters y setters específicos
    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public ArrayList<String> getHistorialPuntos() {
        return historialPuntos;
    }
}
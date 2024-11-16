package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class Recompensa {
    private static JTable tabla;
    private static DefaultTableModel modeloTablaRecompensas;
    private int id;
    private String nombre;
    private String descripcion;
    private int costoPuntos;
    private int stock;

    // Constructor vacío
    public Recompensa() {}

    // Constructor completo
    public Recompensa(int id, String nombre, String descripcion, int costoPuntos, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPuntos = costoPuntos;
        this.stock = stock;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getCostoPuntos() { return costoPuntos; }
    public void setCostoPuntos(int costoPuntos) { this.costoPuntos = costoPuntos; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    // Método para guardar la recompensa
    public boolean guardar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql;
            if (this.id == 0) {
                sql = "INSERT INTO RECOMPENSA (nombre, descripcion, costoPuntos, stock) " +
                        "VALUES (?, ?, ?, ?)";
            } else {
                sql = "UPDATE RECOMPENSA SET nombre = ?, descripcion = ?, " +
                        "costoPuntos = ?, stock = ? WHERE id = ?";
            }

            PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, nombre);
            stmt.setString(2, descripcion);
            stmt.setInt(3, costoPuntos);
            stmt.setInt(4, stock);

            if (this.id != 0) {
                stmt.setInt(5, id);
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
            System.out.println("Error al guardar recompensa: " + e.getMessage());
            return false;
        }
    }

    // Método para eliminar una recompensa
    public boolean eliminar() {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {
            String sql = "DELETE FROM RECOMPENSA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, this.id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar recompensa: " + e.getMessage());
            return false;
        }
    }

    // Método para buscar una recompensa por ID
    public static Recompensa buscarPorId(int id) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM RECOMPENSA WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Recompensa(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("costoPuntos"),
                        rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar recompensa: " + e.getMessage());
        }
        return null;
    }

    // Método para obtener todas las recompensas
    public static ArrayList<Recompensa> obtenerTodas() {
        ArrayList<Recompensa> recompensas = new ArrayList<>();
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT * FROM RECOMPENSA ORDER BY nombre";
            Statement stmt = conexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                recompensas.add(new Recompensa(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getInt("costoPuntos"),
                        rs.getInt("stock")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener recompensas: " + e.getMessage());
        }
        return recompensas;
    }

    // Método para realizar el canje de una recompensa
    public boolean canjearPorAlumno(int alumnoId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            // Iniciar transacción
            conexion.setAutoCommit(false);
            try {
                // Verificar stock y puntos del alumno
                if (!verificarDisponibilidad(alumnoId, conexion)) {
                    throw new SQLException("No hay suficientes puntos o stock disponible");
                }

                // Actualizar stock de recompensa
                String sql = "UPDATE RECOMPENSA SET stock = stock - 1 WHERE id = ?";
                PreparedStatement stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, this.id);
                stmt.executeUpdate();

                // Descontar puntos al alumno
                sql = "UPDATE ALUMNO SET puntos = puntos - ? WHERE id = ?";
                stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, this.costoPuntos);
                stmt.setInt(2, alumnoId);
                stmt.executeUpdate();

                // Registrar transacción
                sql = "INSERT INTO TRANSACCION (alumnoID, recompensaID, tipo, descripcion) " +
                        "VALUES (?, ?, 'CANJE', ?)";
                stmt = conexion.prepareStatement(sql);
                stmt.setInt(1, alumnoId);
                stmt.setInt(2, this.id);
                stmt.setString(3, "Canje de recompensa: " + this.nombre);
                stmt.executeUpdate();

                // Confirmar transacción
                conexion.commit();
                return true;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error al realizar canje: " + e.getMessage());
            return false;
        }
    }

    // Método para verificar disponibilidad de canje
    private boolean verificarDisponibilidad(int alumnoId, Connection conexion) throws SQLException {
        // Verificar stock
        String sql = "SELECT stock FROM RECOMPENSA WHERE id = ?";
        PreparedStatement stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, this.id);
        ResultSet rs = stmt.executeQuery();

        if (!rs.next() || rs.getInt("stock") <= 0) {
            return false;
        }

        // Verificar puntos del alumno
        sql = "SELECT puntos FROM ALUMNO WHERE id = ?";
        stmt = conexion.prepareStatement(sql);
        stmt.setInt(1, alumnoId);
        rs = stmt.executeQuery();

        return rs.next() && rs.getInt("puntos") >= this.costoPuntos;
    }

    // Método para mostrar catálogo de recompensas
    public static void mostrarCatalogo(int alumnoId) {
        JFrame frame = new JFrame("Catálogo de Recompensas");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);

        // Panel superior con información del alumno
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblPuntos = new JLabel("Puntos disponibles: " + obtenerPuntosAlumno(alumnoId));
        topPanel.add(lblPuntos);

        // Tabla de recompensas
        String[] columnas = {"ID", "Nombre", "Descripción", "Costo (puntos)", "Stock"};
        modeloTablaRecompensas = new DefaultTableModel(obtenerDatosTabla(), columnas);
        tabla = new JTable(modeloTablaRecompensas);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNueva = new JButton("Nueva Recompensa");
        JButton btnEditar = new JButton("Editar Recompensa");
        JButton btnEliminar = new JButton("Eliminar Recompensa");
        JButton btnCanjear = new JButton("Canjear Recompensa");
        buttonPanel.add(btnCanjear);
        buttonPanel.add(btnNueva);
        buttonPanel.add(btnEditar);
        buttonPanel.add(btnEliminar);

        // Evento del botón "Eliminar Recompensa"
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila >= 0) {
                int id = (int) tabla.getValueAt(fila, 0);
                if (JOptionPane.showConfirmDialog(frame,
                        "¿Está seguro de que desea eliminar esta recompensa?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Recompensa recompensa = Recompensa.buscarPorId(id);
                    if (recompensa != null && recompensa.eliminar()) {
                        modeloTablaRecompensas.removeRow(fila);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Por favor, seleccione una recompensa para eliminar");
            }
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // Métodos auxiliares
    private static int obtenerPuntosAlumno(int alumnoId) {
        try (Connection conexion = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/siroe_db_prueba", "root", "123456")) {

            String sql = "SELECT puntos FROM ALUMNO WHERE id = ?";
            PreparedStatement stmt = conexion.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("puntos");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener puntos del alumno: " + e.getMessage());
        }
        return 0;
    }

    private static Object[][] obtenerDatosTabla() {
        ArrayList<Recompensa> recompensas = obtenerTodas();
        Object[][] datos = new Object[recompensas.size()][5];

        for (int i = 0; i < recompensas.size(); i++) {
            Recompensa r = recompensas.get(i);
            datos[i] = new Object[]{
                    r.getId(),
                    r.getNombre(),
                    r.getDescripcion(),
                    r.getCostoPuntos(),
                    r.getStock()
            };
        }
        return datos;
    }

    private static void actualizarTabla(JTable tabla) {
        Object[][] nuevosDatos = obtenerDatosTabla();
        ((DefaultTableModel) tabla.getModel()).setDataVector(nuevosDatos,
                new String[]{"ID", "Nombre", "Descripción", "Costo (puntos)", "Stock"});
    }

    private static void realizarCanje(int recompensaId, int alumnoId, JFrame frame) {
        Recompensa recompensa = buscarPorId(recompensaId);
        if (recompensa != null) {
            if (recompensa.canjearPorAlumno(alumnoId)) {
                JOptionPane.showMessageDialog(frame,
                        "¡Canje realizado con éxito!\nPuedes recoger tu recompensa en secretaría.");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "No se pudo realizar el canje. Verifica tus puntos y el stock disponible.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String toString() {
        return "Recompensa{" +
                "nombre='" + nombre + '\'' +
                ", costoPuntos=" + costoPuntos +
                ", stock=" + stock +
                '}';
    }
}
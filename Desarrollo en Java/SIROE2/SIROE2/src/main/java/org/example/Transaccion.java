package org.example;

import java.sql.*;

public class Transaccion {
    private int id;
    private int alumnoId;
    private int recompensaId;
    private Timestamp fecha;
    private TipoTransaccion tipo;
    private String descripcion;

    public enum TipoTransaccion {
        CANJE,
        ASIGNACION
    }

    public Transaccion() {
    }

    public Transaccion(int id, int alumnoId, int recompensaId, Timestamp fecha, TipoTransaccion tipo, String descripcion) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.recompensaId = recompensaId;
        this.fecha = fecha;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public int getAlumnoId() {
        return alumnoId;
    }

    public int getRecompensaId() {
        return recompensaId;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void registrarTransaccion(int alumnoId, int recompensaId, TipoTransaccion tipo, String descripcion) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "INSERT INTO TRANSACCION (alumnoID, recompensaID, fecha, tipo, descripcion) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setInt(1, alumnoId);
            statement.setInt(2, recompensaId);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.setString(4, tipo.name());
            statement.setString(5, descripcion);

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Transacción registrada exitosamente.");
            } else {
                System.out.println("Error al registrar la transacción.");
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar la transacción: " + e.getMessage());
        }
    }

    public Transaccion obtenerTransaccion(int id) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        Transaccion transaccion = null;

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "SELECT id, alumnoID, recompensaID, fecha, tipo, descripcion FROM TRANSACCION WHERE id = ?";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                transaccion = new Transaccion(
                        resultSet.getInt("id"),
                        resultSet.getInt("alumnoID"),
                        resultSet.getInt("recompensaID"),
                        resultSet.getTimestamp("fecha"),
                        TipoTransaccion.valueOf(resultSet.getString("tipo")),
                        resultSet.getString("descripcion")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener la transacción: " + e.getMessage());
        }

        return transaccion;
    }
}

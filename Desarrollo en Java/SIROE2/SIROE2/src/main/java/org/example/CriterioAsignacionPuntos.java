package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CriterioAsignacionPuntos {
    private int id;
    private String nombre;
    private String descripcion;
    private int puntosAsignados;

    public CriterioAsignacionPuntos() {
    }

    public CriterioAsignacionPuntos(int id, String nombre, String descripcion, int puntosAsignados) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.puntosAsignados = puntosAsignados;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getPuntosAsignados() {
        return puntosAsignados;
    }

    public static List<CriterioAsignacionPuntos> obtenerTodosCriterios() {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        List<CriterioAsignacionPuntos> criterios = new ArrayList<>();

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "SELECT id, nombre, descripcion, puntosAsignados FROM CRITERIOS_PUNTOS";
            PreparedStatement statement = conexion.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CriterioAsignacionPuntos criterio = new CriterioAsignacionPuntos(
                        resultSet.getInt("id"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        resultSet.getInt("puntosAsignados")
                );
                criterios.add(criterio);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener los criterios de asignación de puntos: " + e.getMessage());
        }

        return criterios;
    }

    public static void guardarCriterio(String nombre, String descripcion, int puntosAsignados) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "INSERT INTO CRITERIOS_PUNTOS (nombre, descripcion, puntosAsignados) VALUES (?, ?, ?)";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setInt(3, puntosAsignados);

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Criterio de asignación de puntos guardado exitosamente.");
            } else {
                System.out.println("Error al guardar el criterio de asignación de puntos.");
            }
        } catch (SQLException e) {
            System.out.println("Error al guardar el criterio de asignación de puntos: " + e.getMessage());
        }
    }

    public static void actualizarCriterio(int id, String nombre, String descripcion, int puntosAsignados) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "UPDATE CRITERIOS_PUNTOS SET nombre = ?, descripcion = ?, puntosAsignados = ? WHERE id = ?";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setInt(3, puntosAsignados);
            statement.setInt(4, id);

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Criterio de asignación de puntos actualizado exitosamente.");
            } else {
                System.out.println("Error al actualizar el criterio de asignación de puntos.");
            }
        } catch (SQLException e) {
            System.out.println("Error al actualizar el criterio de asignación de puntos: " + e.getMessage());
        }
    }

    public static void eliminarCriterio(int id) {
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String dbUser = "root";
        String dbPassword = "123456";

        try (Connection conexion = DriverManager.getConnection(url, dbUser, dbPassword)) {
            String sql = "DELETE FROM CRITERIOS_PUNTOS WHERE id = ?";
            PreparedStatement statement = conexion.prepareStatement(sql);
            statement.setInt(1, id);

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                System.out.println("Criterio de asignación de puntos eliminado exitosamente.");
            } else {
                System.out.println("Error al eliminar el criterio de asignación de puntos.");
            }
        } catch (SQLException e) {
            System.out.println("Error al eliminar el criterio de asignación de puntos: " + e.getMessage());
        }
    }
}
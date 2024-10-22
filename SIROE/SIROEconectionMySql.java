import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SIROEconectionMySql {

    public static void main(String[] args) {

        // Parámetros de conexión
        String url = "jdbc:mysql://localhost:3306/siroe_db_prueba";
        String usuario = "root";
        String contraseña = "123456";

        // Cargar el driver JDBC
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver JDBC: " + e.getMessage());
            return;
        }

        // Intentar establecer la conexión
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            System.out.println("¡Conexión establecida con éxito!");

            // Aquí puedes realizar operaciones con la base de datos

            // Cerrar la conexión
            conexion.close();
            System.out.println("Conexión cerrada.");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}

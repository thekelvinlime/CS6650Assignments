import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
public class SQLConnectionPool {
    public static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:mysql://assignment2.c8smjzr6a1hx.us-west-2.rds.amazonaws.com:3306/Assignment2");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/Assignment2");
        config.setUsername("root");
        config.setPassword("password");

        // Optional configuration settings (you can adjust these as needed)
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(60); // Set the maximum number of connections in the pool
        config.setAutoCommit(true); // Set auto-commit behavior
        // Add more settings as needed

        return new HikariDataSource(config);
    }
}

import java.sql.*;
public class CheckPrice {
  public static void main(String[] args) throws Exception {
    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/taipeigo?serverTimezone=Asia/Taipei", "root", "password");
    ResultSet rs = conn.createStatement().executeQuery("SELECT ticket_id, ticket_name, adult_price FROM ticket ORDER BY adult_price DESC LIMIT 5");
    while(rs.next()) {
        System.out.println("TICKET: " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getInt(3));
    }
  }
}

package com.example.demo.DbConnectionTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionTest {

    public static void main(String[] args) {
        // application.properties에 있는 정보와 동일하게 입력합니다.
        String url = "jdbc:mysql://127.0.0.1:3306/mydb?serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&useSSL=false";
        String username = "ungha12345";
        String password = "12345";

        System.out.println("데이터베이스 연결을 시도합니다...");

        Connection connection = null;
        try {
            // 1. JDBC 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. 데이터베이스 연결 시도
            connection = DriverManager.getConnection(url, username, password);

            // 3. 결과 출력
            if (connection != null) {
                System.out.println("==========================================");
                System.out.println("축하합니다! 데이터베이스 연결에 성공했습니다!");
                System.out.println("==========================================");
            }

        } catch (ClassNotFoundException e) {
            System.err.println("JDBC 드라이버를 찾을 수 없습니다.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("데이터베이스 연결에 실패했습니다.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
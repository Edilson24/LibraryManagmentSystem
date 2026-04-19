package org.example.librarymanagmentsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    public static Connection obterConexao(){
        String url, user, password;

        try {
            url = "jdbc:mysql://localhost:3306/nome_base_de_dados?useSSL=false&serverTimezone=UTC";
            user = "root";
            password = "root";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Conexão realizada com sucesso!");
            return conn;
        } catch (Exception e) {
            System.err.println("ERRO DE CONEXAO");
            e.printStackTrace();
            return null;
        }
    }

}

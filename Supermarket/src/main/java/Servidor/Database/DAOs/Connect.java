package Servidor.Database.DAOs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    public static Connection connect (String name) throws Exception {
        Connection connect = null;
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            throw e;
        }

        try {
            String connectString = "jdbc:hsqldb:file:db-data/catalog"+name;
            connect = DriverManager.getConnection(connectString, "SA", "");
            } catch (SQLException e){
                throw e;
            }

        return  connect;
    }


    public static void close(Connection c ){
        try{
            if(c != null && !c.isClosed()){
                c.close();
            }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
}


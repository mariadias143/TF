package Servidor;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Catalogo {
    private Connection con;
    // jdbc protocol - hsqldb type - file type - path to resource
    private String connectString = "jdbc:hsqldb:file:db-data/catalog";


    private void connectDB()throws Exception {

        String criarCatalogo = readToString("sql/catalogo.sql");
        String povoarCatalogo = readToString("sql/povoar-catalogo.sql");

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            throw e;
        }

        try {
            con = DriverManager.getConnection(connectString, "SA", "");

            con.createStatement().executeUpdate(criarCatalogo);
            con.createStatement().executeUpdate(povoarCatalogo);

            PreparedStatement pst = con.prepareStatement("select * from catalogo");
            pst.clearParameters();
            ResultSet rs = pst.executeQuery();

            List<Produto> catalogo = new ArrayList<Produto>();
            while(rs.next()){
                catalogo.add(new Produto(
                                rs.getInt(1),
                                rs.getString(2),
                                rs.getFloat(3),
                                rs.getInt(4)
                        )
                );
            }

            for(Produto p : catalogo) {
                System.out.println(p);
            }

        } catch (SQLException e) {
            throw e;
        } finally {
            con.close();
        }
    }

    private String readToString(String filename) throws Exception {
        File file = new File(filename);
        String string = FileUtils.readFileToString(file, "utf-8");
        return string;
    }

    private void adicionarProduto(Produto p){
        try{
            String sql = "insert into catalogo (nome, preco, stock) values (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, p.getNome());
            pst.setFloat(2, p.getPreco());
            pst.setInt(3, p.getStock());
            int res = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Catalogo db = new Catalogo();
        db.connectDB();
    }
}

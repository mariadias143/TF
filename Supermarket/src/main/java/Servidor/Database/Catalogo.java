package Servidor.Database;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Communication.StateUpdate;
import Servidor.Database.DAOs.EncomendaDAO;
import Servidor.Database.DAOs.ProdutoDAO;

import Servidor.Database.DAOs.StateUpdateDAO;
import org.apache.commons.io.FileUtils;

import static java.lang.Thread.sleep;

public class Catalogo {
    private Connection con;
    // jdbc protocol - hsqldb type - file type - path to resource
    private String connectString = "jdbc:hsqldb:file:db-data/catalog";
    private ProdutoDAO products;
    private EncomendaDAO orders;
    private StateUpdateDAO updates;

    private void connectDB()throws Exception {
        products = new ProdutoDAO();
        orders  = new EncomendaDAO();
        updates = new StateUpdateDAO();


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

            Produto pp = new Produto(15,"seita",5,20);
            Encomenda ee = new Encomenda(1,"1",0);
            //ee.addProd(1,4);
            // products.remove(17);
            products.put(1,pp);
            products.updateStock(3,43);

            orders.put(1,ee);
            orders.addProduct(1,new Pair(1,4));

            StateUpdate su = new StateUpdate();
            updates.put(1,su);

            System.out.println(updates.get(1));

            //con.createStatement().executeUpdate("drop  table catalogo");
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
        sleep(1000);
    }
}

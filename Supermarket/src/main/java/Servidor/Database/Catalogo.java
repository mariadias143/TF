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

    public Catalogo(String name){
        this.connectString += name;
    }

    public void connectDB()throws Exception {
        String criarCatalogo = readToString("sql/catalogo.sql");
        String povoarCatalogo = readToString("sql/povoar-catalogo.sql");

        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            throw e;
        }

        try {
            con = DriverManager.getConnection(connectString, "SA", "");

            String[] tableList = {"ENCOMENDA", "ENCOMENDA_PRODUTOS","PRODUTO","STATEUPDATE","STATE_REMPROD"};
            String[] types = {"TABLE"};
            int n = 0;
            ResultSet tables = con.getMetaData().getTables(null, null, "%", types);
            while (tables.next()) {
                for(String t: tableList)
                    if(tables.getString("TABLE_NAME").equals(t))
                        n++;
            }

            if (n != tableList.length){
                con.createStatement().executeUpdate(criarCatalogo);
                con.createStatement().executeUpdate(povoarCatalogo);
                con.commit();
            }
          /*  sleep(100);
            Produto pp = new Produto(15,"seita",5,20);
            Encomenda ee = new Encomenda(1,"1","aa");
            orders.put(1,ee);
            orders.addProduct(1,new Pair(1,4));
            orders.addProduct(1,new Pair(1,5));
            System.out.println(orders.values());
        */
/*


            Produto pp = new Produto(15,"seita",5,20);
            Encomenda ee = new Encomenda(9,"1",0);
            //ee.addProd(1,4);
            // products.remove(17);

            products.updateStock(3,43);




            StateUpdate su = new StateUpdate();
            updates.put(1,su);

            System.out.println(updates.get(1));
            System.out.println(orders.get(1));
            System.out.println(orders.lastId());
*/


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


    public void printPRODUTOS(){
        try {
            con = DriverManager.getConnection(connectString, "SA", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from PRODUTO");
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome= rs.getString("nome");
                float preco= rs.getFloat("preco");
                int stock= rs.getInt("stock");
                System.out.println("ID:"+id+", NOME:"+ nome+", PRECO:"+ preco+", STOCK:"+ stock);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void printENCOMENDAS(){
        try {
            con = DriverManager.getConnection(connectString, "SA", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from ENCOMENDA");
            while (rs.next()) {
                int id = rs.getInt("id");
                float timeout = rs.getFloat("timeout");
                String idUser = rs.getString("idUser");
                System.out.println("ID:"+id+", ID_USER:"+ idUser+", TIMEOUT:"+ timeout);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void printENCOMENDA_PRODUTOS(){
        try {
            con = DriverManager.getConnection(connectString, "SA", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from ENCOMENDA_PRODUTOS");
            while (rs.next()) {
                int idEnc = rs.getInt("idEnc");
                int idProd = rs.getInt("idProd");
                int quantidade = rs.getInt("quantidade");
                System.out.println("ID:"+idEnc+", ID_PROD:"+ idProd+", QUANT:"+ quantidade);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void printSTATEUPDATE(){
        try {
            con = DriverManager.getConnection(connectString, "SA", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from StateUpdate");
            while (rs.next()) {
                int timestamp = rs.getInt("timestam");
                int type = rs.getInt("type");
                int idEnc = rs.getInt("idEnc");
                int idProdAdd = rs.getInt("idProdAdd");
                String userId = rs.getString("userId");
                int qntProdAdd = rs.getInt("qntProdAdd");
                System.out.println("TIMESTAMP:"+timestamp+", TYPE:"+ type+", ID_USER:"+ userId+", ID_ENC:"+ idEnc+", ID_PROD:"+ idProdAdd+", QUANT_ADD:"+ qntProdAdd);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public void printSTATEREMPROD(){
        try {
            con = DriverManager.getConnection(connectString, "SA", "");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from STATE_REMPROD");
            while (rs.next()) {
                int timestamp = rs.getInt("timestam");
                int idProd = rs.getInt("idProd");
                int quantidade = rs.getInt("quantidade");
                System.out.println("TIMESTAMP:"+timestamp + ", ID_PROD:"+ idProd + ", QUANT:" + quantidade);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Catalogo db = new Catalogo("");
        db.connectDB();
        sleep(1000);
    }
}

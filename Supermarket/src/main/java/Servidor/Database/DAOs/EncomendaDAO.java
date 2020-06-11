package Servidor.Database.DAOs;

import Servidor.Database.Encomenda;
import Servidor.Database.Pair;
import Servidor.Database.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncomendaDAO implements Map<Integer, Encomenda> {

    private Connection con;
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean flag =false;
        try{
            con = Connect.connect();
            String sql = "Select  * FROM Encomenda  WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if(rs.next()) {
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return flag;
    }


    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Encomenda get(Object key) {
        Encomenda e = new Encomenda();
        Map<Integer,Pair> products = new HashMap<>();

        try {
            con =Connect.connect();
            String sql = "SELECT * FROM Encomenda WHERE id= ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                e.setIdEnc(rs.getInt(1));
                e.setIdUser(rs.getString(2));
                e.setTimeout(rs.getFloat(3));

                sql = "SELECT * FROM Encomenda_Produtos where idEnc = ?";
                pst=con.prepareStatement(sql);
                pst.setInt(1, (int) key);
                ResultSet rss = pst.executeQuery();
                while (rss.next()) {
                    products.put((int) key,
                            new Pair (rss.getInt(2),
                                      rss.getInt(3)
                            ));
                }
                e.setProds(products);
            }
            else {
                e=null;
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return e;
    }


    @Override
    public Encomenda put(Integer key, Encomenda value) {
        try{
            con = Connect.connect();
            String sql = "insert into Encomenda (id, idUser, timeout) values (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, value.getIdEnc());
            pst.setString(2, value.getIdUser());
            pst.setFloat(3, value.getTimeout());
            int res = pst.executeUpdate();

            for(Pair p : value.getProds().values()) {

                sql = "insert into  Encomenda_Produtos (idEnc,idProd,quantidade) values (?,?,?)";
                pst=con.prepareStatement(sql);
                pst.setInt(1,key);
                pst.setInt(2,p.idProd);
                pst.setInt(3,p.qntProd);
                res = pst.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                con.close();
                return value;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }



        return value;
    }
    @Override
    public Encomenda remove(Object key) {
        try{
            con = Connect.connect();
            String sql = "DELETE FROM Encomenda  WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            int res = pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }



    @Override
    public void putAll(Map<? extends Integer, ? extends Encomenda> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Integer> keySet() {
        return null;
    }

    @Override
    public Collection<Encomenda> values() {
        return null;
    }

    @Override
    public Set<Entry<Integer, Encomenda>> entrySet() {
        return null;
    }


    public void addProduct (int idEnc,Pair p) {
        try{
            con = Connect.connect();
            String sql = "insert into  Encomenda_Produtos (idEnc,idProd,quantidade) values (?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1,idEnc);
            pst.setInt(2,p.idProd);
            pst.setInt(3,p.qntProd);
            int res = pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                con.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}


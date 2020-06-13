package Servidor.Database.DAOs;

import Servidor.Database.Encomenda;
import Servidor.Database.Pair;
import Servidor.Database.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EncomendaDAO implements Map<Integer, Encomenda> {

    private Connection con;
    private String connectString = "";

    public EncomendaDAO(String n){
        this.connectString = n;
    }

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
        boolean flag = false;
        try {
            con = Connect.connect(this.connectString);
            String sql = "Select  * FROM Encomenda  WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
        Map<Integer, Pair> products = new HashMap<>();

        try {
            con = Connect.connect(this.connectString);
            String sql = "SELECT * FROM Encomenda WHERE id= ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                e.setIdEnc(rs.getInt(1));
                e.setIdUser(rs.getString(2));
                e.setEnd(rs.getString(3));

                sql = "SELECT * FROM Encomenda_Produtos where idEnc = ?";
                pst = con.prepareStatement(sql);
                pst.setInt(1, (int) key);
                ResultSet rss = pst.executeQuery();
                while (rss.next()) {
                    products.put( rss.getInt(2),
                            new Pair(rss.getInt(2),
                                    rss.getInt(3)
                            ));
                }
                e.setProds(products);
            } else {
                e = null;
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return e;
    }


    @Override
    public Encomenda put(Integer key, Encomenda value) {
        try {
            con = Connect.connect(this.connectString);
            String sql = "insert into Encomenda (id, idUser,final) values (?, ?,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, value.getIdEnc());
            pst.setString(2, value.getIdUser());
            pst.setString(3,value.getEnd());
            int res = pst.executeUpdate();

            for (Pair p : value.getProds().values()) {

                sql = "insert into  Encomenda_Produtos (idEnc,idProd,quantidade) values (?,?,?)";
                pst = con.prepareStatement(sql);
                pst.setInt(1, key);
                pst.setInt(2, p.idProd);
                pst.setInt(3, p.qntProd);
                res = pst.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
        try {
            con = Connect.connect(this.connectString);
            String sql = "DELETE FROM Encomenda  WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            int res = pst.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
        Collection encomendas = new ArrayList();

        try {
            con = Connect.connect(this.connectString);
            String sql = "SELECT * FROM Encomenda ";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while(rs.next()) {
                Encomenda e = new Encomenda();
                e.setIdEnc(rs.getInt(1));
                e.setIdUser(rs.getString(2));
                e.setEnd(rs.getString(3));

                sql = "SELECT * FROM Encomenda_Produtos where idEnc = ?";
                pst = con.prepareStatement(sql);
                pst.setInt(1, e.getIdEnc());
                ResultSet rss = pst.executeQuery();
                Map<Integer, Pair> products = new HashMap<>();
                while (rss.next()) {
                    products.put((rss.getInt(2)),
                            new Pair(rss.getInt(2),
                                    rss.getInt(3)
                            ));
                }
                e.setProds(products);
                encomendas.add(e);

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encomendas;
    }
        @Override
    public Set<Entry<Integer, Encomenda>> entrySet() {
        return null;
    }


    public void addProduct(int idEnc, Pair p) {
        try {
            con = Connect.connect(this.connectString);

            String sql = "Select * from Encomenda_Produtos where idEnc = ? and idProd = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, idEnc);
            pst.setInt(2, p.idProd);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int quantidade = rs.getInt(3);
                quantidade += p.qntProd;

                sql = "UPDATE Encomenda_Produtos set quantidade = ? where idEnc = ? and idProd = ?";
                pst = con.prepareStatement(sql);
                pst.setInt(1, quantidade);
                pst.setInt(2, idEnc);
                pst.setInt(3, p.idProd);
                int rss = pst.executeUpdate();
            } else {
                sql = "insert into  Encomenda_Produtos (idEnc,idProd,quantidade) values (?,?,?)";
                pst = con.prepareStatement(sql);
                pst.setInt(1, idEnc);
                pst.setInt(2, p.idProd);
                pst.setInt(3, p.qntProd);
                int res = pst.executeUpdate();
            }
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


    public int lastId() {
        int id = -1;
        try {
            con = Connect.connect(this.connectString);
            String sql = "SELECT * FROM Encomenda ORDER BY ID DESC LIMIT 1";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                id = (rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                return id;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return id;

    }
}


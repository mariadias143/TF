package Servidor.Database.DAOs;

import Servidor.Database.Produto;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class ProdutoDAO implements Map<Integer,Produto> {

    private Connection con;
    private String connectString = "";

    public ProdutoDAO(String n){
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
            String sql = "Select  * FROM Produto  WHERE id = ?";
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
    public Produto get(Object key) {
        Produto p = new Produto();

        try {
            con =Connect.connect(this.connectString);
            String sql = "SELECT * FROM Produto WHERE id= ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                p.setId(rs.getInt(1));
                p.setNome(rs.getString(2));
                p.setPreco(rs.getFloat(3));
                p.setStock(rs.getInt(4));
            }
            else {
                p=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
                return p;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return p;
    }


    @Override
    public Produto put(Integer key, Produto value) {
        try{
            con = Connect.connect(this.connectString);
            String sql = "insert into Produto (nome, preco, stock) values (?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, value.getNome());
            pst.setFloat(2, value.getPreco());
            pst.setInt(3, value.getStock());
            int res = pst.executeUpdate();
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
    public Produto remove(Object key) {
        try{
            con = Connect.connect(this.connectString);
            String sql = "DELETE FROM Produto  WHERE id = ?";
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
    public void putAll(Map<? extends Integer, ? extends Produto> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Integer> keySet() {
        return null;
    }

    @Override
    public Collection<Produto> values() {
        return null;
    }

    @Override
    public Set<Entry<Integer, Produto>> entrySet() {
        return null;
    }

    public void updateStock( int index,int stock){ // <0 para retirar >0 para aumentar

        Produto p =get(index);
        int st = p.getStock();
        st+=stock;

        if( st >= 0) {
            try {
                con = Connect.connect(this.connectString);
                String sql = "UPDATE Produto set stock = ? WHERE id = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, st);
                pst.setInt(2, index);
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
        }


    }




}

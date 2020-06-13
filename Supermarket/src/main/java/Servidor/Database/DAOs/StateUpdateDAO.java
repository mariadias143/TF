package Servidor.Database.DAOs;

import Communication.StateUpdate;
import Servidor.Database.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StateUpdateDAO implements Map<Integer, StateUpdate> {

    private Connection con;
    private String connectString = "";

    public StateUpdateDAO(String n){
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
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public StateUpdate get(Object key) {
        StateUpdate su = new StateUpdate();
        List<Pair> productsREM = new ArrayList<>();

        try {
            con =Connect.connect(this.connectString);
            String sql = "SELECT * FROM StateUpdate WHERE timestam = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int) key);
            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                su.setTimestamp(rs.getInt(1));
                su.setType(rs.getInt(2));
                su.setUserId(rs.getString(3));
                su.setIdEnc(rs.getInt(4));
                su.setBegin(rs.getString(5));
                su.setEnd(rs.getString(6));
                su.setIdProdAdd(rs.getInt(7));
                su.setQntProdAdd(rs.getInt(8));

                sql = "SELECT * FROM State_RemProd  where timestam = ?";
                pst=con.prepareStatement(sql);
                pst.setInt(1, (int) key);
                ResultSet rss = pst.executeQuery();
                while (rss.next()) {
                    productsREM.add(
                            new Pair(rss.getInt(2),
                                    rss.getInt(3)
                            ));
                }
                su.setRemProd(productsREM);
            }
            else {
                su=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return su;
    }

    @Override
    public StateUpdate put(Integer key, StateUpdate value) {
        try{
            con = Connect.connect(this.connectString);
            String sql = "insert into StateUpdate (timestam, type, userId,idEnc,inicial,final,idProdAdd,qntProdAdd) values (?, ?,?,?,?,?,?,?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, (int)key);
            pst.setInt(2, value.getType());
            pst.setString(3, value.getUserId());
            pst.setInt(4, value.getIdEnc());
            pst.setString(5,value.getBegin());
            pst.setString(6,value.getEnd());
            pst.setInt(7, value.getIdProdAdd());
            pst.setInt(8, value.getQntProdAdd());
            int res = pst.executeUpdate();

            for(Pair p : value.getRemProd()) {

                sql = "insert into  State_RemProd (timestam,idProd,quantidade) values (?,?,?)";
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
    public StateUpdate remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends StateUpdate> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<Integer> keySet() {
        return null;
    }

    @Override
    public Collection<StateUpdate> values() {
        return null;
    }

    @Override
    public Set<Entry<Integer, StateUpdate>> entrySet() {
        return null;
    }




    public int lastTimestamp() {
        int timestamp = -1;
        try {
            con = Connect.connect(this.connectString);
            String sql = "SELECT * FROM StateUpdate ORDER BY timestam DESC LIMIT 1";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                timestamp = (rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                return timestamp;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return timestamp;

    }


    public Collection<Integer> allTimestamp(){
        List<Integer> timestamps = new ArrayList<>();

        try {
            con = Connect.connect(this.connectString);
            String sql = "SELECT DISTINCT timestam FROM StateUpdate ";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                 int t = (rs.getInt(1));
                 timestamps.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
                return timestamps;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        return timestamps;
    }
}

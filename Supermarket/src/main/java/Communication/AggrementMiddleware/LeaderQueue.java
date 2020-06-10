package Communication.AggrementMiddleware;

import java.util.ArrayList;
import java.util.List;

public class LeaderQueue {
    private List<String> queue;
    private int size;

    public LeaderQueue(){
        queue = new ArrayList<>();
        size = 0;
    }

    public synchronized void remove(String item){
        int index = queue.indexOf(item);

        if (index != -1){
            queue.remove(item);
            size--;
        }
    }

    public synchronized boolean isEmpty(){
        return size == 0;
    }

    public synchronized void add(String s){
        queue.add(s);
        size++;
    }

    public synchronized boolean isLeader(String s){
        boolean isLeader = false;

        if (size > 0 && queue.get(0).equals(s))
            isLeader = true;

        return isLeader;
    }

    public synchronized String toString(){
        StringBuilder bt = new StringBuilder();
        int i = 0;
        for(String s : queue){
            bt.append(i + ":" + " " + s + "\n");
        }
        return bt.toString();
    }
}

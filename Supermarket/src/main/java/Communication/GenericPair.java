package Communication;

import spread.SpreadGroup;

public class GenericPair<K,T> {
    public K fst;
    public T snd;
    public SpreadGroup destination;

    public GenericPair(K fst,T snd,SpreadGroup destination){
        this.fst = fst;
        this.snd = snd;
        this.destination = destination;
    }

    public GenericPair(K fst,T snd){
        this.fst = fst;
        this.snd = snd;
        this.destination = null;
    }
}

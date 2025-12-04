package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    public Iterator<T> iterator(){
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private int cur;
        public LinkedListIterator(){
            cur = 0;
        }

        public boolean hasNext() {
            return cur < siz;
        }

        public T next() {
            return get(cur++);
        }
    }
    public class IntNode<T> {
        T val;
        IntNode pre, nxt;
        public IntNode(){
            val = null;
            pre = nxt = null;
        }
        public IntNode(T v){
            val = v;
            pre = nxt = null;
        }
    }
    IntNode frt, lst;
    int siz;
    public LinkedListDeque(){
        siz = 0;
    }

    public void addFirst(T item) {
        if(siz == 0){
            frt = lst = new IntNode<>(item);
        }
        else {
            IntNode cur = new IntNode<>(item);
            frt.pre = cur; cur.nxt = frt;
            frt = cur;
        }
        siz ++;
    }

    public void addLast(T item) {
        if(siz == 0){
            frt = lst = new IntNode<>(item);
        }
        else {
            IntNode cur = new IntNode<>(item);
            lst.nxt = cur; cur.pre = lst;
            lst = cur;
        }
        siz ++;
    }

    public boolean isEmpty() {
        if(siz != 0)return false;
        else return true;
    }

    public int size() {
        return siz;
    }

    public void printDeque() {
        IntNode p = frt;
        System.out.print(p.val);
        System.out.print(" ");
        while(p != lst){
            p = p.nxt;
            System.out.print(p.val);
            System.out.print(" ");
        }
        System.out.print("\n");
    }

    public T removeFirst() {
        if(siz == 0) return null;
        siz --; IntNode<T> cur = frt;
        if(siz == 0)frt = lst = null;
        else {frt = frt.nxt; frt.pre = null;}
        return cur.val;
    }

    public T removeLast() {
        if(siz == 0) return null;
        siz --; IntNode<T> cur = lst;
        if(siz == 0)lst = lst = null;
        else {lst = lst.pre; lst.nxt = null;}
        return cur.val;
    }

    public T get(int index) {
        if(index >= siz) return null;
        IntNode<T> p = frt;
        while(index != 0){
            index --;
            p = p.nxt;
        }
        return p.val;
    }

    public boolean equals(Deque o) {
        if(this == o)return true;
        if(o == null)return false;
        if(!(o instanceof Deque))return false;
        if(this.size() != o.size())return false;
        for(int i = 0; i < this.size(); i++){
            if(!this.get(i).equals(o.get(i)))return false;
        }
        return true;
    }

    public T getRecursive(int index) {
        if(index >= siz) return null;
        class getHelper{
            public T helper(IntNode p, int index){
                if(index == 0) return (T) p.val;
                else return (T) helper(p.nxt, index - 1);
            }
        }
        getHelper getHelp = new getHelper();
        return getHelp.helper(frt, index);
    }
}

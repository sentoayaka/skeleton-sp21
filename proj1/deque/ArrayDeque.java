package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    int N = 8;
    double k = 2, p = 0.25;
    public T[] a;
    int frt, lst, siz;
    public ArrayDeque(){
        a = (T[])new Object[8];
        frt = 0; lst = 1; siz = 0;
    }

    private boolean CheckTooShort(){
        return siz < N * p;
    }
    private void SpanArray(){
        T[] aNew = (T[])new Object[(int) (N * k)];
        for(int i= 1, p = FindNxt(frt); p != lst; i++, p = FindNxt(p)) {
            aNew[i] = a[p];
        }
        a = aNew; N = (int) (N * k);
        frt = 0; lst= siz + 1;
    }
    private void ShrinkArray(){
        T[] aNew = (T[])new Object[siz + 2];
        for(int i= 1, p = FindNxt(frt); p != lst; i++, p = FindNxt(p)) {
            aNew[i] = a[p];
        }
        a = aNew; N = siz + 2;
        frt = 0; lst= siz + 1;
    }
    private int FindPre(int x){
        if(x > 0)return x - 1;
        else return N-1;
    }
    private int FindNxt(int x){
        if(x < N - 1)return x + 1;
        else return 0;
    }
    @Override
    public void addFirst(T item) {
        siz++; a[frt] = item;
        frt = FindPre(frt);
        if(frt == lst)SpanArray();
    }

    @Override
    public void addLast(T item) {
        siz++; a[lst] = item;
        lst = FindNxt(lst);
        if(frt == lst)SpanArray();
    }

    @Override
    public boolean isEmpty() {
        if(siz != 0)return false;
        else return true;
    }

    @Override
    public int size() {
        return siz;
    }

    @Override
    public void printDeque() {
        if(siz == 0)return ;
        for(int i = FindNxt(frt); i != lst; i = FindNxt(i)){
            System.out.print(a[i]);
            System.out.print(' ');
        }
        System.out.print("\n");
    }

    @Override
    public T removeFirst() {
        if(siz == 0) return null;
        siz--; frt = FindNxt(frt);
        T cur = a[frt];
        if(CheckTooShort())ShrinkArray();
        return cur;
    }

    @Override
    public T removeLast() {
        if(siz == 0) return null;
        siz--; lst = FindPre(lst);
        T cur = a[lst];
        if(CheckTooShort())ShrinkArray();
        return cur;
    }

    @Override
    public T get(int index) {
        if(index > siz)return null;
        int p = FindNxt(frt);
        while(index != 0){p = FindNxt(p); index--;}
        return a[p];
    }

    @Override
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

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    public class ArrayDequeIterator implements Iterator<T>{
        private int cur;
        public ArrayDequeIterator(){
            cur = FindNxt(frt);
        }

        @Override
        public boolean hasNext() {
            return cur != lst;
        }

        @Override
        public T next() {
            T res = a[cur]; cur = FindNxt(cur);
            return res;
        }
    }
}

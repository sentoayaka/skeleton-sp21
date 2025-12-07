package deque;

public interface Deque<T> {
    public void addFirst(T item);
    public void addLast(T item);

    default public boolean isEmpty() {
        if(size() != 0)return false;
        else return true;
    }

    public int size();
    public void printDeque();
    public T removeFirst();
    public T removeLast();
    public T get(int index);
    public boolean equals(Deque o);
}

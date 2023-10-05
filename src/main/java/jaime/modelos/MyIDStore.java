package jaime.modelos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyIDStore {
    private static MyIDStore instance=null;
    private int cont=0;
    private final Lock lockCont = new ReentrantLock(true);
    private MyIDStore(){

    }
    public static MyIDStore getInstance() {
        if (instance == null) {
            instance = new MyIDStore();
        }
        return instance;
    }

    public int addandgetID(){
        lockCont.lock();
        try {
            return this.cont++;
        } finally {
            lockCont.unlock();
        }
    }
}

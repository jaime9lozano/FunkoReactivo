package jaime.modelos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyIDStore {
    private static MyIDStore instance=null;
    private Long cont= Long.valueOf(1);
    private final Lock lockCont = new ReentrantLock(true);
    private MyIDStore(){

    }
    public static MyIDStore getInstance() {
        if (instance == null) {
            instance = new MyIDStore();
        }
        return instance;
    }

    public Long addandgetID(){
        lockCont.lock();
        try {
            return this.cont++;
        } finally {
            lockCont.unlock();
        }
    }
}

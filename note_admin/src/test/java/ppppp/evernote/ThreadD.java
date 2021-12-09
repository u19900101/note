package ppppp.evernote;

/**
 * @author pppppp
 * @date 2021/12/9 17:03
 */
public class ThreadD {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main start...");
        Thread t = new Thread(() -> {
            System.out.println("hello1");
        });
        t.start();
        Thread t2 = new Thread(() -> {
            System.out.println("hello2");
        });
        t2.start();
        /*一个线程还可以等待另一个线程直到其运行结束。
        例如，main线程在启动t线程后，可以通过t.join()等待t线程结束后再继续运行：*/
        t.join();
        t2.join();
        System.out.println("main end...");
    }
}

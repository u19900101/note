package ppppp.evernote;

/**
 * @author pppppp
 * @date 2021/11/7 22:12
 */
public class 跳出递归 {
    public static void main(String args[]) {
        System.out.println("start!");
        try {
            find(0);
        } catch (StopMsgException e) {

        }
        System.out.println("done!");
    }


    private static void find(int level) {

        if (level > 10) {
            // 跳出
            throw new StopMsgException();
        }
        // 执行操作
        System.out.println(level);
        // 递归
        find(level + 1);
    }

    static class StopMsgException extends RuntimeException {
    }
}

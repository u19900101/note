package ppppp.evernote.util.ftp;

/**
 * @author pppppp
 * @date 2021/11/26 22:53
 */
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Date;


public class sftp {
    private static final Logger logger = LoggerFactory.getLogger(sftp.class);

    private static Date last_push_date = null;

    private Session sshSession;

    private ChannelSftp channel;

    private static ThreadLocal<sftp> sftpLocal = new ThreadLocal<sftp>();


   /* public static void main(String[] args) throws Exception {
        new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil("192.168.56.10", 22, "root", "vagrant");
                    File file = new File("C:\\Users\\Administrator\\Desktop\\temp\\1.png");
                    InputStream inputStream = new FileInputStream(file);
                    sftp.uploadFile("/mydata/nginx/html/img", "/test", inputStream, "1.png");
                    sftp.release();
                    System.out.println("1111" + sftpLocal.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        //线程2的ThreadLocalMap中存着key为threadLocalA，value为A2；key为threadLocalB，value为B2
        new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil("192.168.56.10", 22, "root", "vagrant");
                    File file = new File("C:\\Users\\Administrator\\Desktop\\temp\\2.jpg");
                    InputStream inputStream = new FileInputStream(file);
                    sftp.uploadFile("/mydata/nginx/html/img", "/test", inputStream, "2.png");
                    sftp.release();
                    System.out.println("2222" + sftpLocal.get());

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }*/


    private sftp(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        jsch.getSession(username, host, port);
        //根据用户名，密码，端口号获取session
        sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        //修改服务器/etc/ssh/sshd_config 中 GSSAPIAuthentication的值yes为no，解决用户不能远程登录
        sshSession.setConfig("userauth.gssapi-with-mic", "no");

        //为session对象设置properties,第一次访问服务器时不用输入yes
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
        //获取sftp通道
        channel = (ChannelSftp) sshSession.openChannel("sftp");
        channel.connect();
        logger.info("连接ftp成功!" + sshSession);
    }

    /**
     * 是否已连接
     *
     * @return
     */
    private boolean isConnected() {
        return null != channel && channel.isConnected();
    }

    /**
     * 获取本地线程存储的sftp客户端
     *
     * @return
     * @throws Exception
     */
    public static sftp getSftpUtil(String host, int port, String username, String password) throws Exception {
        //获取本地线程
        sftp sftpUtil = sftpLocal.get();
        if (null == sftpUtil || !sftpUtil.isConnected()) {
            //将新连接防止本地线程，实现并发处理
            sftpLocal.set(new sftp(host, port, username, password));
        }
        return sftpLocal.get();
    }

    /**
     * 释放本地线程存储的sftp客户端
     */
    public static void release() {
        if (null != sftpLocal.get()) {
            sftpLocal.get().closeChannel();
            logger.info("关闭连接" + sftpLocal.get().sshSession);
            sftpLocal.set(null);
            sftpLocal.remove();
        }
    }

    /**
     * 判断目录是否存在
     */
    private static boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftpLocal.get().channel.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if ("no such file".equals(e.getMessage().toLowerCase())) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 创建一个文件目录
     */
    private static void createDir(String createpath) throws Exception {
        if (!isDirExist(createpath)) {
            String pathArry[] = createpath.split("/");

            for (String path : pathArry) {
                if (StringUtils.isEmpty(path)) {
                    continue;
                }
                if (isDirExist(path)) {
                    sftpLocal.get().channel.cd(path);
                } else {
                    // 建立目录
                    sftpLocal.get().channel.mkdir(path);
                    // 进入并设置为当前目录
                    sftpLocal.get().channel.cd(path);
                }
            }
        }
    }


    /**
     * 关闭通道
     *
     * @throws Exception
     */
    public static void closeChannel() {
        if (null != sftpLocal.get().channel && sftpLocal.get().channel.isConnected()) {
            try {
                sftpLocal.get().channel.disconnect();
            } catch (Exception e) {
                logger.error("关闭SFTP通道发生异常:", e);
            }
        }
        if (null != sftpLocal.get().sshSession && sftpLocal.get().sshSession.isConnected()) {
            try {
                sftpLocal.get().sshSession.disconnect();
            } catch (Exception e) {
                logger.error("SFTP关闭 session异常:", e);
            }
        }
    }

    /**
     * 上传文件(cd目录一定要注意，层层cd 直接cd一个全路径是不起作用的)
     *
     * @param directory 上传ftp的目录
     * @param input     本地文件InputStream
     */
    public static boolean uploadFile(String basePath, String directory, InputStream input, String fileName) {
        //创建目录
        try {
            sftpLocal.get().channel.cd(basePath);

            createDir(directory);

            sftpLocal.get().channel.put(input, fileName);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage() + e.getMessage() + e.toString());
            return false;
        }
        return true;
    }

}


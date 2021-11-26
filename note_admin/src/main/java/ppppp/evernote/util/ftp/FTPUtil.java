package ppppp.evernote.util.ftp;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;

/**
 * @author pppppp
 * @date 2021/11/23 21:29
 */
@Component
public class FTPUtil {
    private static String host;

    @Value(value = "${ftp-centos.host}")
    public void setHost(String hostName) {
        host = hostName;
    }

    private static Integer port;

    @Value(value = "${ftp-centos.port}")
    public void setPort(Integer portName) {
        port = portName;
    }

    private static String user;

    @Value(value = "${ftp-centos.user}")
    public void setUser(String userName) {
        user = userName;
    }

    private static String password;

    @Value(value = "${ftp-centos.password}")
    public void setPassword(String passwordName) {
        password = passwordName;
    }

    private static String basePath;

    @Value(value = "${ftp-centos.basePath}")
    public void setBasePath(String basePath) {
        FTPUtil.basePath = basePath;
    }

    private static ChannelSftp sftp;

    public static void sshSftp(byte[] bytes, String fileName) throws Exception {
        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        if (port <= 0) {
            //连接服务器，采用默认端口
            session = jsch.getSession(user, host);
        } else {
            //采用指定的端口连接服务器
            session = jsch.getSession(user, host, port);
        }

        //如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new Exception("session is null");
        }
        //设置登陆主机的密码
        session.setPassword(password);
        //设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        //设置登陆超时时间
        session.connect(30000);
        OutputStream outstream = null;
        try {
            //创建sftp通信通道
            channel = (Channel) session.openChannel("sftp");
            channel.connect(1000);
            ChannelSftp sftp = (ChannelSftp) channel;
            //进入服务器指定的文件夹
            sftp.cd(basePath);
            outstream = sftp.put(fileName);
            outstream.write(bytes);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关流操作
            if (outstream != null) {
                outstream.flush();
                outstream.close();
            }
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    /**
     * sftp连接
     */
    public static void connect(String username, String host, int port, String password) throws Exception {
        try {
            if (sftp != null) {
                // System.out.println("sftp is not null");
            }

            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            Session sshSession = jsch.getSession(username, host, port);
            System.out.println("Session created.");
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            System.out.println("Session connected.");
            System.out.println("Opening Channel.");
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            System.out.println("success Connected to " + host + ".");
        } catch (Exception e) {
            throw new Exception("连接:[" + host + "]ftp服务器异常");
        }
    }

    /**
     * sftp断开连接
     */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
            } else if (this.sftp.isClosed()) {
                System.out.println("sftp is closed already");
            }
        }
    }


    /**
     * 上传文件流 本地文件路径 remotePath 服务器路径
     */

    public static String upload(FileInputStream fileInputStream, String remotDir, String fileName) throws Exception {
        try {
            // System.out.println("localFile : " + file.getAbsolutePath());
            // 建立连接
            connect(user, host, port, password);
            try {
                createDir(basePath + remotDir, sftp);
            } catch (Exception e) {
                throw new Exception("创建路径失败：" + basePath + remotDir);
            }

            //列出服务器指定的文件列表
            Vector ls = sftp.ls(basePath + remotDir);

            // 判断文件是否重名  i=2 是排除 两个文件 . 和 ..
            for (int i = 2; i < ls.size(); i++) {
                ChannelSftp.LsEntry l = (ChannelSftp.LsEntry) ls.get(i);
                if(l.getFilename().equals(fileName)){
                    System.out.println("重名鸟...");
                    String[] s = fileName.split("\\.");
                    fileName = s[0] + "_"+UUID.randomUUID().toString().substring(0,2) + "." + s[1];
                    i = 1;
                }
            }
            sftp.cd(basePath + remotDir);
            sftp.put(fileInputStream, fileName);
            return fileName;
        } catch (FileNotFoundException e) {
            throw new Exception("上传文件没有找到");
        } catch (Exception e) {
            throw new Exception("上传ftp服务器错误");
        }
    }

    /**
     * 创建一个文件目录
     */
    public static void createDir(String createpath, ChannelSftp sftp) throws Exception {
        try {
            if (isDirExist(createpath)) {
                sftp.cd(createpath);
                return;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
            sftp.cd(createpath);
        } catch (SftpException e) {
            throw new Exception("创建路径错误：" + createpath);
        }
    }

    /**
     * 判断目录是否存在
     */
    public static boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }
}

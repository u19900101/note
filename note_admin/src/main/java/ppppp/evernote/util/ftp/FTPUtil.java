package ppppp.evernote.util.ftp;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author pppppp
 * @date 2021/11/23 21:29
 */
@Component
public class FTPUtil {


    private static String host;
    @Value(value = "${ftp-centos.host}")
    public  void setHost(String hostName){
        host = hostName;
    }

    private static Integer port;
    @Value(value = "${ftp-centos.port}")
    public  void setPort(Integer portName) {
        port = portName;
    }

    private static String user;
    @Value(value = "${ftp-centos.user}")
    public  void setUser(String userName) {
        user = userName;
    }

    private static String password;
    @Value(value = "${ftp-centos.password}")
    public  void setPassword(String passwordName) {
        password = passwordName;
    }



    private static String basePath;
    @Value(value = "${ftp-centos.basePath}")
    public  void setBasePath(String basePath) {
        FTPUtil.basePath = basePath;
    }

    public static void sshSftp(byte[] bytes, String fileName) throws Exception {
        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();
        if (port<= 0) {
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
            //列出服务器指定的文件列表
//            Vector v = sftp.ls("*");
//            for(int i=0;i<v.size();i++){
//                System.out.println(v.get(i));
//            }
            //以下代码实现从本地上传一个文件到服务器，如果要实现下载，对换以下流就可以了
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
}

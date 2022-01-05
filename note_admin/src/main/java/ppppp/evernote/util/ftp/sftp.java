package ppppp.evernote.util.ftp;

/**
 * @author pppppp
 * @date 2021/11/26 22:53
 */

import com.jcraft.jsch.*;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ppppp.evernote.config.FtpConfig;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.Vector;

@Component
public class sftp {
    private static final Logger logger = LoggerFactory.getLogger(sftp.class);
    private static Date last_push_date = null;
    private Session sshSession;
    private ChannelSftp channel;
    private static ThreadLocal<sftp> sftpLocal = new ThreadLocal<sftp>();

    /**
     * 此处静态方法不能用注入的方式初始化 否则为null
     * 因为@Atuowire在初始化此对象之后执行 而调用静态方法不会初始化对象
     */
    static FtpConfig ftpConfig;
    @Autowired
    FtpConfig ftp;

    @PostConstruct
    public void init() {
        ftpConfig = this.ftp;
    }
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

    /**
     * 获取本地线程存储的sftp客户端
     *
     * @return
     * @throws Exception
     */
    public static sftp getSftpUtil() throws Exception {
        //获取本地线程
        sftp sftpUtil = sftpLocal.get();
        if (null == sftpUtil || !sftpUtil.isConnected()) {
            //将新连接防止本地线程，实现并发处理
            sftpLocal.set(new sftp(ftpConfig.getHost(), ftpConfig.getPort(), ftpConfig.getUsername(), ftpConfig.getPassword()));
        }
        return sftpLocal.get();
    }

    private sftp() {

    }

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
        } else {
            sftpLocal.get().channel.cd(createpath);
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
     * @param remotDir      上传ftp的目录
     * @param multipartFile
     * @return
     */
    public static String uploadVideo(File tempFile, String remotDir, MultipartFile multipartFile, String fileName) throws Exception {
        sftp.getSftpUtil();
        try {
            sftpLocal.get().channel.cd(ftpConfig.getBasepath());
            createDir(remotDir);

            //列出服务器指定的文件列表
            Vector ls = sftpLocal.get().channel.ls(ftpConfig.getBasepath() + remotDir);

            // 判断文件是否重名  i=2 是排除 两个文件 . 和 ..
            for (int i = 2; i < ls.size(); i++) {
                ChannelSftp.LsEntry l = (ChannelSftp.LsEntry) ls.get(i);
                if (l.getFilename().equals(fileName)) {
                    System.out.println("重名鸟...");
                    String[] s = fileName.split("\\.");
                    fileName = s[0] + "_" + UUID.randomUUID().toString().substring(0, 2) + "." + s[1];
                    i = 1;
                }
            }
            //上传视频文件
            FileInputStream fileInputStream = new FileInputStream(tempFile);
            sftpLocal.get().channel.put(fileInputStream, fileName);
            fileInputStream.close();
            System.out.println("视频文件上传成功");
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage() + e.getMessage() + e.toString());
            logger.error(e.getLocalizedMessage() + e.getMessage() + e.toString());
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                System.out.println("文件删除失败 " + tempFile.getAbsolutePath());
            }
            sftp.release();
        }
        return fileName;
    }

    /*上传人脸到服务器*/
    public static String uploadFaceFile(File tempFile, String basePath, String remotDir) throws Exception {
        sftp.getSftpUtil();
        //上传
        FileInputStream fileInputStream = new FileInputStream(tempFile);
        String fileName = tempFile.getName();
        //创建目录
        try {
            sftpLocal.get().channel.cd(basePath);
            createDir(remotDir);
            //列出服务器指定的文件列表
            Vector ls = sftpLocal.get().channel.ls(basePath + remotDir);

            // 判断文件是否重名  i=2 是排除 两个文件 . 和 ..
            for (int i = 2; i < ls.size(); i++) {
                ChannelSftp.LsEntry l = (ChannelSftp.LsEntry) ls.get(i);
                if (l.getFilename().equals(fileName)) {
                    System.out.println("重名鸟...");
                    String[] s = fileName.split("\\.");
                    fileName = s[0] + "_" + UUID.randomUUID().toString().substring(0, 2) + "." + s[1];
                    i = 1;
                }
            }

            sftpLocal.get().channel.put(fileInputStream, fileName);
            //便于文件关闭
            fileInputStream.close();
            System.out.println("人脸图图上传成功");
        } catch (Exception e) {
            System.out.println("error" + e.getLocalizedMessage() + e.getMessage() + e.toString());
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                System.out.println("人脸删除失败 ");
            }
            sftp.release();
        }
        return fileName;
    }

    public static String uploadFile(String remotDir, MultipartFile multipartFile, String fileName) throws Exception {
        sftp.getSftpUtil();
        //创建目录

        File tempFile = File.createTempFile(String.valueOf(UUID.randomUUID()), ".jpg");

        try {
            sftpLocal.get().channel.cd(ftpConfig.getBasepath());
            createDir(remotDir);
            //列出服务器指定的文件列表
            Vector ls = sftpLocal.get().channel.ls(ftpConfig.getBasepath() + remotDir);

            // 判断文件是否重名  i=2 是排除 两个文件 . 和 ..
            for (int i = 2; i < ls.size(); i++) {
                ChannelSftp.LsEntry l = (ChannelSftp.LsEntry) ls.get(i);
                if (l.getFilename().equals(fileName)) {
                    System.out.println("重名鸟...");
                    String type = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                    String name = fileName.substring(0, fileName.lastIndexOf("."));
                    fileName = name.replaceAll("\\.| ", "_") + "_" + UUID.randomUUID().toString().substring(0, 2) + type;
                    i = 1;
                }
            }
            //上传原图文件
            sftpLocal.get().channel.put(multipartFile.getInputStream(), fileName);
            // 上传缩略图 用于列表
            Thumbnails.of(multipartFile.getInputStream()).width(400).toFile(tempFile.getAbsoluteFile());
            FileInputStream fileInputStream = new FileInputStream(tempFile);
            sftpLocal.get().channel.put(fileInputStream, fileName.split("\\.")[0] + "_thumbnails." + fileName.split("\\.")[1]);
            //便于文件关闭
            fileInputStream.close();
            System.out.println("缩略图上传成功");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage() + e.getMessage() + e.toString());
        } finally {
            boolean delete = tempFile.delete();
            if (!delete) {
                System.out.println("文件删除失败 " + tempFile.getAbsoluteFile());
            }
            // todo 切面 aop
            sftp.release();
        }
        return fileName;
    }


    public static void deleteImageFromServer(String path, boolean deleteThumbnails) {

        new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil();
                    Boolean deleteFiles = deleteFile(ftpConfig.getBasepath(), path, deleteThumbnails);
                    sftp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static boolean deleteImageFromServer(ArrayList<String> deleteImageUrls, ArrayList<String> deleteFaceUrls) throws InterruptedException {
        final boolean[] deleteFiles = {false};
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil();
                    /*删除照片时顺带删除人脸*/
                    deleteFiles[0] = deleteFiles(deleteImageUrls, deleteFaceUrls);
                    sftp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join();
        return deleteFiles[0];
    }

    public static boolean deleteImageFromServer(ArrayList<String> paths, boolean deleteThumbnails) throws InterruptedException {
        final boolean[] deleteFiles = {false};
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil();
                    String basepath = ftpConfig.getBasepath();
                    /*指定删除人脸*/
                    if (!deleteThumbnails) {
                        // "/mydata/nginx/html/img/face"
                        basepath += "face/";
                    }
                    /*删除照片时顺带删除人脸*/
                    deleteFiles[0] = deleteFiles(basepath, paths, deleteThumbnails);
                    sftp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join();
        return deleteFiles[0];
    }

    public static Boolean deleteFiles(ArrayList<String> deleteImageUrls, ArrayList<String> deleteFaceUrls) throws IOException {
        boolean flag = true;
        try {
            /*删除照片和缩略图*/
            if(deleteImageUrls.size() > 0){
                for (String path : deleteImageUrls) {
                    /*删除大图*/
                    String[] split = path.split("/");
                    String dir = split[4];
                    String fileName = split[5];
                    String tempDir = ftpConfig.getBasepath() + dir;
                    sftpLocal.get().channel.cd(tempDir);
                    sftpLocal.get().channel.rm(fileName);
                    /*删除缩略图 NAME_thumbnails.jpg*/
                    String s = fileName.split("\\.")[0] + "_thumbnails." + fileName.split("\\.")[1];
                    sftpLocal.get().channel.rm(s);
                }
            }


            /*删除face*/
            if(deleteFaceUrls.size() > 0){

                for (String path : deleteFaceUrls) {
                    /*删除face*/
                    String[] split = path.split("/");
                    String dir = split[4];
                    String fileName = split[5];
                    String tempDir = ftpConfig.getBasepath().replace("img","face") + dir;
                    sftpLocal.get().channel.cd(tempDir);
                    sftpLocal.get().channel.rm(fileName);
                }
            }



            System.out.println("删除成功");
        } catch (Exception e) {
            System.out.println("删除服务器文件失败" + e.getLocalizedMessage() + e.getMessage() + e.toString());
            flag = false;
        }
        return flag;
    }

    public static Boolean deleteFiles(String basePath, ArrayList<String> paths, boolean deleteThumbnails) throws IOException {
        boolean flag = true;
        try {
            sftpLocal.get().channel.cd(basePath);
            for (String path : paths) {
                /*删除大图*/
                sftpLocal.get().channel.rm(path);

                /*删除缩略图 NAME_thumbnails.jpg*/
                if (deleteThumbnails) {
                    String s = path.split("\\.")[0] + "_thumbnails." + path.split("\\.")[1];
                    sftpLocal.get().channel.rm(s);
                }
            }
            System.out.println("删除成功");
        } catch (Exception e) {
            System.out.println("删除服务器文件失败" + e.getLocalizedMessage() + e.getMessage() + e.toString());
            flag = false;
        }
        return flag;
    }

    /*删除单张*/
    public static Boolean deleteFile(String basePath, String path, boolean deleteThumbnails) throws IOException {
        boolean flag = true;
        try {
            sftpLocal.get().channel.cd(basePath);
            /*删除大图*/
            sftpLocal.get().channel.rm(path);
            /*删除缩略图 NAME_thumbnails.jpg*/
            if (deleteThumbnails) {
                String s = path.split("\\.")[0] + "_thumbnails." + path.split("\\.")[1];
                sftpLocal.get().channel.rm(s);
            }
            System.out.println("删除成功");
        } catch (Exception e) {
            System.out.println("删除服务器文件失败" + e.getLocalizedMessage() + e.getMessage() + e.toString());
            flag = false;
        }
        return flag;
    }
}


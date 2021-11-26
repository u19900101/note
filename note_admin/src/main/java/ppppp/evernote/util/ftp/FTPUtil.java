package ppppp.evernote.util.ftp;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ppppp.evernote.entity.Picture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ppppp.evernote.controller.PictureController.getPictureInfo;

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

    /**
     * sftp连接
     */
    public static void connect() throws Exception {
        try {
            JSch jsch = new JSch();
            jsch.getSession(user, host, port);
            Session sshSession = jsch.getSession(user, host, port);
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
    public static void disconnect() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            } else if (sftp.isClosed()) {
                System.out.println("sftp is closed already");
            }
        }
    }


    /**
     * 上传文件流 本地文件路径 remotePath 服务器路径
     */
    public static ArrayList<Picture> upload(List<MultipartFile> fileList) throws Exception {
        ArrayList<Picture> pictureArrayList = new ArrayList<>();
        connect();
        for (MultipartFile multipartFile : fileList) {
            Picture picture = getPictureInfo(multipartFile);
            String uploadDir = new SimpleDateFormat("yyyy-MM-dd").format(picture.getCreateTime());
            String fileName = upload((FileInputStream) multipartFile.getInputStream(), uploadDir, picture.getTitle());
            // 若重名，则重新命名文件
            if(!fileName.equals(picture.getTitle())){
                picture.setTitle(fileName);
            }
            String imgUrl = "http://lpgogo.top/img/" + uploadDir + "/" + fileName;
            picture.setUrl(imgUrl);
            // 保存文件信息到数据库
            // boolean save = pictureService.save(picture);
            pictureArrayList.add(picture);
        }
        disconnect();
        return pictureArrayList;
    }

    public static String upload(FileInputStream fileInputStream, String remotDir, String fileName) throws Exception {
        try {
            System.out.println();
            try {
                createDir(basePath + remotDir + "/", sftp);
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
            System.out.println(fileName + "  上传成功");
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

package ppppp.evernote;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ppppp.evernote.entity.Picture;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author pppppp
 * @date 2021/12/20 15:39
 */
// @RunWith(SpringRunner.class)
// @SpringBootTest

public class GetVideo {

    public static void getVideoInfo(String videoPath, Picture picture) {
        //ffmepg工具地址
        String ffmpegPath = "D:\\MyJava\\mylifeImg\\src\\main\\resource\\ffmpeg.exe";
        //拼接cmd命令语句
        StringBuffer buffer = new StringBuffer();
        buffer.append(ffmpegPath);
        //注意要保留单词之间有空格
        buffer.append(" -i ");
        buffer.append(videoPath);
        //执行命令语句并返回执行结果
        try {
            Process process = Runtime.getRuntime().exec(buffer.toString());
            InputStream in = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            /*逐行读取视频文件的信息*/
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.trim().startsWith("Duration:")) {
                    //根据字符匹配进行切割
                    // System.out.println("视频时间 = " + line.trim().substring(0,line.trim().indexOf(",")));
                    picture.setLastTime(line.trim().substring(0, line.trim().indexOf(",")).split(" ")[1]);
                }
                //一般包含fps的行就包含分辨率
               /* if (line.contains("fps")) {
                    //根据
                    String definition = line.split(",")[2];
                    definition = definition.trim().split(" ")[0];
                }*/

                if (picture.getCreateTime() == null && line.trim().startsWith("creation_time")) {
                    String[] creation_time = line.trim().split(" ");
                    String timeStr = creation_time[creation_time.length - 1];
                    String create_time = timeStr.replace("T", " ").split("\\.")[0];
                    Date createTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(create_time);
                    createTime = new Date(createTime.getTime() + 8 * 3600 * 1000);
                    picture.setCreateTime(createTime);
                }

                if (picture.getLocation() == null && line.trim().startsWith("location")) {
                    //根据字符匹配进行切割
                    String[] location = line.trim().split("\\+");
                    picture.setLocation(location[2].replace("/", "") + "," + location[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void T_() throws IOException {
        // // String str = "http://lpgogo.top/video/1.mp4";
        String filePath = "C:\\Users\\Administrator\\Desktop\\temp\\video\\d.mp4";
        // File pictureFile = new File(str);
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        // MockMultipartFile(String name, @Nullable String originalFilename, @Nullable String contentType, InputStream contentStream)
        // 其中originalFilename,String contentType 旧名字，类型  可为空
        // 模拟产生上传的 multipartFile 文件
        MultipartFile multipartFile = new MockMultipartFile("copy" + file.getName(), file.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        Picture picture = getVideoInfo(multipartFile);
        System.out.println(picture);
    }

    public static Picture getVideoInfo(MultipartFile file) {
        File pictureFile = null;
        Picture picture = new Picture();
        try {
            // 用uuid作为文件名，防止生成的临时文件重复
            pictureFile = File.createTempFile(String.valueOf(UUID.randomUUID()), file.getOriginalFilename());
            // MultipartFile to File
            file.transferTo(pictureFile);
            picture.setSize(pictureFile.length());
            picture = getVideoInfo(pictureFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //程序结束时，删除临时文件
            deleteFile(pictureFile);
        }
        return picture;
    }

    private static void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }


    public static Picture getVideoInfo(File pictureFile) throws EncoderException {
        Picture picture = new Picture();
        Encoder encoder = new Encoder();
        MultimediaInfo m = encoder.getInfo(pictureFile);
        // 视频时长 00:03:20
        picture.setLastTime(getLastTime(m.getDuration()));
        //视频帧宽高
        picture.setWidthH(m.getVideo().getSize().getWidth() + "x" + m.getVideo().getSize().getHeight());
        String type = m.getFormat();
        picture.setSize(pictureFile.length());
        /*视频创建时间*/
        Date createTime = getCreateTime(pictureFile);
        picture.setCreateTime(createTime);
        return picture;
    }

    private static String getLastTime(long duration) {
        int hour = (int) duration / (60 * 60 * 1000);//小时
        int minute = (int) (duration % (60 * 60 * 1000)) / 60000;//分钟
        int second = (int) ((duration % (60 * 60 * 1000)) % 60000) / 1000;//秒
        String lastTime = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        return lastTime;
    }

    public static Date getCreateTime(File file) {
        if (file == null) {
            return null;
        }
        //ffmepg工具地址
        String ffmpegPath = "D:\\MyMind\\note\\note_admin\\src\\main\\resources\\ffmpeg.exe";
        //拼接cmd命令语句
        StringBuffer buffer = new StringBuffer();
        buffer.append(ffmpegPath);
        //注意要保留单词之间有空格
        buffer.append(" -i ");
        buffer.append(file.getAbsolutePath());
        //执行命令语句并返回执行结果
        try {
            Process process = Runtime.getRuntime().exec(buffer.toString());
            InputStream in = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            /*逐行读取视频文件的信息 只提取时间信息*/
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if (line.trim().startsWith("creation_time")) {
                    String[] creation_time = line.trim().split(" ");
                    String timeStr = creation_time[creation_time.length - 1];
                    String create_time = timeStr.replace("T", " ").split("\\.")[0];
                    Date createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(create_time);
                    createTime = new Date(createTime.getTime() + 8 * 3600 * 1000);
                    return createTime;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
package ppppp.evernote.util;

/**
 * @author pppppp
 * @date 2022/1/4 8:42
 */
import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 下载http对应URL的图片资源
 *
 */
public class DownloadImage{
    /**
     * 日志
     */
    protected  final Logger logger = Logger.getLogger(DownloadImage.class);

    private String ImageRootPath = "/data/application/tempImg";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        DownloadImage downloadImage = new DownloadImage();
        String httpUrlStr = "https://avatar.csdn.net/3/6/0/1_qq_25646191.jpg?1523447843";
        String fileName  = System.currentTimeMillis() + ".png";
        downloadImage.download(httpUrlStr, fileName);
    }

    /**
     * 下载图片
     * @param httpUrlStr
     * @param filename
     * @return
     */
    public String download(String httpUrlStr, String filename) {
        return download(httpUrlStr,filename,ImageRootPath);
    }

    @SuppressWarnings("finally")
    private String download(String httpUrlStr, String filename,String savePath){
        InputStream is = null;
        OutputStream os = null;
        String filePath = null;
        try {
            filePath = savePath + File.separator + filename;
            logger.info(String.format("下载图片... url = {%s}, filePath = {%s} ", httpUrlStr, filePath));
            // 构造URL
            URL url = new URL(httpUrlStr);
            // 打开连接
            URLConnection con = url.openConnection();
            //设置请求超时为5s
            con.setConnectTimeout(5*1000);
            // 输入流
            is = con.getInputStream();

            // 1K的数据缓冲
            byte[] bs = new byte[1024];
            // 读取到的数据长度
            int len;
            // 输出的文件流
            File sf=new File(savePath);
            if(!sf.exists()){
                sf.mkdirs();
            }
            os = new FileOutputStream(filePath);
            // 开始读取
            while ((len = is.read(bs)) != -1) {
                os.write(bs, 0, len);
            }
            logger.info(String.format("下载图片成功 图片存放路径 = {%s}", filePath));
        } catch (MalformedURLException e) {
            logger.error("下载图片失败 : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e){
            logger.error("普通异常下载图片失败 : " + e.getMessage());
        } finally {
            // 完毕，关闭所有链接
            try {
                if(null != os){
                    os.close();
                }
                if(null != is){
                    is.close();
                }
            } catch (IOException e) {
                logger.error("下载图片关闭流失败: " + e.getMessage());
            }
            return filePath;
        }
    }


    /**
     * 删除临时文件  注意，只能删除指定目录下面的临时文件
     * @param   fileName    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        boolean flag = false;
        try{
            String sPath = getImageRootPath() + File.separator + fileName;
            logger.info(String.format("删除临时文件 path = {%s} ", sPath));
            File file = new File(sPath);
            // 路径为文件且不为空则进行删除
            if (file.isFile() && file.exists()) {
                file.delete();
                flag = true;
            }
        } catch(Exception e){
            logger.info("删除文件异常 " + e.getMessage());
        }
        return flag;
    }

    public String getImageRootPath() {
        return ImageRootPath;
    }

    public void setImageRootPath(String ImageRootPath) {
        this.ImageRootPath = ImageRootPath;
    }
}

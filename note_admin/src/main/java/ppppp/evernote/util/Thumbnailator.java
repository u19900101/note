package ppppp.evernote.util;

import net.coobird.thumbnailator.Thumbnails;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

/**
 * @author pppppp
 * @date 2021/11/27 21:34
 */
public class Thumbnailator {
    /**
     * 重新生成图片宽、高
     * @param srcPath 图片路径
     * @param destPath 新生成的图片路径
     * @param newWith 新的宽度
     * @param newHeight 新的高度
     * @param forceSize 是否强制使用指定宽、高,false:会保持原图片宽高比例约束
     * @return
     * @throws IOException
     */
    public static boolean resizeImage (String srcPath, String destPath, int newWith, int newHeight, boolean forceSize) throws IOException {
        if (forceSize) {
            Thumbnails.of(srcPath).forceSize(newWith, newHeight).toFile(destPath);
        } else {
            Thumbnails.of(srcPath).width(newWith).height(newHeight).toFile(destPath);
        }
        return true;
    }
    @Test
    public void T_() throws IOException {
        OutputStream outstream =  new FileOutputStream("output");;
        String imageName = "a4.jpg";
        String IMAGE_PATH = "C:\\Users\\Administrator\\Desktop\\";
        String srcPath =  IMAGE_PATH + imageName;
        FileInputStream fileInputStream = new FileInputStream(new File(srcPath));


        Thumbnails.of(fileInputStream).width(400).toOutputStream(outstream);
        System.out.println();
    }
    /**
     * 测试重新生成图片宽、高
     * @throws IOException
     */
    @Test
    public void testResizeImage() throws IOException {
        String imageName = "a4.jpg";
        String IMAGE_PATH = "C:\\Users\\Administrator\\Desktop\\";
        String srcPath =  IMAGE_PATH + imageName;

        imageName = "java_coffee_resize.jpg";
        String destPath = IMAGE_PATH + imageName;
        boolean forceSize = false;
        Assert.assertTrue(resizeImage(srcPath, destPath, 200, 200, forceSize));
    }
}

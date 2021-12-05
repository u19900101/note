package ppppp.evernote.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.drew.imaging.ImageMetadataReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ppppp.evernote.entity.*;
import ppppp.evernote.mapper.PictureMapper;
import ppppp.evernote.service.NotebookService;
import ppppp.evernote.service.PictureService;
import ppppp.evernote.service.SortWayService;
import ppppp.evernote.service.TagService;
import ppppp.evernote.util.MyUtils;
import ppppp.evernote.util.ResultUtil;
import ppppp.evernote.util.ftp.sftp;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static ppppp.evernote.util.RequestUtils.sendGetRequest;
import static ppppp.evernote.util.ftp.sftp.deleteFiles;

/**
 * @author lppppp
 * @create 2021-02-01 10:36
 */
@RestController
@RequestMapping("/admin/file")
public class PictureController {
    private static ThreadLocal<sftp> sftpLocal = new ThreadLocal<sftp>();

    @Autowired
    PictureService pictureService;
    @Autowired
    SortWayService sortWayService;
    @Autowired
    TagService tagService;
    @Autowired
    NotebookService notebookService;
    @Autowired
    PictureMapper pictureMapper;

    /*文件上传*/
    @RequestMapping("/allFiles")
    public String getAllPictures() {
        List<Picture> pictureListList = getSortWayNotes();
        /*给照片封装标签数据*/
        fillPictureListList(pictureListList);
        return ResultUtil.successWithData(pictureListList);
    }

    private void fillPictureListList(List<Picture> pictureListList) {
        for (Picture picture : pictureListList) {
            if (picture.getTagUid() != null && picture.getTagUid().length() > 1) {
                for (String tagId : picture.getTagUid().split(",")) {
                    Tag tag = tagService.getById(tagId);
                    picture.getTagList().add(tag);
                }
            }
        }
    }

    /*获取回收站的图片*/
    @RequestMapping("/getWastepaperPictureList")
    public String getWastepaperPictureList() {
        List<Picture> pictureListList = pictureService.lambdaQuery().orderByDesc(Picture::getCreateTime).eq(Picture::getWastepaper, true).list();
        Sortway sortway = sortWayService.getById(1);
        if (sortway.getReverse()) {
            Collections.reverse(pictureListList);
        }
        return ResultUtil.successWithData(pictureListList);
    }

    // 恢复所有回收站中的照片
    @PostMapping("/recoverAllPictures")
    public String recoverAllPictures() {
        LambdaUpdateChainWrapper<Picture> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(pictureMapper);
        boolean update = lambdaUpdateChainWrapper.eq(Picture::getWastepaper, 1).set(Picture::getWastepaper, 0).update();
        if (update) {
            return getAllPictures();
        }
        return ResultUtil.errorWithMessage("失败 恢复所有回收站中的照片");
    }

    @PostMapping("/recoverSelectedPictures")
    public String recoverSelectedPictures(@RequestBody Object jsonpictureArrayIdList) {

        ArrayList<Integer> pictureArrayIdList = (ArrayList) jsonpictureArrayIdList;

        LambdaUpdateChainWrapper<Picture> lambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(pictureMapper);
        boolean update = lambdaUpdateChainWrapper.in(Picture::getId, pictureArrayIdList).set(Picture::getWastepaper, 0).update();
        if (update) {
            return getAllPictures();
        }
        return ResultUtil.errorWithMessage("失败 恢复所有回收站中的照片");
    }

    // 1.更新图片收藏 2.  3.
    @PostMapping("/update")
    public String updateImage(@RequestBody Picture picture) {
        picture.setUpdateTime(new Date());
        boolean updateById = pictureService.updateById(picture);
        return ResultUtil.successWithData(updateById);
    }

    private List<Picture> getSortWayNotes() {
        // 根据sortway中的排序字段进行查询
        Sortway sortway = sortWayService.getById(1);
        List<Picture> pictureList = null;

        //逆序
        if (sortway.getReverse()) {
            pictureList = pictureService.lambdaQuery().eq(Picture::getWastepaper, false).orderByDesc(Picture::getCreateTime).list(); /*last("limit 10").*/
        } else {
            pictureList = pictureService.lambdaQuery().eq(Picture::getWastepaper, false).orderByAsc(Picture::getCreateTime).list();
        }
        return pictureList;
    }

    /*每张照片都发送一次请求上传*/
    @PostMapping("/uploadFileAndInsert")
    public void uploadFileAndInsert(HttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multiRequest.getFile("file");
        Picture picture = getPictureInfo(multipartFile);
        String uploadDir = new SimpleDateFormat("yyyy-MM-dd").format(picture.getCreateTime());

        new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil("192.168.56.10", 22, "root", "vagrant");
                    String fileName = sftp.uploadFile("/mydata/nginx/html/img", "/" + uploadDir, multipartFile, picture.getTitle());
                    sftp.release();
                    if (!fileName.equals("error")) {
                        System.out.println("上传成功 " + fileName);

                        // 若重名，则重新命名文件
                        if (!fileName.equals(picture.getTitle())) {
                            picture.setTitle(fileName);
                        }
                        String imgUrl = "http://lpgogo.top/img/" + uploadDir + "/" + fileName;
                        picture.setUrl(imgUrl);
                        // 保存文件信息到数据库
                        boolean save = pictureService.save(picture);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @PostMapping("/deleteImage")
    public String deleteImage(@RequestBody Picture picture) {
        picture = pictureService.getById(picture.getId());
        // 1.更新 标签数量
        if (picture.getTagUid() != null) {
            // 新笔记本 +1
            String[] tagIdList = picture.getTagUid().split(",");
            for (String tagId : tagIdList) {
                Tag tag = tagService.getById(tagId);
                tag.setNoteCount(tag.getNoteCount() - 1);
                tagService.updateById(tag);
            }
        }

        boolean isLogicalDelete, isWastepaperSucceed;
        boolean isUpdateNoteBookCountSucceed = true;
        // 3.逻辑删除和状态修改
        Notebook wastepaperNotebook = notebookService.getById(10); //回收站id为10
        if (picture.getWastepaper()) {
            //  逻辑删除
            isLogicalDelete = pictureService.removeById(picture.getId());
            if(isLogicalDelete){ //在服务器上进行删除 http://lpgogo.top/img/2016-08-16/2.jpg
                ArrayList<String> stringArrayList = new ArrayList<>();
                stringArrayList.add(picture.getUrl().replace("http://lpgogo.top/img/",""));
                deleteImageFromServer(stringArrayList);
            }
            // 修改废纸篓的数量 -1
            isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() - 1));
        } else { // 状态修改
            // 2.更新 noteBook的数量
           /* isUpdateNoteBookCountSucceed = updateNoteCountWrapper(note.getPid(), -1);
            note.setWastepaper(true);
            isLogicalDelete = noteService.updateById(note);*/
            // 修改废纸篓的数量 +1
            isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() + 1));
        }
       /* if (isUpdateNoteBookCountSucceed && isLogicalDelete && isWastepaperSucceed) {
            return sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        }*/
        picture.setWastepaper(true);
        boolean update = pictureService.updateById(picture);
        return ResultUtil.successWithData(update);
    }

    private void deleteImageFromServer(ArrayList<String> paths) {

        new Thread() {
            @Override
            public void run() {
                try {
                    sftp.getSftpUtil("192.168.56.10", 22, "root", "vagrant");
                    Boolean deleteFiles = deleteFiles("/mydata/nginx/html/img", paths);
                    sftp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /*批量移入到回收站或者从回收站彻底删除*/
    @PostMapping("/deleteImageBatch")
    public String deleteImageBatch(@RequestBody Object jsonpictureArrayList) {
        boolean update = false;
        boolean isLogicalDelete = false;
        ArrayList<Integer> pictureArrayList = (ArrayList) jsonpictureArrayList;
        ArrayList<String> removeImagesPathList = new ArrayList<>();
        for (Integer picId : pictureArrayList) {
            // 1.更新 标签数量
            Picture picture = pictureService.getById(picId);
            if (picture.getTagUid() != null) {
                // 新笔记本 +1
                String[] tagIdList = picture.getTagUid().split(",");
                for (String tagId : tagIdList) {
                    Tag tag = tagService.getById(tagId);
                    tag.setNoteCount(tag.getNoteCount() - 1);
                    tagService.updateById(tag);
                }
            }

            boolean isWastepaperSucceed;
            boolean isUpdateNoteBookCountSucceed = true;
            // 3.逻辑删除和状态修改
            Notebook wastepaperNotebook = notebookService.getById(10); //回收站id为10
            if (picture.getWastepaper()) {
                //  逻辑删除
                isLogicalDelete = pictureService.removeById(picture.getId());

                removeImagesPathList.add(picture.getUrl().replace("http://lpgogo.top/img/",""));
                // 修改废纸篓的数量 -1
                isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() - 1));
                if (!isLogicalDelete) {
                    return ResultUtil.errorWithMessage("批量删除更新失败");
                }
            }
            else { // 状态修改
                // 2.更新 noteBook的数量
           /* isUpdateNoteBookCountSucceed = updateNoteCountWrapper(note.getPid(), -1);
            note.setWastepaper(true);
            isLogicalDelete = noteService.updateById(note);*/
                // 修改废纸篓的数量 +1
                isWastepaperSucceed = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() + 1));
                picture.setWastepaper(true);
                update = pictureService.updateById(picture);
                if (!update) {
                    return ResultUtil.errorWithMessage("批量删除更新失败");
                }
            }
       /* if (isUpdateNoteBookCountSucceed && isLogicalDelete && isWastepaperSucceed) {
            return sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        }*/

        }
        if(isLogicalDelete){
            deleteImageFromServer(removeImagesPathList);
            return ResultUtil.successWithMessage("彻底删除成功");
        }
        return getAllPictures();
    }

    private void execUpload(MultipartHttpServletRequest request) throws Exception {
        MultipartHttpServletRequest multiRequest = request;
        List<MultipartFile> fileList = multiRequest.getFiles("file");
        ArrayList<Map<String, Object>> item = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        // ArrayList<Picture> pictureArrayList = FTPUtil.upload(fileList);
        for (MultipartFile multipartFile : fileList) {

        }
    }


    // 获取文件的信息 保存到数据库 返回文件唯一的名称  2021-11-25-fileName-id.type
    public static Picture getPictureInfo(MultipartFile multipartFile) throws Exception {
        long size = multipartFile.getSize();
        // 得到照片的信息 0.照片的名称(规范时间信息)  1.坐标 2.拍摄时间(没有就为当前时间) 3.宽高
        Picture imgInfo = getImgInfo(multipartFile.getInputStream(), multipartFile.getOriginalFilename());
        imgInfo.setSize(size);
        return imgInfo;
    }

   /* @PostMapping("/uploadFile")
    public Map<String, Object> uploadFile(HttpServletRequest request) {

        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        List<MultipartFile> fileList = multiRequest.getFiles("file");
        ArrayList<Map<String, Object>> item = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (MultipartFile file : fileList) {
            Map<String, Object> temp = new HashMap<>();
            if (file.isEmpty()) {
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }

            String fileName = file.getOriginalFilename();
            // String filePath = "C:\\Users\\Administrator\\Desktop\\temp\\";
            // File dest = new File(filePath + fileName);
            try {
                // file.transferTo(dest); // 保存到本地
                byte[] bytes = file.getBytes();
                FTPUtil.sshSftp(bytes, file.getOriginalFilename());
                temp.put("fileName", fileName);
                temp.put("url", "http://lpgogo.top/img/" + fileName);
                item.add(temp);
            } catch (Exception e) {
                System.out.println("bug");
                map.put("uploaded", -1);
                map.put("errorMessage", "上传失败...");
                return map;
            }
        }

        map.put("uploaded", 1); //"上传成功"
        map.put("item", item);
        return map;
    }*/

    // 1.获取照片的拍摄时间 经度 纬度信息  宽高
    public static Picture getImgInfo(InputStream file, String fileName) throws Exception {

        Picture pic = new Picture();
        //如果你对图片的格式有限制，可以直接使用对应格式的Reader如：JPEGImageReader
        ImageMetadataReader.readMetadata(file)
                .getDirectories().forEach(v ->
                v.getTags().forEach(t -> {
                    switch (t.getTagName()) {
                        //  纬度
                        case "GPS Latitude":
                            pic.setLnglat(toX(t.getDescription()));
                            break;
                        // 经度
                        case "GPS Longitude":
                            pic.setLnglat(pic.getLnglat() + "," + toX(t.getDescription()));
                            break;
                        // 拍摄时间
                        // 解决有的照片中有两个 Date/Time Original 但是格式不一样
                        case "Date/Time Original":
                            // 修改格式
                            // 2016:08:16 09:33:17
                            // 2016-08-16T09:33:17 location: 100_33_11.27,36_33_34.23
                            String time = t.getDescription();
                            try {
                                Date date = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss").parse(time);
                                pic.setCreateTime(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        //    获取照片的尺寸 便于照片去重判断
                        case "Image Height":
                            if (pic.getWidthH() == null) {
                                pic.setWidthH(t.getDescription().split(" ")[0]);
                            } else {
                                pic.setWidthH(pic.getWidthH() + "x" + t.getDescription().split(" ")[0]);
                            }
                            break;
                        case "Image Width":
                            if (pic.getWidthH() == null) {
                                pic.setWidthH(t.getDescription().split(" ")[0]);
                            } else {
                                pic.setWidthH(t.getDescription().split(" ")[0] + "x" + pic.getWidthH());
                            }
                            break;
                        default:
                            break;
                    }
                })
        );

        String isContainCreate_time = MyUtils.nameToCreateTime(fileName);
        // 照片名称中含有时间信息   才对照片重命名 则将其作为照片的拍摄时间
        if (isContainCreate_time != null) {
            if (pic.getCreateTime() == null) {
                pic.setCreateTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(isContainCreate_time));
                fileName = isContainCreate_time.replace(" ", "_") + fileName.substring(fileName.lastIndexOf("."));
            }
        }

        // 照片本身不包含时间信息  将当前时间作为照片的创建时间
        if (pic.getCreateTime() == null) {
            pic.setCreateTime(new Date());
        }
        pic.setTitle(fileName);
        // String picStrId = MyUtils.createPicIdByAbsPath(fileAbspath);
        // todo 给照片生成唯一的指纹信息 用于相似照片的检测
        pic.setImguuid("picStrId");

        //通过经纬度 获取照片的位置信息
        if (pic.getLnglat() != null) {
            pic.setLocation(getLocation(pic.getLnglat()));
        }
        return pic;
    }

    //经纬度转地址
    public static String getLocation(String lnglat) {
        String key = "GjG3XAdmywz7CyETWqHwIuEC6ZExY6QT";
        String url = "http://api.map.baidu.com/geocoder/v2/?ak=" + key + "&output=json&coordtype=bd09ll&location=" + lnglat;
        String res = sendGetRequest(url);

        //获取详细地址
        String addressLocation = JSON.parseObject(res).getJSONObject("result").getString("formatted_address");
        return addressLocation;
    }

    // 将 度分秒 转化为小数坐标
    private static String toX(String description) {
        String[] l = description.split(" ");
        float d = Float.valueOf(l[0].substring(0, l[0].length() - 1));
        float f = Float.valueOf(l[1].substring(0, l[1].length() - 1));
        float m = Float.valueOf(l[2].substring(0, l[2].length() - 1));
        return String.valueOf(d + f / 60 + m / 3600);
    }


    public static HashMap<String, String> getVideoInfo(String videoPath) {
        //ffmepg工具地址
        String ffmpegPath = "D:\\MyJava\\mylifeImg\\src\\main\\resource\\ffmpeg.exe";
        //拼接cmd命令语句
        StringBuffer buffer = new StringBuffer();
        buffer.append(ffmpegPath);
        //注意要保留单词之间有空格
        buffer.append(" -i ");
        buffer.append(videoPath);
        //执行命令语句并返回执行结果
        HashMap<String, String> map = new HashMap<>();
        try {
            Process process = Runtime.getRuntime().exec(buffer.toString());
            InputStream in = process.getErrorStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("Duration:")) {
                    //根据字符匹配进行切割
                    // System.out.println("视频时间 = " + line.trim().substring(0,line.trim().indexOf(",")));
                    map.put("duration", line.trim().substring(0, line.trim().indexOf(",")).split(" ")[1]);
                }
                //一般包含fps的行就包含分辨率
                if (line.contains("fps")) {
                    //根据
                    String definition = line.split(",")[2];
                    definition = definition.trim().split(" ")[0];
                    // System.out.println("分辨率 = " + definition);
                    map.put("size", definition);
                }

                if (map.get("creation_time") == null && line.trim().startsWith("creation_time")) {
                    String[] creation_time = line.trim().split(" ");
                    String timeStr = creation_time[creation_time.length - 1];
                    String s = timeStr.replace("T", " ").split("\\.")[0];

                    // SimpleDateFormat bjSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");     // 北京
                    // bjSdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));  // 设置北京时区
                    // 直接将时间按伦敦时间解析为date
                    SimpleDateFormat londonSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 伦敦
                    londonSdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));  // 设置伦敦时区
                    Date dld = londonSdf.parse(s);

                    map.put("create_time", dld.toLocaleString());
                    // System.out.println(dld.toLocaleString());

                }

                if (map.get("location") == null && line.trim().startsWith("location")) {
                    //根据字符匹配进行切割
                    String[] location = line.trim().split("\\+");
                    map.put("location", location[2].replace("/", "") + "," + location[1]);
                    // System.out.println(line.trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /*public void checkAndCreateImg(String destDir, File uploadImgTemp,
                                  ArrayList<HashMap<String, Object>> successMapList,
                                  ArrayList<HashMap<String, Object>> failedMapList) throws ParseException, IOException, ImageProcessingException {
        //在整个数据库中进行 照片去重检查
        ArrayList<String> stringList = new ArrayList<>();
        HashMap<String,Object> map =new HashMap<>();
        MyUtils.getallpath(destDir,stringList);

        //判断图片在数据库中是否存在相似的照片
        int [] imgA = MyUtils.aHash(uploadImgTemp.getAbsolutePath());
        // 查询库中所有的 图片
        List<Picture> pictures = mapper.selectByExample(new PictureExample());
        // 将上传的图片与 现有的所有id进行比对
        Picture uploadPicture = getImgInfo(uploadImgTemp);
        map.put("uploadPicture", uploadPicture);

        //应对有可能的改变图片命名  产生的路径不存在问题
        uploadImgTemp = new File(tempimgDir,uploadPicture.getPath());
        boolean isExist = isPictureExist(uploadImgTemp,pictures,map,imgA);

        //存在
        if(isExist){
            System.out.println("  存在相同照片.....");
            // 判断 服务器中是否存在  不存在就移动  存在就不动
            File existImg = new File(baseDir+"\\"+uploadPicture.getPath().substring(uploadPicture.getPath().indexOf("img")));
            if(!existImg.exists()){
                MyUtils.copyFileUsingFileStreams(existImg.getAbsolutePath(), baseDir+"\\"+((Picture)map.get("existPicture")).getPath());
            }
            map.put("failedMsg","图片："+uploadImgTemp.getName()+"  疑似重复鸟！！");
            failedMapList.add(map);
        }
        // 不存在，按照片信息建立文件夹上传
        else {
            // 上传成功后马上将信息写入数据库 以免刷新时 的重复上传
            // 将照片信息放入map中 便于页面显示
            map.put("uploadPicture", uploadPicture);
            map.put("successMsg","图片："+uploadImgTemp.getName()+" 上传成功！！");
            successMapList.add(map);
        }
    }
  */  //在整个数据库中对比是否存在相似的照片
    /*private boolean isPictureExist(File srcFile, List<Picture> pictures, HashMap<String, Object> map, int[] imgA) throws IOException {
        long l = System.currentTimeMillis();
        int count = 0;
        for (Picture picture : pictures) {
            String s = picture.getPath();
            if(MyUtils.isImgType(s)){
                String picturePid = picture.getPid();
                int[] imgB = MyUtils.getIntNum(picturePid);
                double imageSimilar = MyUtils.diff(imgA, imgB);
                System.out.println("图片 ："+s+"与原图的相似度为： "+imageSimilar);
                count++;
                // 返回找到的第一张相似照片
                if(imageSimilar>IMAGE_SIMILARITY){
                    map.put("failedMsg","上传失败,存在相同照片.....");
                    // 将存在的照片信息放入map中 便于页面显示
                    map.put("existPicture", picture);
                    // 检测服务器上指定位置是否存在该文件
                    File isExistFile = new File(uploadimgDir.replace("img", ""),s);
                    if(!isExistFile.exists()){
                        //检测父文件夹是否存在
                        MyUtils.creatDir(isExistFile.getParent());
                        //只能复制，不可移动过去，不然 页面显示又找不到文件
                        MyUtils.copyFileUsingFileStreams(srcFile.getAbsolutePath(), isExistFile.getAbsolutePath());
                    }
                    System.out.println(("耗时："+(System.currentTimeMillis()-l)/1000)+" s 共检测照片 "+count+" 张");
                    return true;
                }
            }
        }
        System.out.println(("耗时："+(System.currentTimeMillis()-l)/1000)+" s 共检测照片 "+count+" 张");
        return false;
    }*/
    public static TreeMap<String, ArrayList<Picture>> groupPictureByMonth(List<Picture> pictures) {
        //将带时间的照片按月进行分组
        TreeMap<String, ArrayList<Picture>> map = new TreeMap<>(new Comparator<String>() {
            // 月份按照降序排列
            @Override
            public int compare(String o1, String o2) {
                return -o1.compareTo(o2);
            }
        });

        for (Picture picture : pictures) {
            // 有拍摄时间 信息的就按月分组
            if (picture.getCreateTime() != null) {
                // String month = picture.getCreateTime().substring(0, 7);
                String month = "";
                if (!map.containsKey(month)) {
                    ArrayList<Picture> pictureArrayList = new ArrayList<>();
                    pictureArrayList.add(picture);
                    map.put(month, pictureArrayList);
                } else {
                    ArrayList<Picture> pictureArrayList = map.get(month);
                    pictureArrayList.add(picture);
                }
            } else {
                if (!map.containsKey("神秘时间")) {
                    ArrayList<Picture> noneTimePicArrayList = new ArrayList<>();
                    map.put("神秘时间", noneTimePicArrayList);
                    noneTimePicArrayList.add(picture);
                } else {
                    ArrayList<Picture> noneTimePicArrayList = map.get("神秘时间");
                    noneTimePicArrayList.add(picture);
                }
            }

        }
        return map;
    }
}

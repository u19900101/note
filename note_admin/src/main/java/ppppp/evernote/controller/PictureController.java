package ppppp.evernote.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.drew.imaging.ImageMetadataReader;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ppppp.evernote.config.FtpConfig;
import ppppp.evernote.config.KnowFaceConfig;
import ppppp.evernote.entity.*;
import ppppp.evernote.mapper.FaceMapper;
import ppppp.evernote.mapper.PictureMapper;
import ppppp.evernote.service.*;
import ppppp.evernote.util.MyUtils;
import ppppp.evernote.util.RestTemplateUtil;
import ppppp.evernote.util.ResultUtil;
import ppppp.evernote.util.ftp.sftp;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ppppp.evernote.util.RequestUtils.sendGetRequest;
import static ppppp.evernote.util.ftp.sftp.deleteImageFromServer;

/**
 * 文件上传
 *
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
    ImageTagService imageTagService;
    @Autowired
    PictureMapper pictureMapper;
    @Autowired
    FaceService faceService;
    @Autowired
    FaceMapper faceMapper;
    @Autowired
    PersonService personService;
    @Autowired
    KnowFaceConfig knowFaceConfig;

    /*获取所有文件*/
    @RequestMapping("/allFiles")
    public String getAllPictures() {
        List<Picture> pictureListList = getSortWayNotes();
        /*给照片封装标签数据 和人脸数据*/
        fillPictureListList(pictureListList);
        return ResultUtil.successWithData(pictureListList);
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

    // 更新  1.收藏 2.名称  3.标签
    @PostMapping("/update")
    public String updateImage(@RequestBody Picture picture) {
        picture.setUpdateTime(new Date());
        boolean updateById = pictureService.updateById(picture);
        return ResultUtil.successWithData(updateById);
    }

    @Autowired
    FtpConfig ftpConfig;

    /*每张照片都发送一次请求上传*/
    @PostMapping("/uploadFileAndInsert")
    public String uploadFileAndInsert(HttpServletRequest request) throws Exception {
        ArrayList<Picture> res = new ArrayList<>();
        Picture picture = new Picture();
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multiRequest.getFile("file");
        String filename = multipartFile.getOriginalFilename();
        File videoFile = null;
        /*封装上传的文件*/
        if (filename.endsWith(".mp4") || filename.endsWith(".avi") || filename.endsWith(".flv")) {
            ArrayList<Object> videoInfo = getVideoInfo(multipartFile);
            picture = (Picture) videoInfo.get(0);
            if (picture.getCreateTime() == null) {
                picture.setCreateTime(new Date());
            }
            videoFile = (File) videoInfo.get(1);
        } else {
            picture = getPictureInfo(multipartFile);
        }

        String uploadDir = new SimpleDateFormat("yyyy-MM-dd").format(picture.getCreateTime());
        Picture finalPicture = picture;
        File finalVideoFile = videoFile;
        Thread thread = new Thread(() -> {
            try {
                /*上传视频*/
                String fileName = "";
                String faceUid = "";
                if (finalVideoFile != null) {
                    fileName = sftp.uploadVideo(finalVideoFile, uploadDir, multipartFile, finalPicture.getTitle());
                } else {
                    fileName = sftp.uploadFile(uploadDir, multipartFile, finalPicture.getTitle());
                }
                sftp.release();
                if (!fileName.equals("error")) {
                    System.out.println("上传成功 " + fileName);
                    // 若重名，则重新命名文件
                    if (!fileName.equals(finalPicture.getTitle())) {
                        finalPicture.setTitle(fileName);
                    }
                    // String imgUrl = "http://lpgogo.top/img/" + uploadDir + "/" + fileName;
                    String imgUrl = "http://47.101.137.245/img/" + uploadDir + "/" + fileName;
                    finalPicture.setUrl(imgUrl);
                    // 保存文件信息到数据库
                    boolean save = pictureService.save(finalPicture);
                    if (save) {
                        res.add(finalPicture);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        /*启动另一个线程异步执行*/
        /*进行人脸识别 若存在人脸则写入face表中 并且更新picture表*/
        if (finalVideoFile == null) {
            new Thread(() -> {
                try {
                    faceRecognition(finalPicture.getUrl(), finalPicture.getId());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        if (res.size() == 1) {
            return ResultUtil.successWithData(res.get(0));
        }

        return ResultUtil.errorWithMessage("上传错误...");
    }

    /*进行人脸识别 若存在人脸则写入face表中 并且更新picture表*/
    private void faceRecognition(String imageUrl, Integer pictureId) throws IOException, InterruptedException {
        /*1.检测known_face.txt是否需要更新*/
        checkKnowFaceData();
        // multipartFile.transferTo(pictureFile);
        // String imgAbsPath = pictureFile.getAbsolutePath();
        ArrayList<Face> faceArrayList = getFaceFromServer(imageUrl);
        /*将人脸批量写进数据库  并更新表picture中 face_uid*/
        if (faceArrayList != null) {
            for (Face face : faceArrayList) {
                face.setPictureId(pictureId);
            }
            /*保存人脸到数据库*/
            boolean b = faceService.saveBatch(faceArrayList);
            /*更新对应的picture*/
            if (b) {
                String faceUid = "";
                for (Face face : faceArrayList) {
                    int faceNameId = face.getPersonId();
                    /*若出现新的面孔 则创建新的faceName*/
                    updatePerson(faceNameId, pictureId);
                    faceUid += face.getId() + ",";
                }
                Picture picture = pictureMapper.selectById(pictureId);
                picture.setFaceUid(faceUid);
                pictureService.updateById(picture);
            }
        }
    }

    public ArrayList<Face> getFaceFromServer(String imageUrl) {

        //1. 简单Get请求
        String rootUrl = "http://47.101.137.245:5000";
        // String imageUrl = "http://47.101.137.245/img/l.jpg";
        System.out.println("发送请求");
        HashMap<String, Object> res = RestTemplateUtil.getInstance().getForObject(rootUrl + "?imageUrl=" + imageUrl, HashMap.class);
        int faceNum = (int) res.get("faceNum");
        /*判断是否检测到了人脸*/
        if (faceNum == 0) {
            System.out.println("未检测到人脸");
            return null;
        }

        ArrayList<Face> faces = new ArrayList<>();
        ArrayList<String> face_name_ids = (ArrayList<String>) res.get("face_name_ids");
        ArrayList<ArrayList<String>> face_encodings = (ArrayList<ArrayList<String>>) res.get("face_encodings");
        ArrayList<ArrayList<String>> face_locations = (ArrayList<ArrayList<String>>) res.get("face_locations");
        ArrayList<ArrayList<String>> face_landmarks = (ArrayList<ArrayList<String>>) res.get("face_landmarks");

        /*对齐的人脸路径*/
        ArrayList<String> face_urls = (ArrayList) res.get("face_urls");

        /*将人脸封装为单张*/
        for (int i = 0; i < faceNum; i++) {
            Face face = new Face();
            face.setPersonId(Integer.valueOf(JSONObject.toJSONString(face_name_ids.get(i))));
            face.setFaceEncoding(JSONObject.toJSONString(face_encodings.get(i)));
            face.setFaceLandmarks(JSONObject.toJSONString(face_landmarks.get(i)));
            face.setFaceLocations(JSONObject.toJSONString(face_locations.get(i)));
            face.setUrl(face_urls.get(i));
            faces.add(face);
        }
        return faces;
    }


    /*检测known_face.txt是否需要更新 因为删除face后，不更新data，改为识别之前进行更新*/
    public void checkKnowFaceData() throws IOException {
        int count = faceService.count();
        BufferedReader reader = new BufferedReader(new FileReader(knowFaceConfig.getKnownFaceIdsPath()));
        String readLine = reader.readLine().replaceAll(" +", " ");
        if (readLine != null) {
            String[] line = readLine.split(" ");
            /*若两者不相等 重写文件*/
            if (count != line.length) {
                reWriteKnownFace();
            } else {
                //    判断 是否需要重写 face_id.txt
                //    两者的顺序是否一致 因为有可能手动调整了person_id
                List<Face> faceList = faceService.lambdaQuery().select(Face::getPersonId).list();
                StringBuilder s = new StringBuilder();
                faceList.forEach((f) -> s.append(f.getPersonId() + " "));
                if (!s.toString().equalsIgnoreCase(readLine)) {
                    reWriteKnownFaceIds();
                }
            }
        }
    }


    /*更新人物的pid 或新建人物*/
    private void updatePerson(int faceNameId, int pictureId) {
        Person person = personService.getById(faceNameId);
        //新建人物
        if (person == null) {
            personService.save(new Person(faceNameId, "添加姓名", 1, pictureId + ","));
        } else {// 更新人物
            person.setPictureUid(person.getPictureUid() + pictureId + ",");
            person.setCount(person.getCount() + 1);
            personService.updateById(person);
        }
    }


    public ArrayList<Face> getFaceMethod(String imgAbsPath) {

        ArrayList<Face> faces = new ArrayList<>();
        /*1.人脸识别*/
        /*2.人类比对*/

        /*使用已经得到的人脸作为人脸数据库*/

        /* 人脸编码：128*1 人脸位置： 矩形  特征点位置：72*2  */
        // String known_face_encodings = "[[-0.022834885865449905, -0.013802939094603062, 0.0029389522969722748, -0.042331527918577194, -0.036763351410627365, -0.06913075596094131, -0.04332069307565689, -0.11545366048812866, 0.12632925808429718, -0.0907406359910965, 0.23085357248783112, -0.09232445061206818, -0.2305309772491455, -0.10378826409578323, -0.057692039757966995, 0.18100687861442566, -0.13906297087669373, -0.12566958367824554, -0.05405247211456299, 0.030917704105377197, 0.09482577443122864, -0.02022041380405426, 0.0053490400314331055, 0.06884001195430756, -0.1259840577840805, -0.34013086557388306, -0.13103780150413513, -0.08421936631202698, -0.05631003901362419, -0.07067498564720154, -0.045991212129592896, 0.11862901598215103, -0.13752105832099915, 0.009470473974943161, 0.041768819093704224, 0.04796783998608589, 0.03485319763422012, -0.05196763575077057, 0.16419541835784912, 0.014049118384718895, -0.2810373306274414, 0.02937339060008526, 0.025483455508947372, 0.2228596806526184, 0.1734302043914795, 0.006144563667476177, -0.0075507014989852905, -0.15715812146663666, 0.10663694143295288, -0.1399655044078827, 0.02266889251768589, 0.10937392711639404, 0.09286586195230484, 0.039662349969148636, 0.012870749458670616, -0.09817356616258621, 0.012453965842723846, 0.1209140419960022, -0.16984297335147858, -0.031018204987049103, 0.015731502324342728, -0.13375996053218842, -0.044117819517850876, -0.11635033041238785, 0.1727331131696701, 0.07557569444179535, -0.12115654349327087, -0.190398707985878, 0.14663854241371155, -0.15190596878528595, -0.05025138705968857, 0.11840397119522095, -0.1535266935825348, -0.1887577623128891, -0.3106735348701477, -0.011321647092700005, 0.341266930103302, 0.08348941802978516, -0.23574550449848175, 0.04638682305812836, -0.0630284994840622, 0.015792790800333023, 0.14515501260757446, 0.1287955641746521, 0.04141320660710335, 0.019672930240631104, -0.10981252789497375, -0.01749645173549652, 0.21438078582286835, -0.11450610309839249, -0.017947107553482056, 0.2067253738641739, -0.03470892831683159, 0.08493780344724655, 0.006373962387442589, 0.01252283900976181, -0.0457242988049984, 0.055555276572704315, -0.1166352778673172, -0.012638650834560394, 0.03447915241122246, 0.024171169847249985, -0.005052044987678528, 0.08594578504562378, -0.11098217964172363, 0.07693938910961151, 0.037946466356515884, 0.009359689429402351, 0.0562986359000206, -0.004606084898114204, -0.07443134486675262, -0.11625683307647705, 0.10372993350028992, -0.27965906262397766, 0.1578962802886963, 0.13240957260131836, 0.08105415850877762, 0.07586409151554108, 0.03973495960235596, 0.0873270034790039, -0.06345000118017197, -0.07950236648321152, -0.23084065318107605, 0.04661783576011658, 0.15054501593112946, 0.009585132822394371, 0.03438431769609451, -0.053176186978816986], [-0.11825861036777496, 0.04915862902998924, 0.06903685629367828, -0.05819728225469589, -0.08897768706083298, -0.07185576856136322, -0.046024177223443985, -0.057649292051792145, 0.15126483142375946, -0.09419794380664825, 0.2461787611246109, -0.12812389433383942, -0.2592028081417084, -0.09480959177017212, -0.025718316435813904, 0.21543969213962555, -0.21477481722831726, -0.07726842910051346, 0.0126909539103508, 0.01519523561000824, 0.13964277505874634, 0.0017896723002195358, 0.03144191578030586, 0.05953390151262283, -0.11791184544563293, -0.42182785272598267, -0.10884703695774078, -0.0725506842136383, 0.03154096007347107, -0.051472507417201996, -0.06291381269693375, 0.02651319094002247, -0.1882096529006958, -0.07922888547182083, 0.0029536783695220947, 0.06672757118940353, -0.002278439234942198, -0.07611808180809021, 0.18582786619663239, 0.009779013693332672, -0.290788471698761, -0.015116099268198013, 0.029516976326704025, 0.23993287980556488, 0.17168456315994263, 0.08887393027544022, 0.03002234920859337, -0.11360159516334534, 0.06645035743713379, -0.19399502873420715, 0.024545123800635338, 0.11290828138589859, 0.08012722432613373, 0.006480492651462555, -0.012488525360822678, -0.13372091948986053, 0.05424286425113678, 0.10679683089256287, -0.22681382298469543, -0.026030708104372025, 0.09847485274076462, -0.0906519740819931, 0.05620303750038147, -0.0034779682755470276, 0.26015040278434753, 0.044274214655160904, -0.10519144684076309, -0.17828619480133057, 0.13565678894519806, -0.16894714534282684, -0.037980686873197556, 0.10181944817304611, -0.12010770291090012, -0.17593519389629364, -0.4007662832736969, -0.04275020211935043, 0.4299006462097168, 0.08757861703634262, -0.1626228243112564, 0.04045096039772034, -0.1241329163312912, -0.00195382721722126, 0.19166743755340576, 0.13449473679065704, -0.0008216984570026398, 0.024199917912483215, -0.15272094309329987, -0.016157254576683044, 0.21668848395347595, -0.07670328766107559, -0.056074343621730804, 0.2123553454875946, 0.04272289574146271, 0.08253134042024612, 0.017842618748545647, 0.03899769484996796, -0.08220463991165161, 0.032945021986961365, -0.0822468250989914, -0.015029087662696838, 0.03663666918873787, -0.00619914848357439, 0.008278325200080872, 0.14852744340896606, -0.2229294627904892, 0.1248430460691452, 0.016504459083080292, -0.010622246190905571, 0.010256370529532433, 0.006239213049411774, -0.10829469561576843, -0.07655877619981766, 0.10261870920658112, -0.29181498289108276, 0.12983360886573792, 0.22180181741714478, 0.06778649985790253, 0.13137762248516083, 0.0912918895483017, 0.03684549033641815, -0.04259098693728447, -0.061904631555080414, -0.22649110853672028, 0.0025956295430660248, 0.08619814366102219, -0.029610704630613327, 0.15119017660617828, 0.024150971323251724], [-0.022834885865449905, -0.013802939094603062, 0.0029389522969722748, -0.042331527918577194, -0.036763351410627365, -0.06913075596094131, -0.04332069307565689, -0.11545366048812866, 0.12632925808429718, -0.0907406359910965, 0.23085357248783112, -0.09232445061206818, -0.2305309772491455, -0.10378826409578323, -0.057692039757966995, 0.18100687861442566, -0.13906297087669373, -0.12566958367824554, -0.05405247211456299, 0.030917704105377197, 0.09482577443122864, -0.02022041380405426, 0.0053490400314331055, 0.06884001195430756, -0.1259840577840805, -0.34013086557388306, -0.13103780150413513, -0.08421936631202698, -0.05631003901362419, -0.07067498564720154, -0.045991212129592896, 0.11862901598215103, -0.13752105832099915, 0.009470473974943161, 0.041768819093704224, 0.04796783998608589, 0.03485319763422012, -0.05196763575077057, 0.16419541835784912, 0.014049118384718895, -0.2810373306274414, 0.02937339060008526, 0.025483455508947372, 0.2228596806526184, 0.1734302043914795, 0.006144563667476177, -0.0075507014989852905, -0.15715812146663666, 0.10663694143295288, -0.1399655044078827, 0.02266889251768589, 0.10937392711639404, 0.09286586195230484, 0.039662349969148636, 0.012870749458670616, -0.09817356616258621, 0.012453965842723846, 0.1209140419960022, -0.16984297335147858, -0.031018204987049103, 0.015731502324342728, -0.13375996053218842, -0.044117819517850876, -0.11635033041238785, 0.1727331131696701, 0.07557569444179535, -0.12115654349327087, -0.190398707985878, 0.14663854241371155, -0.15190596878528595, -0.05025138705968857, 0.11840397119522095, -0.1535266935825348, -0.1887577623128891, -0.3106735348701477, -0.011321647092700005, 0.341266930103302, 0.08348941802978516, -0.23574550449848175, 0.04638682305812836, -0.0630284994840622, 0.015792790800333023, 0.14515501260757446, 0.1287955641746521, 0.04141320660710335, 0.019672930240631104, -0.10981252789497375, -0.01749645173549652, 0.21438078582286835, -0.11450610309839249, -0.017947107553482056, 0.2067253738641739, -0.03470892831683159, 0.08493780344724655, 0.006373962387442589, 0.01252283900976181, -0.0457242988049984, 0.055555276572704315, -0.1166352778673172, -0.012638650834560394, 0.03447915241122246, 0.024171169847249985, -0.005052044987678528, 0.08594578504562378, -0.11098217964172363, 0.07693938910961151, 0.037946466356515884, 0.009359689429402351, 0.0562986359000206, -0.004606084898114204, -0.07443134486675262, -0.11625683307647705, 0.10372993350028992, -0.27965906262397766, 0.1578962802886963, 0.13240957260131836, 0.08105415850877762, 0.07586409151554108, 0.03973495960235596, 0.0873270034790039, -0.06345000118017197, -0.07950236648321152, -0.23084065318107605, 0.04661783576011658, 0.15054501593112946, 0.009585132822394371, 0.03438431769609451, -0.053176186978816986], [-0.11825861036777496, 0.04915862902998924, 0.06903685629367828, -0.05819728225469589, -0.08897768706083298, -0.07185576856136322, -0.046024177223443985, -0.057649292051792145, 0.15126483142375946, -0.09419794380664825, 0.2461787611246109, -0.12812389433383942, -0.2592028081417084, -0.09480959177017212, -0.025718316435813904, 0.21543969213962555, -0.21477481722831726, -0.07726842910051346, 0.0126909539103508, 0.01519523561000824, 0.13964277505874634, 0.0017896723002195358, 0.03144191578030586, 0.05953390151262283, -0.11791184544563293, -0.42182785272598267, -0.10884703695774078, -0.0725506842136383, 0.03154096007347107, -0.051472507417201996, -0.06291381269693375, 0.02651319094002247, -0.1882096529006958, -0.07922888547182083, 0.0029536783695220947, 0.06672757118940353, -0.002278439234942198, -0.07611808180809021, 0.18582786619663239, 0.009779013693332672, -0.290788471698761, -0.015116099268198013, 0.029516976326704025, 0.23993287980556488, 0.17168456315994263, 0.08887393027544022, 0.03002234920859337, -0.11360159516334534, 0.06645035743713379, -0.19399502873420715, 0.024545123800635338, 0.11290828138589859, 0.08012722432613373, 0.006480492651462555, -0.012488525360822678, -0.13372091948986053, 0.05424286425113678, 0.10679683089256287, -0.22681382298469543, -0.026030708104372025, 0.09847485274076462, -0.0906519740819931, 0.05620303750038147, -0.0034779682755470276, 0.26015040278434753, 0.044274214655160904, -0.10519144684076309, -0.17828619480133057, 0.13565678894519806, -0.16894714534282684, -0.037980686873197556, 0.10181944817304611, -0.12010770291090012, -0.17593519389629364, -0.4007662832736969, -0.04275020211935043, 0.4299006462097168, 0.08757861703634262, -0.1626228243112564, 0.04045096039772034, -0.1241329163312912, -0.00195382721722126, 0.19166743755340576, 0.13449473679065704, -0.0008216984570026398, 0.024199917912483215, -0.15272094309329987, -0.016157254576683044, 0.21668848395347595, -0.07670328766107559, -0.056074343621730804, 0.2123553454875946, 0.04272289574146271, 0.08253134042024612, 0.017842618748545647, 0.03899769484996796, -0.08220463991165161, 0.032945021986961365, -0.0822468250989914, -0.015029087662696838, 0.03663666918873787, -0.00619914848357439, 0.008278325200080872, 0.14852744340896606, -0.2229294627904892, 0.1248430460691452, 0.016504459083080292, -0.010622246190905571, 0.010256370529532433, 0.006239213049411774, -0.10829469561576843, -0.07655877619981766, 0.10261870920658112, -0.29181498289108276, 0.12983360886573792, 0.22180181741714478, 0.06778649985790253, 0.13137762248516083, 0.0912918895483017, 0.03684549033641815, -0.04259098693728447, -0.061904631555080414, -0.22649110853672028, 0.0025956295430660248, 0.08619814366102219, -0.029610704630613327, 0.15119017660617828, 0.024150971323251724]]";
        // String known_face_ids = "[0, 1, 0, 1]";

        try {
            Resource resource = new ClassPathResource("pyresources\\getFaceInfo.py");
            //获1.txt的取相对路径
            String pyFilePath = resource.getFile().getPath();
            // String pyFilePath = "D:\\MyMind\\note\\data\\pythonModule\\python\\getFaceInfo.py";
            //要指定python的环境 不然使用的系统默认
            String[] args = new String[]{"D:\\MyMind\\note\\venv\\Scripts\\python",
                    pyFilePath, imgAbsPath}; /*, known_face_encodings, known_face_ids*/
            Process proc = Runtime.getRuntime().exec(args);// 执行py文件
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            ArrayList<String> face_encodings = new ArrayList<>();
            ArrayList<String> face_locations = new ArrayList<>();
            ArrayList<String> face_landmarks = new ArrayList<>();
            String[] face_name_ids = null;


            int faceNum = 0;
            /*判断是否检测到了人脸*/
            if ((line = in.readLine()) != null) {
                if (Integer.valueOf(line) == 0) {
                    System.out.println("未检测到人脸");
                    return null;
                }
                faceNum = Integer.parseInt(line);
            }
            /*face_name_ids*/
            if ((line = in.readLine()) != null) {
                face_name_ids = line.replaceAll(" |\\[|\\]", "").split(",");
            }
            /*face_encodings*/
            if ((line = in.readLine()) != null) {
                /*可能存在 - 号开头的情况*/
                getByReg("\\[\\-?\\d+.*?\\]", face_encodings, line);
            }
            /*face_locations*/
            if ((line = in.readLine()) != null) {
                getByReg("\\[\\d+.*?\\]", face_locations, line);
            }
            /*face_landmarks*/
            if ((line = in.readLine()) != null) {
                getByReg("\\[{2}\\d+.*?\\]{2}", face_landmarks, line);
            }

            /*对齐的人脸路径*/
            String[] face_urls = null;
            if ((line = in.readLine()) != null) {
                //temp_1.jpg,temp_2.jpg,    String absPre = "D:\\MyMind\\note\\data\\pythonModule\\python\\";
                face_urls = line.split(",");
            }

            /*将人脸封装为单张*/
            for (int i = 0; i < faceNum; i++) {
                Face face = new Face();
                face.setPersonId(Integer.valueOf(face_name_ids[i]));
                face.setFaceEncoding(face_encodings.get(i));
                face.setFaceLandmarks(face_landmarks.get(i));
                face.setFaceLocations(face_locations.get(i));
                face.setUrl(face_urls[i]);
                faces.add(face);
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        return faces;
    }

    private void getByReg(String reg, ArrayList<String> arr, String content) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while (m.find()) {
            arr.add(content.substring(m.start(), m.end()));
        }
    }

    @PostMapping("/deleteImage")
    public String deleteImage(@RequestBody Picture picture) throws InterruptedException {
        picture = pictureService.getById(picture.getId());
        // 1.更新 标签数量
        if (picture.getTagUid() != null) {
            if (picture.getTagUid() != null && picture.getTagUid().length() > 1) {
                String[] tagIdList = picture.getTagUid().split(",");
                for (String tagId : tagIdList) {
                    ImageTag tag = imageTagService.getById(Integer.parseInt(tagId));
                    tag.setNoteCount(tag.getNoteCount() - 1);
                    imageTagService.updateById(tag);
                }
            }
        }

        boolean isLogicalDelete, isWastepaperSucceed;
        boolean isUpdateNoteBookCountSucceed = true;
        // 3.逻辑删除和状态修改
        Notebook wastepaperNotebook = notebookService.getById(10); //回收站id为10
        if (picture.getWastepaper()) {
            //  逻辑删除
            isLogicalDelete = pictureService.removeById(picture.getId());
            if (isLogicalDelete) { //在服务器上进行删除 http://lpgogo.top/img/2016-08-16/2.jpg
                ArrayList<String> stringArrayList = new ArrayList<>();
                // stringArrayList.add(picture.getUrl().replace("http://lpgogo.top/img/", ""));
                stringArrayList.add(picture.getUrl().replace("http://47.101.137.245/img/", ""));
                deleteImageFromServer(stringArrayList, true);
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

        picture.setWastepaper(true);
        boolean update = pictureService.updateById(picture);
        return ResultUtil.successWithData(update);
    }


    /*批量移入到回收站或者从回收站彻底删除*/
    @PostMapping("/deleteImageBatch")
    public String delete(@RequestBody HashMap obj) throws IOException, InterruptedException {

        ArrayList<Integer> deleteImageIds = (ArrayList) obj.get("deleteIds");
        String type = (String) obj.get("type");
        /*彻底删除*/
        if (type.equals("delete")) {
            boolean deleteImageBatch = deleteImageBatch(deleteImageIds);
            return ResultUtil.errorWithMessage(deleteImageBatch ? "彻底删除成功" : "批量删除更新失败");
        }/*移入到回收站*/ else {
            boolean move = moveToCycleBin(deleteImageIds);
            if (!move) {
                return ResultUtil.errorWithMessage("移动到回收站失败");
            }
        }
        return getAllPictures();
    }

    private boolean moveToCycleBin(ArrayList<Integer> deleteImageIds) {
        /*1.找到要移动的照片*/
        List<Picture> pictureList = pictureMapper.selectBatchIds(deleteImageIds);

        /*2.更新imageTag相关*/
        updateImageTag(pictureList);

        /*3.修改废纸篓的数量 + n*/
        Notebook wastepaperNotebook = notebookService.getById(10); //回收站id为10
        boolean updateWastePaper = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() + deleteImageIds.size()));

        /*4.直接修改 waste字段*/
        for (Picture picture : pictureList) {
            picture.setWastepaper(true);
        }
        boolean updateBatchById = pictureService.updateBatchById(pictureList);
        return updateWastePaper && updateBatchById;
    }

    /*批量彻底删除照片*/
    private boolean deleteImageBatch(ArrayList<Integer> deleteImageIds) throws IOException, InterruptedException {

        /*1.找到要删除照片*/
        List<Picture> pictureList = pictureMapper.selectBatchIds(deleteImageIds);

        /*6.从服务器删除照片*/
        ArrayList<String> deleteImageUrls = new ArrayList<>();
        ArrayList<String> deleteFaceUrls = new ArrayList<>();
        for (Picture picture : pictureList) {
            // 1.更新 标签数量
            // deleteImageUrls.add(picture.getUrl().replace("http://lpgogo.top/img/", ""));
            deleteImageUrls.add(picture.getUrl().replace("http://47.101.137.245/img/", ""));
            String faceUid = picture.getFaceUid();
            if( faceUid != null && faceUid.length() > 1){
                String[] faceIds = faceUid.split(",");
                List<Face> faceList = faceService.lambdaQuery().select(Face::getUrl).in(Face::getId,Arrays.asList(faceIds)).list();
                faceList.forEach(face ->  deleteFaceUrls.add(face.getUrl().replace("http://47.101.137.245/face/", "")));
            }

        }

        boolean deleteImageFromServer = deleteImageFromServer(deleteImageUrls, deleteFaceUrls);

        int deleteBatchIds = -1;
        boolean updateWastePaper = false;
        /*从服务删除成功后才从操作数据库*/
        if (deleteImageFromServer) {
            /*2.更新face表 3.重写known_face文件 再删除照片*/
            updateFaceTable(pictureList);

            /*4.更新imageTag相关*/
            updateImageTag(pictureList);

            /*3.修改废纸篓的数量*/
            Notebook wastepaperNotebook = notebookService.getById(10); //回收站id为10
            updateWastePaper = notebookService.updateById(wastepaperNotebook.setNoteCount(wastepaperNotebook.getNoteCount() - deleteImageIds.size()));

            /*5.批量删除照片*/
            deleteBatchIds = pictureMapper.deleteBatchIds(deleteImageIds);
        }
        return updateWastePaper && deleteBatchIds == deleteImageIds.size();

    }



    private void updateImageTag(List<Picture> pictureList) {
        for (Picture picture : pictureList) {
            if (picture.getTagUid() != null) {
                String[] tagIdList = picture.getTagUid().split(",");
                for (String tagId : tagIdList) {
                    ImageTag tag = imageTagService.getById(tagId);
                    tag.setNoteCount(tag.getNoteCount() - 1);
                    imageTagService.updateById(tag);
                }
            }
        }
    }


    private void updateFaceTable(List<Picture> pictureList) throws IOException, InterruptedException {
        for (Picture picture : pictureList) {
            String faceUid = picture.getFaceUid();
            if (faceUid != null && faceUid.length() > 1) {
                /*a.删除数据量中图片对应的face数据*/
                ArrayList<String> faceUids = new ArrayList<>(Arrays.asList(faceUid.split(",")));

                /*b.从服务器删除人脸文件*/
                ArrayList<String> deleteFaceUrls = new ArrayList<>();
                List<Face> faces = faceMapper.selectBatchIds(faceUids);
                for (Face f : faces) {
                    // 1.更新 标签数量
                    // deleteFaceUrls.add(f.getUrl().replace("http://lpgogo.top/img/face/", ""));
                    deleteFaceUrls.add(f.getUrl().replace("http://47.101.137.245/face/", ""));
                    /*2.更新人物 将pid从人物中移除 若移除后无pid 则将该人物删除*/
                    updatePersonByPid(f.getPersonId(), picture.getId());
                }
                boolean deleteImageFromServer = deleteImageFromServer(deleteFaceUrls, false);
                faceMapper.deleteBatchIds(faceUids);
            }
        }
    }

    private void updatePersonByPid(Integer faceNameId, Integer pictureId) {
        Person person = personService.getById(faceNameId);
        String replaceAll = person.getPictureUid().replaceAll(pictureId + ",", "");
        /*当人物pid为空时 删除*/
        if (replaceAll.length() == 0) {
            personService.removeById(faceNameId);
        } else {
            person.setPictureUid(replaceAll);
            person.setCount(person.getCount() - 1);
            personService.updateById(person);
        }

    }

    public void reWriteKnownFace() {
        List<Face> knownFaces = faceService.lambdaQuery().select(Face::getPersonId, Face::getFaceEncoding).list();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(knowFaceConfig.getKnownFaceEncodingsPath()));
            BufferedWriter out2 = new BufferedWriter(new FileWriter(knowFaceConfig.getKnownFaceIdsPath()));
            for (Face face : knownFaces) {
                out.write(face.getFaceEncoding().replaceAll(" |\\[|\\]", "").replace(",", " ") + "\n");
                out2.write(face.getPersonId() + " ");
            }
            out.close();
            out2.close();
            System.out.println("success reWriteKnownFace！");
        } catch (IOException e) {
            System.out.println("failed reWriteKnownFace");
        }
    }

    /*重写ids*/
    public void reWriteKnownFaceIds() {
        List<Face> knownFaces = faceService.lambdaQuery().select(Face::getPersonId).list();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(knowFaceConfig.getKnownFaceIdsPath()));
            for (Face face : knownFaces) {
                out.write(face.getPersonId() + " ");
            }
            out.close();
            System.out.println("success reWriteKnownFace！");
        } catch (IOException e) {
            System.out.println("failed reWriteKnownFace");
        }
    }


    private void fillPictureListList(List<Picture> pictureListList) {
        for (Picture picture : pictureListList) {
            if (picture.getTagUid() != null && picture.getTagUid().length() > 1) {
                for (String tagId : picture.getTagUid().split(",")) {
                    ImageTag tag = imageTagService.getById(tagId);
                    picture.getTagList().add(tag);
                }
            }

            /*封装所有人脸*/
            if (picture.getFaceUid() != null && picture.getFaceUid().length() > 1) {
                for (String faceId : picture.getFaceUid().split(",")) {
                    Face face = faceService.getById(faceId);
                    if (face != null) {
                        /*不封装人脸编码字段*/
                        face.setFaceEncoding(null);
                        List<Face> faceListTemp = picture.getFaceList();
                        faceListTemp.add(face);
                        picture.setFaceList(faceListTemp);
                    }
                }
            }
        }
    }

    private List<Picture> getSortWayNotes() {
        // 根据sortway中的排序字段进行查询
        Sortway sortway = sortWayService.getById(1);
        List<Picture> pictureList = null;

        //逆序
        if (sortway.getCreateTime()) {
            pictureList = pictureService.lambdaQuery().eq(Picture::getWastepaper, false).orderByDesc(Picture::getCreateTime).list(); /*.last("limit 10").*/
        } else if (sortway.getUpdateTime()) {
            pictureList = pictureService.lambdaQuery().eq(Picture::getWastepaper, false).orderByDesc(Picture::getUpdateTime).list();
        }
        //逆序
        if (sortway.getReverse()) {
            Collections.reverse(pictureList);
        }

        return pictureList;
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
                            pic.setLnglat(toX(t.getDescription()) + "," + pic.getLnglat());
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
        lnglat = lnglat.split(",")[1] + "," + lnglat.split(",")[0];
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


    /*获取视频文件信息*/
    public static ArrayList<Object> getVideoInfo(MultipartFile file) {
        ArrayList<Object> list = new ArrayList<>();
        File pictureFile = null;
        Picture picture = new Picture();
        String fileName = file.getOriginalFilename().replaceAll(" |\"|\'", "_");
        picture.setTitle(fileName);
        try {
            // 用uuid作为文件名，防止生成的临时文件重复
            pictureFile = File.createTempFile(String.valueOf(UUID.randomUUID()), fileName);
            file.transferTo(pictureFile);
            picture.setSize(pictureFile.length());
            getVideoInfo(pictureFile, picture);
            list.add(picture);
            list.add(pictureFile.getAbsoluteFile());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //程序结束时，删除临时文件  上传结束后再进行删除
            // deleteFile(pictureFile);
        }
        return list;
    }

    public static void getVideoInfo(File pictureFile, Picture picture) throws EncoderException, IOException {

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

    }

    public static Date getCreateTime(File file) throws IOException {
        if (file == null) {
            return null;
        }
        //ffmepg工具地址
        Resource resource = new ClassPathResource("ffmpeg.exe");
        String ffmpegPath = resource.getFile().getPath();
        // String ffmpegPath = "D:\\MyMind\\note\\note_admin\\src\\main\\resources\\ffmpeg.exe";

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
                System.out.println(line);
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

    private static void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private static String getLastTime(long duration) {
        int hour = (int) duration / (60 * 60 * 1000);//小时
        int minute = (int) (duration % (60 * 60 * 1000)) / 60000;//分钟
        int second = (int) ((duration % (60 * 60 * 1000)) % 60000) / 1000;//秒
        String lastTime = (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
        return lastTime;
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

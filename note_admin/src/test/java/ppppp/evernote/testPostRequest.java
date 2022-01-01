package ppppp.evernote;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import ppppp.evernote.entity.Face;
import ppppp.evernote.service.FaceService;
import ppppp.evernote.util.RestTemplateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pppppp
 * @date 2021/11/8 20:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class testPostRequest {
    @Test
    public void T_() {
        String s = sendPostRequest("http://localhost:8080/admin/noteBook/noteBooksTree");
        System.out.println(s);
    }

    public static String sendPostRequest(String url) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity requestEntity = new HttpEntity<>(headers);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }

    @Test
    public void T_kk() {
        RestTemplate restTemplate = new RestTemplate();
        //1. 简单Get请求
        String rootUrl = "http://localhost:8080/admin/note/updateNoteCountWrapper";
        int noteBookId = 3;
        int count = -1;
        String result = restTemplate.getForObject(rootUrl + "?noteBookId=" + noteBookId + "&count=" + count, String.class);
        System.out.println("简单Get请求:" + result);
    }

    @Autowired
    FaceService faceService;
    @Test
    public void T_getFace() {
        // RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = RestTemplateUtil.getInstance();
        //1. 简单Get请求
        String rootUrl = "http://47.101.137.245:5000";
        String imageUrl = "http://47.101.137.245/img/2004-11-27/763X-fyinvwu3508871.jpg";


        // String imageUrl = "http://47.101.137.245/img/l.jpg";
        System.out.println("发送请求");
        HashMap<String, Object> res = restTemplate.getForObject(rootUrl + "?imageUrl=" + imageUrl, HashMap.class);
        int faceNum = (int) res.get("faceNum");
        /*判断是否检测到了人脸*/
        if (faceNum == 0) {
            System.out.println("未检测到人脸");
            return;
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
        System.out.println(faces);
    }


    private void getByReg(String reg, ArrayList<String> arr, String content) {
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(content);
        while (m.find()) {
            arr.add(content.substring(m.start(), m.end()));
        }
    }
}

package ppppp.evernote;

import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author pppppp
 * @date 2021/11/8 20:05
 */
public class testPostRequest {
    @Test
    public void T_(){
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
    public void T_kk(){
        RestTemplate restTemplate = new RestTemplate();
        //1. 简单Get请求
        String rootUrl = "http://localhost:8080/admin/note/updateNoteCountWrapper";
        int noteBookId = 3;
        int count = -1;
        String result = restTemplate.getForObject(rootUrl + "?noteBookId="+noteBookId+"&count="+count, String.class);
        System.out.println("简单Get请求:" + result);
    }
}

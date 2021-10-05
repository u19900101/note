package ppppp.evernote.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author pppppp
 * @date 2021/10/5 22:45
 */
@RestController
@RequestMapping("/es")
public class NoteSearch {

    @PostMapping("/search")
    private String search(@RequestBody String data) throws Exception {
        System.out.println(data);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(data, headers);
        String res = restTemplate.postForObject("http://192.168.56.10:9200/canal_note/_search", request ,String.class);
        System.out.println(res);
        return res;
    }
}

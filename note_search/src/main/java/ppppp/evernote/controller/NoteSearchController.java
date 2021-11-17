package ppppp.evernote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ppppp.evernote.entity.Search;
import ppppp.evernote.service.SearchService;
import ppppp.evernote.util.ResultUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author pppppp
 * @date 2021/10/5 22:45
 */
@RestController
@RequestMapping("/es")
public class NoteSearchController {
    @Autowired
    SearchService searchService;

    @PostMapping("/search")
    private String search(@RequestBody String data) throws Exception {
        System.out.println(data);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(data, headers);
        String res = restTemplate.postForObject("http://47.101.137.245:9200/canal_note/_search", request, String.class);
        // String res = restTemplate.postForObject("http://192.168.56.10:9200/canal_note/_search", request, String.class);
        System.out.println(res);
        return res;
    }


    @PostMapping("/insertSearchWords")
    private String insertSearchWords(@RequestBody Search search){
        search.setCreateTime(new Date());
        boolean save = searchService.save(search);
        if (save) return ResultUtil.successWithData(save);

        return ResultUtil.errorWithMessage("error");
    }

    @RequestMapping("/getSearchHistroy")
    private String getSearchHistroy(){
        /*最近的十条*/
        List<Search> searchList = searchService.lambdaQuery().orderByDesc(Search::getCreateTime).last("limit 10").list();
        return ResultUtil.successWithData(searchList);
    }
}

package ppppp.evernote.util;

import org.springframework.web.client.RestTemplate;

/**
 * @author pppppp
 * @date 2022/1/1 16:24
 */
public class RestTemplateUtil {
    public static RestTemplate getInstance() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
        return restTemplate;
    }
}


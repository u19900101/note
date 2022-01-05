package ppppp.evernote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pppppp
 * @date 2021/12/28 8:59
 */
@Data
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpConfig {
    private String host;
    private Integer port;
    public String username;
    private String password;
    private String basepath;
    private String dns;
}


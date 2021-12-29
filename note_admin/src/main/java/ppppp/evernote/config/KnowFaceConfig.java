package ppppp.evernote.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pppppp
 * @date 2021/12/29 20:04
 */
@Data
@Component
@ConfigurationProperties(prefix = "face")
public class KnowFaceConfig {
    private String knownFaceEncodingsPath;
    private String knownFaceIdsPath;
}
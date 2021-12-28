package ppppp.evernote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.config.FtpConfig;

/**
 * @author pppppp
 * @date 2021/12/28 8:45
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FtpDemo {
    @Autowired
    FtpConfig ftpConfig;

    @Test
    public void T_kk(){
        System.out.println("kkkkk");
        System.out.println(ftpConfig.toString());
    }
}

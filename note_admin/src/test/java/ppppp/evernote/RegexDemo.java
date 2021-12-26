package ppppp.evernote;

import org.junit.Test;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pppppp
 * @date 2021/12/7 23:18
 */
public class RegexDemo {

    @Test
    public void T_(){
        String fileName = "vip-jingjing-3.18 (14).jpg";
        String type = fileName.substring(fileName.lastIndexOf("."));
        String name = fileName.substring(0,fileName.lastIndexOf("."));
        fileName = name.replaceAll("\\.| ","_") + "_"+ UUID.randomUUID().toString().substring(0,2) + type;
        System.out.println(fileName);
    }

    @Test
    public void T_特殊字符(){
        String re1 = "abc";
        System.out.println("abc".matches(re1));
        System.out.println("Abc".matches(re1));
        System.out.println("abcd".matches(re1));

        String re2 = "a\\&c"; // 对应的正则是a\&c
        System.out.println("a&c".matches(re2));
        System.out.println("a-c".matches(re2));
        System.out.println("a&&c".matches(re2));
    }

    @Test
    public void T_匹配开头和结尾(){
      //^A\d{3}$，可以匹配"A001"、"A380"
        String reg = "^\\<img.*\\>$";
        System.out.println("<img ddd />".matches(reg));
        System.out.println("<A001>".matches(reg));
        System.out.println("<A001>".matches(reg));
        System.out.println("A380".matches(reg));
    }

    @Test
    public void T_分组匹配(){
        Pattern p = Pattern.compile("(\\d{3,4})\\-(\\d{7,8})");
        Matcher m = p.matcher("010-12345678");
        if (m.matches()) {

            System.out.println(m.group(0));
            System.out.println(m.group(1));
            System.out.println(m.group(2));
        } else {
            System.out.println("匹配失败!");
        }
    }

    @Test
    public void T_replaeAll(){
        String content = "ddddddddddddddddddddd<img 第一个/> sssssssssssss<img xxxxx/>ssssssss<video controls dddddffff></video> gggggggg<video controls dddddffff>视频</video>gggggggg";
        // String replaceAll = content.replaceAll("^\\<img.*?\\>$","替换");
        String replaceAll = content.replaceAll("\\<img.*?\\>|\\<video.*?video\\>","替换");
        System.out.println(replaceAll);
    }

    @Test
    public void T_replaeAll2(){
        String content = "1,2,3,";
        // String replaceAll = content.replaceAll("^\\<img.*?\\>$","替换");
        String replaceAll = content.replaceAll("\\[|\\]","替换");
        System.out.println(replaceAll);
    }

    @Test
    public void T_搜索(){
        String content = "dddddddddd<img src=\"http://lpgogo.xxxxxreensdddd9.png\" style=\"zoom:30%;\"/>\nggggg";
        Pattern p = Pattern.compile("\\<img.*?\\>");
        Matcher m = p.matcher(content);
        while (m.find()) {
            String sub = content.substring(m.start(), m.end());
            System.out.println(sub);
            String s = sub.substring(sub.indexOf("\"") + 1);
            String s1 = s.substring(0, s.indexOf("\""));
            System.out.println(s1);
            break;
        }
    }
    @Test
    public void T_非贪婪匹配(){
        Pattern pattern = Pattern.compile("(\\d+?)(0*)");
        Matcher matcher = pattern.matcher("1230000");
        if (matcher.matches()) {
            System.out.println("group1=" + matcher.group(1)); // "123"
            System.out.println("group2=" + matcher.group(2)); // "0000"
        }
    }

    @Test
    public void T_找出所有数字并分组(){
        Pattern p = Pattern.compile("\\d+");
        String content = "sss1230,1000,256,";
        Matcher m = p.matcher(content);
        while (m.find()) {
            String sub = content.substring(m.start(), m.end());
            System.out.println(sub);
        }
    }
    @Test
    public void T_kk(){
        String content = "[[1,2,4],[1,2,3,4],[1,2,3,4]]";
        Pattern p = Pattern.compile("\\[\\d+.*?\\]");
        Matcher m = p.matcher(content);
        while (m.find()) {
            String sub = content.substring(m.start(), m.end());
            System.out.println(sub);
        }
    }

    @Test
    public void T_kk2(){
        String content = "[[-1240, 1122], [1253, 1212], [-1271, 1301], [1271, 1301]]";
        Pattern p = Pattern.compile("\\[\\-?\\d+.*?\\]");
        Matcher m = p.matcher(content);
        while (m.find()) {
            String sub = content.substring(m.start(), m.end());
            System.out.println(sub);
        }
    }


}

package ppppp.evernote;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pppppp
 * @date 2021/12/25 9:25
 */
public class ArrayListDemo {
    @Test
    public void T_集合去重(){
        //无重并集
        String toPersonPictureUid = "1,2,3,4,5,3,4,";
        List<String> personUids =  new ArrayList<>(Arrays.asList(toPersonPictureUid.split(",")));
        List uniqueList = personUids.stream().distinct().collect(Collectors.toList());
        System.out.println(uniqueList.toString().substring(1,uniqueList.toString().length()-1) + ",");
    }
}

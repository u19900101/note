package ppppp.evernote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ppppp.evernote.entity.Face;
import ppppp.evernote.entity.Picture;
import ppppp.evernote.service.FaceService;
import ppppp.evernote.service.PictureService;

import java.io.*;
import java.util.List;

/**
 * @author pppppp
 * @date 2021/12/26 10:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FaceDataDemo {
    final String knownFaceEncodingsPath = "D:\\MyMind\\note\\data\\pythonModule\\python\\known_face_encodings.txt";
    final String knownFaceIdsPath = "D:\\MyMind\\note\\data\\pythonModule\\python\\known_face_ids.txt";

    @Autowired
    FaceService faceService;

    @Test
    public void T_() throws IOException {
        checkKnowFaceData();
    }
    public void checkKnowFaceData() throws IOException {
        int count = faceService.count();
        BufferedReader reader = new BufferedReader(new FileReader(knownFaceIdsPath));
        String[] line = reader.readLine().replaceAll(" +"," ").split(" ");
        /*若两者不相等 重写文件*/
        if(count != line.length){
            reWriteKnownFace();
        }
    }

    public void reWriteKnownFace() {
        List<Face> knownFaces = faceService.lambdaQuery().select(Face::getPersonId, Face::getFaceEncoding).list();
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(knownFaceEncodingsPath));
            BufferedWriter out2 = new BufferedWriter(new FileWriter(knownFaceIdsPath));
            for (Face face : knownFaces) {
                out.write(face.getFaceEncoding().replaceAll(" |\\[|\\]", "").replace(",", " ") + "\n");
                out2.write(face.getPersonId() + " ");
            }
            out.close();
            out2.close();
            System.out.println("success reWriteKnownFace！");
        } catch (IOException e) {
            System.out.println("failed reWriteKnownFace");
        }
    }

    @Autowired
    PictureService pictureService;
    @Test
    public void T_getPicture(){
        Picture picture = pictureService.getById(188);
        System.out.println(picture);
    }
}

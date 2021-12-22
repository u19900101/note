package ppppp.evernote.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
/*人脸库*/
@Data
public class Face {
    @TableField()
    private Integer id;
    private Integer faceNameId;
    /*所对应的图片*/
    private Integer pictureId;
    /*faceId 编码 128*1 的浮点数组*/
    private String faceEncoding;
    /*在图片中的位置 4*1坐标*/
    private String faceLocations;
    /*72*2特征点*/
    private String faceLandmarks;
}
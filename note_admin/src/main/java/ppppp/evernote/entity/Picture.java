package ppppp.evernote.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Picture {
    private String path;

    private String pid;

    private String pname;

    private Date pcreatime;

    private String gpsLongitude;

    private String gpsLatitude;

    private Integer pwidth;

    private Integer pheight;

    private Long psize;

    private String plabel;

    private String pdesc;

}
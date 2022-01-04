# 获取照片的基本信息
# title location  widthH lnglat createTime updateTime tagUid faceUid url size lastTime
import ntpath

import exifread
import os
import time
from PIL import Image
from HtmlToMd.faceReg import getFaceInfo
from HtmlToMd.func import get_locationName, insertFace, insertPicture, insertFaceIntoPicture, update_insert_person
from HtmlToMd.ssh import uploadFile
def getLatOrLng(refKey, tudeKey,tags):
    """
    获取经度或纬度
    """
    if refKey not in tags:
        return None
    ref = tags[refKey].printable
    LatOrLng = tags[tudeKey].printable[1:-1].replace(" ", "").replace("/", ",").split(",")
    # print(LatOrLng)
    LatOrLng = float(LatOrLng[0]) + float(LatOrLng[1]) / 60 + float(LatOrLng[2])/float(LatOrLng[3]) / 3600
    if refKey == 'GPS GPSLatitudeRef' and tags[refKey].printable != "N":
        LatOrLng = LatOrLng * (-1)
    if refKey == 'GPS GPSLongitudeRef' and tags[refKey].printable != "E":
        LatOrLng = LatOrLng * (-1)
    return LatOrLng
def getPictureInfo(path):
    f = open(path, 'rb')
    tags = exifread.process_file(f)
    # # 打印所有照片信息，会以键值对的方法保存
    size = os.path.getsize(path)
    updateTime = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    # 对无法识别的图片进行另一种方式的信息获取
    if(len(tags) == 0):
        path = "1.jpg"
        img = Image.open(path)
        widthH = str(img.size[0]) +"x"+ str(img.size[1])
        return size,widthH,updateTime,updateTime,'',''
    for tag in tags.keys():
        print("Key: {0}, value {1}".format(tag, tags[tag]))
    # 打印照片其中一些信息
    lat = getLatOrLng('GPS GPSLatitudeRef', 'GPS GPSLatitude',tags)  # 纬度
    lng = getLatOrLng('GPS GPSLongitudeRef', 'GPS GPSLongitude',tags)  # 经度
    lnglat = str(lng) + "," + str(lat)
    widthH = str(tags['EXIF ExifImageWidth']) + "x"+  str(tags['EXIF ExifImageLength'])
    location =  get_locationName( str(lng),str(lat))
    createTime = str(tags['EXIF DateTimeOriginal'])
    createTime = createTime[:4] + "-" + createTime[5:7] + "-" + createTime[8:]
    return size,widthH,createTime,updateTime,lnglat,location
remote_face_dir = "/mydata/nginx/html/face"
remote_img_dir = "/mydata/nginx/html/img"
yu_url = "http://lpgogo.top"

local_picture_path = "1.jpg"
# 获取照片的基本信息
size,widthH,createTime,updateTime,lnglat,location = getPictureInfo(local_picture_path)

# 上传图片到服务器 按照月进行分类
remote_file_path = uploadFile(local_picture_path, remote_img_dir+ '/' + createTime[:7])
if remote_file_path is None:
    # return "error"
    print("失败  图片上传 ...",remote_file_path)
# 将图片信息写进数据库
picture_id = insertPicture(ntpath.basename(remote_file_path),location,widthH,lnglat,createTime,updateTime,yu_url + remote_file_path.replace(remote_img_dir,'/img'),size)

# 进行人脸识别 并上传人脸到服务器
res = getFaceInfo(local_picture_path)
if res:
    face_uids = []
    for i in range(res['faceNum']) :
        remote_file_path = uploadFile(res["face_urls"][i], remote_face_dir + '/' + createTime[:7])
        if remote_file_path is None:
            # return "error"
            print('失败  人脸上传')
            break
        # personId pictureId faceEncoding faceLocations faceLandmarks url
        url = yu_url + remote_file_path.replace(remote_face_dir,'/face')
        face_id = insertFace(res['face_name_ids'][i],picture_id,res['face_encodings'][i],res['face_locations'][i], res['face_landmarks'][i],url)
        # 更新或新建person
        update_insert_person(res['face_name_ids'][i],picture_id)
        face_uids.append(face_id)
    # 将人脸信息写进picture中
    insertFaceIntoPicture(picture_id,",".join('%s' %a for a in face_uids)  + ",")
    print('成功  人脸上传',ntpath.basename(remote_file_path))



# 修改笔记中的图片路径为服务器路径


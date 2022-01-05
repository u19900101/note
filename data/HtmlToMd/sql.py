import face_recognition
import numpy as  np
import cv2
import dlib
import os.path
import uuid
import pymysql
from markdownify import markdownify as md
import re
import requests
import os
import time
from HtmlToMd.ssh import get_conn
import ntpath
import exifread
import os
import time
from PIL import Image
from HtmlToMd.ssh import uploadFile, uploadFileCover
local_pydata_path = './'
# face保存的位置
local_face_path = './face/'
local_known_face_encodings = 'known_face_encodings.txt'
local_known_face_ids = 'known_face_ids.txt'
remote_known_face_encodings = "/mydata/python/known_face_encodings.txt"
remote_known_face_ids = "/mydata/python/known_face_ids.txt"
remote_face_dir = "/mydata/nginx/html/face"
remote_img_dir = "/mydata/nginx/html/img"
romote_py = "/mydata/python"
yu_url = "http://lpgogo.top"
# 人脸对齐
def save_face(local_face_path, cv_bgr_image):
    # 生成唯一name，判断是否重名
    uid = str(uuid.uuid1()) + ".jpg"
    facepath = local_face_path + uid
    while(os.path.isfile(facepath)):
        uid = str(uuid.uuid1()) + ".jpg"
        facepath = local_face_path + uid
    if not os.path.exists(local_face_path):
        os.makedirs(local_face_path)
    cv2.imwrite(facepath,cv_bgr_image)
    return facepath
def face_alignment(rgb_img):
    # opencv的颜色空间是BGR，需要转为RGB才能用在dlib中
    predicter_path =local_pydata_path + 'shape_predictor_68_face_landmarks.dat'
    detector = dlib.get_frontal_face_detector()
    # 导入检测人脸特征点的模型
    sp = dlib.shape_predictor(predicter_path)
    # 检测图片中的人脸
    dets = detector(rgb_img, 1)# (top, right, bottom, left)  803  982  892  892 # (left,top, right, bottom) 892  803  982  892

    # 人脸对齐
    # 识别人脸特征点，并保存下来
    faces = dlib.full_object_detections()
    for det in dets:
        faces.append(sp(rgb_img, det))
    images = dlib.get_face_chips(rgb_img, faces, size=320)

    # 显示对齐结果
    face_paths = []
    for image in images:
        cv_rgb_image = np.array(image).astype(np.uint8)# 先转换为numpy数组
        cv_bgr_image = cv2.cvtColor(cv_rgb_image, cv2.COLOR_RGB2BGR)# opencv下颜色空间为bgr，所以从rgb转换为bgr
        # 直接保存人脸到硬盘上
        facepath = save_face(local_face_path,cv_bgr_image)
        face_paths.append(facepath)
    return face_paths
def cv_imread(file_path):
    cv_img = cv2.imdecode(np.fromfile(file_path, dtype=np.uint8), -1)
    return cv_img
def cv_imwrite(img, path):
    suffix = os.path.splitext(path)[-1]
    cv2.imencode(suffix, img)[1].tofile(path)
def getFaceInfo(imgpath):
    rectangle = []
    keypoint = []
    scale = 1
    img_rgb = face_recognition.load_image_file(imgpath)

    face_locations = face_recognition.face_locations(img_rgb,1)
    face_landmarks = face_recognition.face_landmarks(img_rgb,face_locations) #72个点
    face_encodings = face_recognition.face_encodings(img_rgb,face_locations)

    # 将结果整理封装
    for (top, right, bottom, left),face_landmark  in zip(face_locations,face_landmarks):
        rectangle.append([left,top,right-left,bottom-top])
        pointTemp = []
        for value in face_landmark.values():
            for point in value:
                pointTemp.append([point[0], point[1]])
        keypoint.append(pointTemp)

    faceNum = len(face_locations)
    faceDic = {}
    faceDic['faceNum'] = faceNum
    print(faceDic['faceNum'])
    if(faceNum == 0):
        print("未检测到人脸")
        return
    # 4*1
    faceDic['face_locations'] = (np.array(rectangle)/scale).astype(int).tolist()
    # 72*2
    faceDic['face_landmarks'] = (np.array(keypoint)/scale).astype(int).tolist()
    # 128*1
    faceDic['face_encodings'] = np.asarray(face_encodings).tolist()

    # 与数据库比对  得到人脸 id  新人脸就在原有人脸最大id上+1，得到新id
    faceDic['face_name_ids'] = getFaceIndex(face_encodings)
    # 人脸对齐
    faceDic['face_urls'] = face_alignment(img_rgb)
    return faceDic
def reWrite_known_data(face_id, face_encoding):
    with open(local_pydata_path + 'known_face_ids.txt', 'a+') as f:
        f.write(str(face_id)+' ')
    with open(local_pydata_path + 'known_face_encodings.txt', 'a+') as f:
        f.write(str(face_encoding.tolist()).replace("[","").replace("]","").replace(","," ") + '\n')
    print("写入known_data.txt")
def getFaceIndex(face_encodings):
    # 从本地读取人脸文件
    known_face_encodings = np.loadtxt(local_known_face_encodings).tolist()
    known_face_ids = np.loadtxt(local_known_face_ids).tolist()
    # 解决只有一个id时的bug
    if isinstance(known_face_ids,float):
        known_face_ids = [known_face_ids]
        known_face_encodings = [known_face_encodings]
    # 计算 面孔编码两两之间的距离 来判断是否为同一人
    faceId = []
    # 当文件为空时 直接写入
    if(len(known_face_encodings) == 0):
        face_id = 0
        for face_encoding in  face_encodings:
            if len(known_face_encodings) > 0:
                #  找到差距小于 0.2 的所有位置 true false
                compare_faces = face_recognition.compare_faces(known_face_encodings,face_encoding,0.5)
                #  -1 表示为 在已有 中找不到 误差小于 0.2 的人脸
                face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
                # 取出这个最近人脸的评分
                best_match_index = np.argmin(face_distances)
                if compare_faces[best_match_index]:
                    face_id = known_face_ids[best_match_index]
                else:
                    # 出现新面孔
                    face_id += 1
            reWrite_known_data(face_id,face_encoding)
            known_face_encodings.append(face_encoding)
            known_face_ids.append(face_id)
        return known_face_ids


    known_face_ids = [int(x) for x in known_face_ids]
    # known_face_ids = list(map(int, (re.compile(r'\d+')).findall(known_face_ids)))# 查找数字
    max_faceId = np.asarray(known_face_ids).max()
    for face_encoding in  face_encodings:
        #  找到差距小于 0.2 的所有位置 true false
        compare_faces = face_recognition.compare_faces(known_face_encodings,face_encoding,0.5)
        #  -1 表示为 在已有 中找不到 误差小于 0.2 的人脸
        face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
        # 取出这个最近人脸的评分
        best_match_index = np.argmin(face_distances)
        if compare_faces[best_match_index]:
            face_id = known_face_ids[best_match_index]
        else:
            # 出现新面孔
            max_faceId +=1
            face_id = max_faceId
        #  更新两个txt文件
        reWrite_known_data(face_id,face_encoding)
        faceId.append(face_id)
    return faceId
# 同步人脸数据取文件
def syncData():
    ssh_client = get_conn()
    ftp_client_down_re = ssh_client.open_sftp()
    ftp_client_down_re.get(remote_known_face_encodings,local_known_face_encodings)
    ftp_client_down_re.get(remote_known_face_ids,local_known_face_ids)
    ftp_client_down_re.close()
    # 判断数据是个数是否与数据库中有变动
    with open(local_known_face_ids, 'r') as f:
        count = f.read().strip().split(' ')
        # 不一致(存在在此期间可能对人脸进行过删除操作)就重写
        if get_face_count() != len(count):
            print('同步人脸数据')
            rewrite_known_data_from_db()
            # 覆盖本地的know_data.txt到服务器
            uploadFileCover(local_known_face_encodings, romote_py)
            uploadFileCover(local_known_face_ids, romote_py)
# ******************** 获取图片的基本信息******************** #
def getLatOrLng(refKey, tudeKey,tags):
    """
    获取经度或纬度
    """
    if refKey not in tags:
        return -1
    ref = tags[refKey].printable
    LatOrLng = tags[tudeKey].printable[1:-1].replace(" ", "").replace("/", ",").split(",")
    # print(LatOrLng)
    latlng = float(LatOrLng[0]) + float(LatOrLng[1]) / 60
    if float(LatOrLng[3]) > 0:
        latlng +=  float(LatOrLng[2])/float(LatOrLng[3]) / 3600
    if refKey == 'GPS GPSLatitudeRef' and tags[refKey].printable != "N":
        latlng = latlng * (-1)
    if refKey == 'GPS GPSLongitudeRef' and tags[refKey].printable != "E":
        latlng = latlng * (-1)
    return latlng
def getPictureInfo(path):
    f = open(path, 'rb')
    tags = exifread.process_file(f)
    # # 打印所有照片信息，会以键值对的方法保存
    size = os.path.getsize(path)
    updateTime = time.strftime("%Y/%m/%d %H:%M", time.localtime())
    updateTime = d8_to_utc(updateTime)
    # 对无法识别的图片进行另一种方式的信息获取
    if 'EXIF ExifImageWidth' not in tags and 'Image ImageWidth' not in tags:
        img = Image.open(path)
        widthH = str(img.size[0]) +"x"+ str(img.size[1])
        return size,widthH,updateTime,updateTime,'',''
    lat = getLatOrLng('GPS GPSLatitudeRef', 'GPS GPSLatitude',tags)  # 纬度
    lng = getLatOrLng('GPS GPSLongitudeRef', 'GPS GPSLongitude',tags)  # 经度
    if lat > 0 and lng > 0:
        lnglat = str(lng) + "," + str(lat)
        location =  get_locationName(str(lng),str(lat))
    else:
        lnglat = ''
        location = ''
    if 'EXIF ExifImageWidth' in tags:
        widthH = str(tags['EXIF ExifImageWidth']) + "x"+  str(tags['EXIF ExifImageLength'])
    else:
        widthH = str(tags['Image ImageWidth']) + "x"+  str(tags['Image ImageLength'])
    if 'EXIF DateTimeOriginal' not in tags and 'Image DateTime' not in tags:
        createTime = updateTime
    else:
        if'EXIF DateTimeOriginal' in tags:
            createTime = str(tags['EXIF DateTimeOriginal'])
        else:
            createTime = str(tags['Image DateTime'])
    createTime = createTime[:4] + "/" + createTime[5:7] + "/" + createTime[8:-3]
    createTime = d8_to_utc(createTime)
    return size,widthH,createTime,updateTime,lnglat,location


def get_local_picture_info_upload_insert(local_picture_path):
    # 获取照片的基本信息
    size,widthH,createTime,updateTime,lnglat,location = getPictureInfo(local_picture_path)

    # 上传图片到服务器 按照月进行分类
    remote_file_path = uploadFile(local_picture_path, remote_img_dir+ '/' + createTime[:7])
    if remote_file_path is None:
        # return "error"
        print("失败  图片上传 ...",remote_file_path)
    else:
        # 上传缩略图
        im=Image.open(local_picture_path)
        im.thumbnail((400,400))
        file_name = ntpath.basename(remote_file_path)
        if len(re.findall(r'.jpg|.png|.jpeg',file_name)) == 0:
            file_name += '.jpg'
        temp_path = './temp/'+ file_name.split('.')[0] + '_thumbnails.' + file_name.split('.')[-1]
        im.save(temp_path)
        uploadFile(temp_path, remote_img_dir+ '/' + createTime[:7])
        os.remove(temp_path)
    # 将图片信息写进数据库
    img_url = yu_url + remote_file_path.replace(remote_img_dir,'/img')
    picture_id = insertPicture(ntpath.basename(remote_file_path),location,widthH,lnglat,createTime,updateTime,img_url,size)

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
            # 上传图片成功 删除本地人脸
            else:
                os.remove(res["face_urls"][i])

            # personId pictureId faceEncoding faceLocations faceLandmarks url
            url = yu_url + remote_file_path.replace(remote_face_dir,'/face')
            face_id = insertFace(res['face_name_ids'][i],picture_id,res['face_encodings'][i],res['face_locations'][i], res['face_landmarks'][i],url)
            # 更新或新建person
            update_insert_person(res['face_name_ids'][i],picture_id)
            face_uids.append(face_id)
        # 将人脸信息写进picture中
        insertFaceIntoPicture(picture_id,",".join('%s' %a for a in face_uids)  + ",")
        print('成功  人脸上传',ntpath.basename(remote_file_path))
    # 覆盖本地的know_data.txt到服务器
    uploadFileCover(local_known_face_encodings, romote_py)
    uploadFileCover(local_known_face_ids, romote_py)
    return img_url
# ******************** #数据库相关操作******************** #
WEBSITE = "http://lpgogo.top/img/"
db = pymysql.connect(host='192.168.56.10', user='root', password='root', database='canal_test')
cursor = db.cursor()
# 去掉文件和文件夹名称中的特殊字符
def renameFiles(path):
    fileList=os.listdir(path)
    for i in fileList:
        if os.path.isdir(path + "\\"+i):
            renameFiles(path + "\\"+i)
        # 中英文逗号
        if re.findall(r'[\s\[\],，。]', i):
            os.rename(path + "\\"+i,path + "\\"+re.sub(r'_+', '_', re.sub(r'[\s\[\],，。]', '_', i)))
            print("重命名文件" + i)
def d8_to_utc(d8_time):
    d8_time = time.strptime(d8_time, "%Y/%m/%d %H:%M")
    #3.将时间数组转换为时间戳
    d8_time = time.mktime(d8_time)
    #4.将时间戳转换为东八区的时间戳
    utc_time =  d8_time - 8*60*60
    #5.将时间戳进行格式化即可
    utc_time = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(utc_time))
    return utc_time
def gen_video_tag(s):
    return re.sub('(?P<value>\[.*?mp4\))', video_match, s)
def video_match(matched):
    value = matched.group('value')
    value = re.sub('(?P<value>\[.*\])', "\n", value)
    if value.endswith(".mp4)"):
        matchObj = re.search( r'\((.*)\).*', value)
        # 加上网址前缀
        if matchObj.group(1):
            filename =  re.sub(r'_+', '_', re.sub(r'[\s\[\],，。]', '_', matchObj.group(1)))
            value = "\n\n<video controls preload=\"auto\" src=\"" + WEBSITE + filename + "\"></video>\n\n"
    return value
# 将html转化为md
def htmlToMd(dir,htmlPath):
    with open(dir +"/"+ htmlPath, 'r', encoding='UTF-8') as f:
        htmlpage = f.read()
        # 处理html格式文件中的内容
        text = md(htmlpage)
        # 非贪婪匹配去点 body, td ...}
        text = re.sub(r'body[\s\S]*?}', '', text)
        # 1.去除多余的换行
        text = re.sub(r'(\n)+', "\n", re.sub(r' +\n', '\n', text))


        text = text.replace("\xa0", " ")
        text = text.split("\n")
        result = text[6:]
        # # 4.去掉第一行和第二行
        result.insert(0,text[2])
    return result
def get_locationName(lng, lat):
    with open('gps_key.txt', 'r') as f:
        key = f.read()
        r = requests.get(url='https://restapi.amap.com/v3/geocode/regeo',
                         params={'location':  str(lng)+','+ str(lat), 'key': key})
        result = r.json()
        if result['status'] == '1':
            return result['regeocode']['formatted_address']
        return ''
def getFiled(targetArr):
    pattern = re.compile(r'\*\d+.*\*')  # 查找数字
    count = 1  #记录查找到的行数，用于输出content的位置
    createTime,updateTime,location,lng_lat = ["","","",""]
    tagList = []
    for i in targetArr:
        if i.startswith("| **"):
            count += 1
        if i.startswith("| **创建时间"):
            createTime = pattern.findall(i)[0].replace("*", "").strip()
            createTime = d8_to_utc(createTime)
        elif i.startswith("| **更新时间"):
            updateTime = pattern.findall(i)[0].replace("*", "").strip()
            updateTime = d8_to_utc(updateTime)
        elif i.startswith("| **位置"):
            locationLink = re.compile(r'http.*\)').findall(i)[0].replace(")", "").strip()
            lng_lat = re.compile(r'q=.*').findall(locationLink)[0].replace("q=", "").split(",")
            location = get_locationName(eval(lng_lat[1]), eval(lng_lat[0])).strip()
            lng_lat = lng_lat[1] + ',' + lng_lat[0]
        elif i.startswith("| **标签"):
            tagList = [x.replace("*", "").strip() for x in re.compile(r'\*.*?\*').findall(i.strip())[2:]]


    return createTime,updateTime, location, lng_lat, tagList, count
def md_sql(fileArr,dir):
    # 周总结
    # | **创建时间：** | *2015/9/1 19:03* |
    # | **更新时间：** | *2018/9/28 19:40* |
    # | **位置：** | [*30°35'12 N  114°14'6 E*](http://maps.google.com/maps?z=6&q=30.586600,114.235000) |
    # | **标签：** | *周总结* |
    # 1.钓鱼，钓到了大鲶鱼，下池塘捞线捞漂，
    title = fileArr[0].strip()
    # 在第 1-4行进行查找
    createTime,updateTime, location, lng_lat, tagList, count = getFiled(fileArr[1:6])
    content = addHttp(WEBSITE,''.join(fileArr[count:]),dir)
    # 5.二次转化视频标签
    content = gen_video_tag(content)
    content = content[:len(content)-2]  # 去掉末尾的特殊字符 �

    # 给写进数据库的内容加上标题
    content = "# " + title + "\n\n" + content
    return title, createTime, updateTime, location, lng_lat, tagList, content
def html_sql(htmlPath):
    htmlToMd(htmlPath)
    return md_sql(htmlPath.replace(".html", ".md"))
def getTag_uid(tagList):
    tag_uid = ''
    if len(tagList) > 0:
        for tagName in tagList[0].split(","):
            resId = isTagExist(tagName.strip())
            if resId == -1:
                resId = insertTag(tagName.strip())
            tag_uid += str(resId) + ','
    return tag_uid
# 查找所有 ![](NAME)
# 缩放图片  将 ![](NAME)   转化为 \n<img src="http://lpgogo.top/a4.jpg" alt = "FILENAME.TYPE" style="zoom:30%;">\
def addHttp(httpname,content,dir):
    for i in re.compile(r'!\[\]\(.*?\)').findall(content):
        if 'en_todo' in i:
            content = content.replace(i,'')
            continue
        # 提取 (NAME) 中的 NAME
        matchObj = re.search( r'\((.*)\).*', i)
        # 加上网址前缀
        if matchObj.group(1):
            filename  = re.sub(r'_+', '_', re.sub(r'[\s\[\],，。]', '_', matchObj.group(1))).replace('_"点击下载"','')
            # 进行图片上传到服务器 并进行人脸识别
            img_url = get_local_picture_info_upload_insert(dir + '/' +filename)
            content = content.replace(i,'\n\n<img src="' + img_url + '" alt = "' +filename.replace('http://lpgogo.top/img/','') + '" style="zoom:30%;"/>\n\n')
    return content
def insertTag(tagName):
    timestr = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    sql = "INSERT INTO tag(title, sort,create_time,update_time) VALUES ('%s', '%s', '%s', '%s')" % \
          (tagName,getMaxSort() + 1,timestr,timestr)
    try:
        # 执行sql语句
        cursor.execute(sql)
        # 最新插入行的主键id
        tag_id = db.insert_id()
        # 执行sql语句
        db.commit()
        return tag_id
    except:
        print('error...')
        db.rollback()
def getMaxSort():
    sql = "SELECT MAX(sort) FROM tag"
    try:
        # 执行SQL语句
        cursor.execute(sql)
        res = cursor.fetchall()
        if(res[0][0] is None):
            return 0
        else:
            return res[0][0]
    except:
        print("Error: unable to fetch data")
def isTagExist(tagName):
    sql = "SELECT id,title FROM tag "
    try:
        # 执行SQL语句
        cursor.execute(sql)
        # 获取所有记录列表
        results = cursor.fetchall()
        for row in results:
            # 忽略大小写 比较tag名称
            if tagName.lower() == row[1].lower():
                return row[0]
        return -1
    except:
        print("Error: unable to fetch data")
def insertNote(title, tag_uid, createTime, updateTime, location, lnglat, content):
    # pymysql.escape_string 解决 ' 保存的问题
    if len(updateTime) > 0:
        sql = "INSERT INTO note(title, tag_uid, create_time, update_time, location, lnglat, content)  VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')" % \
          (pymysql.escape_string(title), tag_uid, createTime, updateTime, location, lnglat, pymysql.escape_string(content))
    else:
        sql = "INSERT INTO note(title, tag_uid, create_time,  location, lnglat, content)  VALUES ('%s', '%s', '%s', '%s', '%s', '%s')" % \
              (pymysql.escape_string(title), tag_uid, createTime,  location, lnglat, pymysql.escape_string(content))
    try:
        cursor.execute(sql)
        db.commit()
        print('保存成功',title)
    except Exception as e:
        print(e,title)
        db.rollback()
def closeConn():
    db.close()

# ******************** face相关******************** #
def insertFace(personId,pictureId,faceEncoding,faceLocations,faceLandmarks,url):
    sql = "INSERT INTO face(person_id,picture_id,face_encoding,face_locations,face_landmarks,url)  " \
          "VALUES ('%s', '%s', '%s', '%s', '%s', '%s')" % \
          (personId,pictureId,faceEncoding,faceLocations,faceLandmarks,url)
    try:
        cursor.execute(sql)
        uid = db.insert_id()
        db.commit()
        # print('face sql保存成功')
        return uid
    except Exception as e:
        print('insertFace',e)
        db.rollback()
def insertFaceIntoPicture(picture_id,face_uid):
    try:
        sql = "UPDATE picture SET face_uid=%s WHERE id=%s"
        val = (face_uid, picture_id)
        cursor.execute(sql,val)
        db.commit()
    except Exception as e:
        print('insertFaceIntoPicture',e)
        db.rollback()
def get_face_count():
    try:
        sql = "SELECT COUNT(*) FROM face;"
        cursor.execute(sql)
        results = cursor.fetchall()
        return results[0][0]
        db.commit()
    except Exception as e:
        print(e)
        db.rollback()
def rewrite_known_data_from_db():
    # 先将文件清空
    with open('known_face_ids.txt', 'w') as f:
        f.write('')
    with open('known_face_encodings.txt', 'w') as f:
        f.write('')
    sql = "SELECT person_id,face_encoding FROM face"
    try:
        cursor.execute(sql)
        # 获取所有记录列表
        results = cursor.fetchall()
        # 存在就更新
        if results:
            for face in results:
                with open('known_face_ids.txt', 'a+') as f:
                    f.write(str(face[0]) + ' ')
                with open('known_face_encodings.txt', 'a+') as f:
                    f.write(re.sub(r'[\[\],]','',face[1]) + '\n')

    except Exception as e:
        print(e)
        db.rollback()
# picture操作
def insertPicture(title,location,widthH,lnglat,createTime,updateTime,url,size):
    sql = "INSERT INTO picture(title,location,width_h,lnglat,create_time,update_time,url,size)  " \
          "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')" % \
          (title,location,widthH,lnglat,createTime,updateTime,url,size)
    try:
        cursor.execute(sql)
        uid = db.insert_id()
        db.commit()
        # print('picture sql保存成功')
        return uid
    except Exception as e:
        print(e)
        db.rollback()
# person操作
def update_insert_person(person_id,picture_id):
    sql = "SELECT * FROM person where id  = %s"
    val = (person_id)
    try:
        cursor.execute(sql,val)
        # 获取所有记录列表
        results = cursor.fetchall()
        # 存在就更新
        if results:
            sql = "UPDATE person SET picture_uid = %s,count = %s where id = %s"
            if results[0][3]:
                if str(picture_id) + "," in results[0][3]:
                    val = (results[0][3] ,results[0][2] + 1,person_id)
                else:
                    val = (results[0][3] + str(picture_id) + ",",results[0][2] + 1,person_id)
            else:
                val = (str(picture_id) + ",",results[0][2] + 1,person_id)
            cursor.execute(sql,val)
        #  插入
        else:
            sql = "INSERT into person SET id = %s,name = %s,picture_uid = %s,count = %s"
            val = (person_id,'未命名',str(picture_id) + ",",1)
            cursor.execute(sql,val)
        db.commit()
    except Exception as e:
        print(e)
        db.rollback()
# 手动同步person和face数据
def sync_person_from_face():
    try:
        sql = "SELECT person_id,picture_id FROM face"
        cursor.execute(sql)
        # 获取所有记录列表
        results = cursor.fetchall()
        # 存在就更新
        for face in results:
            person_id = face[0]
            picture_id = face[1]
            sql = "SELECT * FROM person where id  = %s"
            val = (person_id)
            cursor.execute(sql,val)
            # 获取所有记录列表
            results = cursor.fetchall()
            if results:
                sql = "UPDATE person SET picture_uid = %s,count = %s where id = %s"
                if results[0][3]:
                    if str(picture_id) + "," in results[0][3]:
                        val = (results[0][3] ,results[0][2] + 1,person_id)
                    else:
                        val = (results[0][3] + str(picture_id) + ",",results[0][2] + 1,person_id)
                else:
                    val = (str(picture_id) + ",",results[0][2] + 1,person_id)
                cursor.execute(sql,val)
            #  插入
            else:
                sql = "INSERT into person SET id = %s,name = %s,picture_uid = %s,count = %s"
                val = (person_id,'未命名',str(picture_id) + ",",1)
                cursor.execute(sql,val)
            db.commit()
    except Exception as e:
        print(e)
        db.rollback()
syncData()

# 手动同步person和face



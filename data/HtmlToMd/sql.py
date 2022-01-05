import pymysql
import time
from markdownify import markdownify as md
import re
import requests
import os
import time
WEBSITE = "http://lpgogo.top/img/"
# 打开数据库连接
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
    key = 'GjG3XAdmywz7CyETWqHwIuEC6ZExY6QT'
    r = requests.get(url='http://api.map.baidu.com/geocoder/v2/',
                     params={'location': str(lat) + ',' + str(lng), 'ak': key, 'output': 'json'})
    result = r.json()
    # print(result)
    province = result['result']['addressComponent']['province']
    city = result['result']['addressComponent']['city']
    district = result['result']['addressComponent']['district']
    street = result['result']['addressComponent']['street']
    street_number = result['result']['addressComponent']['street_number']
    return city + " " + district + " " + street + " " + street_number
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
def md_sql(fileArr):
    # 周总结
    # | **创建时间：** | *2015/9/1 19:03* |
    # | **更新时间：** | *2018/9/28 19:40* |
    # | **位置：** | [*30°35'12 N  114°14'6 E*](http://maps.google.com/maps?z=6&q=30.586600,114.235000) |
    # | **标签：** | *周总结* |
    # 1.钓鱼，钓到了大鲶鱼，下池塘捞线捞漂，
    title = fileArr[0].strip()
    # 在第 1-4行进行查找
    createTime,updateTime, location, lng_lat, tagList, count = getFiled(fileArr[1:6])
    content = addHttp(WEBSITE,''.join(fileArr[count:]))
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
def addHttp(httpname,content):
    for i in re.compile(r'!\[\]\(.*?\)').findall(content):
        # 提取 (NAME) 中的 NAME
        matchObj = re.search( r'\((.*)\).*', i)
        # 加上网址前缀
        if matchObj.group(1):
            filename  = re.sub(r'_+', '_', re.sub(r'[\s\[\],，。]', '_', matchObj.group(1))).replace('_"点击下载"','')
            content = content.replace(i,'\n\n<img src="' + httpname +  filename + '" alt = "' +filename + '" style="zoom:30%;"/>\n\n')
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

# face操作
def insertFace(personId,pictureId,faceEncoding,faceLocations,faceLandmarks,url):
    sql = "INSERT INTO face(person_id,picture_id,face_encoding,face_locations,face_landmarks,url)  " \
          "VALUES ('%s', '%s', '%s', '%s', '%s', '%s')" % \
          (personId,pictureId,faceEncoding,faceLocations,faceLandmarks,url)
    try:
        cursor.execute(sql)
        uid = db.insert_id()
        db.commit()
        print('face sql保存成功')
        return uid
    except Exception as e:
        print(e)
        db.rollback()
def insertFaceIntoPicture(picture_id,face_uid):
    try:
        sql = "UPDATE picture SET face_uid=%s WHERE id=%s"
        val = (face_uid, picture_id)
        cursor.execute(sql,val)
        db.commit()
    except Exception as e:
        print(e)
        db.rollback()
def get_face_count():
    try:
        sql = "SELECT COUNT(*) FROM person;"
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
            sql = "UPDATE person SET picture_uid = %s,count = %s"
            if results[0][3]:
                if str(picture_id) + "," in results[0][3]:
                    val = (results[0][3] ,results[0][2] + 1)
                else:
                    val = (results[0][3] + str(picture_id) + ",",results[0][2] + 1)
            else:
                val = (str(picture_id) + ",",results[0][2] + 1)
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

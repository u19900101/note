from markdownify import markdownify as md
import re
import requests
import os
import time
from HtmlToMd.pyMyql import isTagExist, insertTag, insertNote, closeConn
WEBSITE = "http://lpgogo.top/img/"
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
dir = "D:\MyJava\mylifeImg\others\我的抗战2.0\\"
# dir = "./temp"
# 去除文件名称中的特殊字符
renameFiles(dir)
for i in os.listdir(dir):
    if i.endswith(".html") and (i.startswith("5.342") or i.startswith("5.343")or i.startswith("5.344")or i.startswith("5.345")):
        print(i)
        title, createTime, updateTime, location, lng_lat, tagList, content = md_sql(htmlToMd(dir,i))
        # 封装 tag 写进tag表中 写入 note表中
        insertNote(title,getTag_uid(tagList), createTime, updateTime, location, lng_lat,  str(content))
#       写入md
#       with open(dir + i.replace(".html",".md"), 'w', encoding='UTF-8') as f:
#           f.write(createTime + "\n" + updateTime+ "\n" +location+ "\n" +lng_lat+ "\n" +str(tagList)+ "\n" +content)
# 关闭数据库连接
closeConn()



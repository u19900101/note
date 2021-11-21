from markdownify import markdownify as md
import re
import requests
import os

from HtmlToMd.pyMyql import isTagExist, insertTag, insertNote, closeConn

# 查找所有 ![](NAME)
def addHttp(httpname,content):
    for i in re.compile(r'!\[\]\(.*?\)').findall(content):
        # 提取 (NAME) 中的 NAME
        matchObj = re.search( r'\((.*)\).*', i)
        # 加上网址前缀
        if matchObj.group(1):
            content = content.replace(matchObj.group(1),httpname + matchObj.group(1))
    return content
def gen_video_tag(s):
    return re.sub('(?P<value>\[.*?mp4\))', video_match, s)


def video_match(matched):
    value = matched.group('value')
    value = re.sub('(?P<value>\[.*\])', "\n", value)
    if value.endswith(".mp4)"):
        value = value.replace("(", "<video src=\"").replace(")", "\"></video>")
    return value


def htmlToMd(dir,htmlPath):
    mdFilePath = htmlPath.replace(".html", ".md")
    with open(dir + htmlPath, 'r', encoding='UTF-8') as f:
        res = ''
        htmlpage = f.read()
        # 处理html格式文件中的内容
        text = md(htmlpage)
        text = text.replace("body, td {\n font-family: 微软雅黑;\n font-size: 10pt;\n }\n", "")
        # 1.去除多余的换行
        text = re.sub(r'(\n)+', "\n", re.sub(r' +\n', '\n', text))
        text = text.replace("\xa0", " ")
        # 5.二次转化视频标签
        text = gen_video_tag(text)
        res += text
        res = res.split("\n")
        result = res[6:]
        # # 4.去掉第一行和第二行
        result.insert(0,res[2])
    # with open(dir + mdFilePath, 'w', encoding='UTF-8') as f:
    #     f.write('\n'.join(result))
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
        elif i.startswith("| **更新时间"):
            updateTime = pattern.findall(i)[0].replace("*", "").strip()
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
    content = addHttp("http://lpgogo.top/",''.join(fileArr[count:]))
    # print('title is ', title)
    # print('createTime is ', createTime)
    # print('updateTime is ', updateTime)
    # print('location is ', location)
    # print('lng_lat is ', lng_lat)
    # print('tagList is ', tagList)
    # print('content is ', content)
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


# htmlPath = "1.html"
# md_sql(htmlToMd(htmlPath))
dir = "D:\MyJava\mylifeImg\others\读研期间\\"
# dir = "temp\\"
for i in os.listdir(dir):
    if i.endswith(".html"):
        # print(i)
        title, createTime, updateTime, location, lng_lat, tagList, content = md_sql(htmlToMd(dir,i))
        # # 1.封装 tag 写进tag表中
        tag_uid = getTag_uid(tagList)
        # print(title, tagList,tag_uid, createTime, updateTime, location, lng_lat,  content[:10])
        # # 写入 note表中
        insertNote(title,tag_uid, createTime, updateTime, location, lng_lat,  str(content))
# 关闭数据库连接
closeConn()



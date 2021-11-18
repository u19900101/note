from markdownify import markdownify as md
import linecache
import re
import requests
def gen_video_tag(s):
    return re.sub('(?P<value>\[.*?mp4\))', video_match, s)
def video_match(matched):
    value = matched.group('value')
    value = re.sub('(?P<value>\[.*\])', "\n", value)
    if value.endswith(".mp4)"):
        value = value.replace("(","<video src=\"").replace(")","\"></video>")
    return value
def htmlToMd(path,descpath):
    with open(path, 'r', encoding='UTF-8') as f:
        res = ''
        htmlpage = f.read()
        # 处理html格式文件中的内容
        text = md(htmlpage)
        text = text.replace("body, td {\n font-family: 微软雅黑;\n font-size: 10pt;\n }\n","")
        # 1.去除多余的换行
        text = text.replace("\n\n\n\n","")
        # 2.生成换行
        text = text.replace("  \n","  \n\n")
        # 3.去掉特殊字符
        text = text.replace("\xa0"," ")
        # 4.去掉标题  只去掉匹配的第一个
        text = re.sub(r'\n\n.*\n', "", text,1)
        # 5.二次转化视频标签
        text = gen_video_tag(text)
        res += text
        with open(descpath, 'w', encoding='UTF-8') as f:
            f.write(res)
def get_locationName(lng,lat):
    key = 'GjG3XAdmywz7CyETWqHwIuEC6ZExY6QT'
    r = requests.get(url='http://api.map.baidu.com/geocoder/v2/', params={'location':str(lat)+','+str(lng),'ak':key,'output':'json'})
    result = r.json()
    print(result)
    province = result['result']['addressComponent']['province']
    city = result['result']['addressComponent']['city']
    district = result['result']['addressComponent']['district']
    street = result['result']['addressComponent']['street']
    street_number = result['result']['addressComponent']['street_number']
    return city+" "+district+" "+street+" "+street_number
path = "2.html"
descpath = '1.md'
htmlToMd(path,descpath)

# md 提取
title = linecache.getline(descpath, 1).strip()
createTime = linecache.getline(descpath, 6).strip()
# | **创建时间：** | *2021/1/8 23:31* |
updateTime = linecache.getline(descpath, 7).strip()
#  | **更新时间：** | *2021/1/8 23:42* |
location = linecache.getline(descpath, 8).strip()
# | **位置：** | [*9°32'32 N  112°52'52 E*](http://maps.google.com/maps?z=6&q=9.542190,112.881000) |
tagList = linecache.getline(descpath, 9).strip()
#  | **标签：** | *万卷书* | *万卷书* | *万卷书* |

pattern = re.compile(r'\*\d+.*\*')   # 查找数字
createTime = pattern.findall(createTime)[0].replace("*","")
updateTime = pattern.findall(updateTime)[0].replace("*","").strip()
locationLink = re.compile(r'http.*\)').findall(location)[0].replace(")","").strip()
location = pattern.findall(location)[0].replace("*","").strip()
tagList = [x.replace("*","").strip() for x in re.compile(r'\*.*?\*').findall(tagList)[2:]]

content = linecache.getlines(descpath)[11:]
# 替换掉多个换行为一次换行
content = re.sub(r' +\n','\n',''.join(content))
content = re.sub(r'(\n)+', "\n", content)

print('title is ',title)
print('createTime is ',createTime)
print('updateTime is ',updateTime)
print('location is ',location)
print('locationLink is ',locationLink)
print('tagList is ',tagList)
print('content is ',content)


print(get_locationName(110.27388888888888,20.0225))



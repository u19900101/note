from flask import Flask
from flask import request
import json
import face_recognition
import numpy as  np
import cv2
import dlib
import os.path
import uuid
import re
import sys
datapath_pre = '/mydata/python/py/'
# face保存的位置
faceimagepath_pre = '/mydata/nginx/html/face/'
class MyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        elif isinstance(obj, np.floating):
            return float(obj)
        elif isinstance(obj, np.ndarray):
            return obj.tolist()
        else:
            return super(MyEncoder, self).default(obj)
# 人脸对齐
def save_face(faceimagepath_pre, cv_bgr_image):
    # 生成唯一name，判断是否重名
    uid = str(uuid.uuid1()) + ".jpg"
    facepath = faceimagepath_pre + uid
    while(os.path.isfile(facepath)):
        uid = str(uuid.uuid1()) + ".jpg"
        facepath = faceimagepath_pre + uid
    if not os.path.exists(faceimagepath_pre):
        os.makedirs(faceimagepath_pre)
    cv2.imwrite(facepath,cv_bgr_image)
    return 'http://47.101.137.245/face/'+uid


def face_alignment(rgb_img):
    # opencv的颜色空间是BGR，需要转为RGB才能用在dlib中
    predicter_path =datapath_pre + 'shape_predictor_68_face_landmarks.dat'
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
        facepath = save_face(faceimagepath_pre,cv_bgr_image)
        face_paths.append(facepath)
    return face_paths
def getFaceInfo(imgpath):
    rectangle = [];
    keypoint = [];
    scale = 1
    img = cv2.imread(imgpath) # 0.22s
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

    face_locations = face_recognition.face_locations(img_rgb,1)
    face_landmarks = face_recognition.face_landmarks(img,face_locations) #72个点
    face_encodings = face_recognition.face_encodings(img,face_locations)

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
        return
    # 4*1
    faceDic['face_locations'] = (np.array(rectangle)/scale).astype(int).tolist()
    # 72*2
    faceDic['face_landmarks'] = (np.array(keypoint)/scale).astype(int).tolist()
    # 128*1
    faceDic['face_encodings'] = np.asarray(face_encodings).tolist()

    # 与数据库比对  得到人脸 id  新人脸就在原有人脸最大id上+1，得到新id
    faceDic['face_name_ids'] = getFaceIndex(face_encodings)
    # pic_id,face_num,locations,face_ids,landmarks
    # 此处的输出是给java控制台传递值
    # print("与数据库对比后的人脸id: ",faceDic['face_name_ids'])
    # print("人脸编码：128*1 ",[a for a in  faceDic['face_encodings']])
    # print("人脸位置： 矩形",[a for a in  faceDic['face_locations']])
    # print("特征点位置：72*2 ",[a for a in  faceDic['face_landmarks']])

    # print([a for a in  faceDic['face_encodings']])
    # print([a for a in  faceDic['face_locations']])
    # print([a for a in  faceDic['face_landmarks']])
    # 人脸对齐
    faceDic['face_urls'] = face_alignment(img_rgb)
    return faceDic
def reWrite_known_data(face_id, face_encoding):
    with open(datapath_pre + 'known_face_ids.txt', 'a+') as f:
        f.write(str(face_id)+' ')
    with open(datapath_pre + 'known_face_encodings.txt', 'a+') as f:
        f.write(str(face_encoding.tolist()).replace("[","").replace("]","").replace(","," ") + '\n')
    print("写入known_data.txt")
def getFaceIndex(face_encodings):
    # 从本地读取人脸文件
    known_face_encodings = np.loadtxt(datapath_pre + 'known_face_encodings.txt').tolist()
    known_face_ids = np.loadtxt(datapath_pre + 'known_face_ids.txt').tolist()
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
            #  更新两个txt文件
            reWrite_known_data(face_id,face_encoding)
            faceId.append(face_id)
            face_id += 1
        return faceId


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

app = Flask(__name__)

def request_parse(req_data):
    # '''解析请求数据并以json形式返回'''
    if req_data.method == 'POST':
        data = req_data.json
    elif req_data.method == 'GET':
        data = req_data.args
    return data

@app.route('/', methods = ["GET","POST"])   # GET 和 POST 都可以
def get_data():
    print("come into...")
    data = request_parse(request)
    print(data)
    # 假设有如下 URL
    # http://47.101.137.245:5000?imageUrl=http://47.101.137.245/img/l.jpg
    imgpath = data.get("imageUrl")
    print(imgpath)
    print(imgpath.replace("http://47.101.137.245","/mydata/nginx/html"))
    # imgpath = "/mydata/nginx/img/html/l.jpg"
    res = getFaceInfo(imgpath.replace("http://47.101.137.245","/mydata/nginx/html"))

    return json.dumps(res,cls=MyEncoder)

if __name__ == '__main__':
    # 此处要配置为内网ip 不能配置为127.0.0.1 不然无法远程访问
    app.run(host = '172.24.87.228',port = 5000)

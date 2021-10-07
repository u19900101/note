let response = {
  "data": {
    "nowpage": 1,
    "totle_page": 1,
    "user_id": "11",
    "user_createdate": "2015-03-10 12:54",
    "user_phone": "18614088211",
    "user_status": "2",
    "last_logindate": "2015-03-10 12:55",
  }
}
for (let key in response.data) {
  console.log(key, response.data[key]);
  // for (b in response.data[a]) {
  //   console.log(b + ' : ' + response.data[a][b])
  // }
}

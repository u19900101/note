function f(...params) {
  for(let i of params){
    console.log(i)
  }

  for(let i in params){
    console.log(i,params[i])
  }
}

f('kkkkk',2,3)

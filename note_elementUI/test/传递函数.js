function f(funcName,...param) {
    return funcName(...param)
}

function add(a,b) {
    return a+b
}

function substr(a,b) {
    return a-b
}

console.log(f(substr,1,2))
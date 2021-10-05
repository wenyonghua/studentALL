const $ = layui.$
const layer = layui.layer
const baseUrl = 'http://localhost:8081/draw'
let drawLevel = 0
const table = layui.table
function requestFullScreen(element) {
    var requestMethod = element.requestFullScreen || element.webkitRequestFullScreen || element.mozRequestFullScreen || element.msRequestFullScreen;
    if (requestMethod) {
        requestMethod.call(element);
    } else if (typeof window.ActiveXObject !== "undefined") {
        var wscript = new ActiveXObject("WScript.Shell");
        if (wscript !== null) {
            wscript.SendKeys("{F11}");
        }
    }
}
function initCanvasSize() {
    requestFullScreen(document.documentElement)
    $("#canvasOne").width(window.screen.width + 'px');
    $("#canvasOne").height(window.screen.height + 'px');
}

// 抽奖开始
function draw_start() {
    $('#draw-start-btn').hide()
    console.log("抽奖开始滚动！")
    $('#draw-end-btn').show()
    speed = 50
    $('.result-div').show()
    startRollStaff()
    $('.result-header').hide()
}
// 抽奖结束主要处理逻辑
function draw_done(staff) {
    displayStaff(staff)
    stopRollStaff()
    checkShowNext()
    updateDrawNumber()
    $('.result-header').show()
}
// 点击进入下一关
function draw_next() {
    $('#draw-next-btn').hide()
    $('.result-div').hide()
    initDraw()
    speed = 1
    $('#draw-start-btn').show()
}

// 获取所有用户姓名
function get_all_staff_name() {
    $.ajax({
        url: baseUrl + '/getAllStaff',
        method: 'get',
        dataType: 'json',
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                for(let staff of res.data) {
                    staffNameList.push(staff.staffName)
                    staffList.push(staff)
                }
            } else {
                layer.msg('失败！')
            }
        },
        error: function (error) {
            console.log(error)
            layer.msg('异常！')
        }
    })
}

// 点击重置按钮事件
function on_reset_draw_staff() {
    layer.msg('是否真的要重置所有获奖信息？', {
        time: 20000, //20s后自动关闭
        btn: ['重置', '取消'],
        btn1: function (index) {
            reset_draw_staff(index)
        },
        btn2: function (index) {
            layer.close(index)
        }
    })
}

// 重置获奖信息
function reset_draw_staff(index) {
    $.ajax({
        url: baseUrl + '/resetDrawStaff',
        method: 'post',
        dataType: 'json',
        contentType: 'application/json',
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                layer.msg('成功！')
                initDraw()
                $('.result-div').hide()
            } else {
                layer.msg('失败！')
            }
            layer.close(index)
        },
        error: function (error) {
            console.log(error)
            layer.msg('异常！')
            layer.close(index)
        }
    })
}

// 渲染表格数据
const tableU = table.render({
    elem: '#draw-staff-table',
    url: baseUrl + '/getDrawStaff',
    page: false,
    toolbar: 'default',
    cols: [[
        {field: 'drawName', title: '奖项'},
        {field: 'staffId', title: '工号'},
        {field: 'staffName', title: '姓名'},
        {field: 'staffMobile', title: '电话'},
        {field: 'drawTime', title: '抽奖时间'},
        {fixed: 'right', title: '操作',width:80, toolbar: $("#tableTool")},
    ]]
})

// 展示获奖信息
function show_draw_staff() {
    tableU.reload()
    layer.open({
        type: 1,
        content: $("#draw-staff-table-div"),
        area: '900px',
        title: '获奖人员列表'
    })
}

// 表格按钮点击
table.on('tool(draw-staff-table)', function (obj) {
    console.log(obj)
    if (obj.event === 'del') {
        layer.msg('是否真的要删除此获奖信息？', {
            time: 20000, //20s后自动关闭
            btn: ['删除', '取消'],
            btn1: function (index) {
                delDrawStaff(obj.data, index)
            },
            btn2: function (index) {
                layer.close(index)
            }
        });
    }
})

// 删除获奖信息
function delDrawStaff(data, index) {
    $.ajax({
        url: baseUrl + '/delDrawStaff',
        method: 'post',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({
            staffId: data.staffId
        }),
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                layer.msg('成功！')
            } else {
                layer.msg('失败！')
            }
            tableU.reload()
            layer.close(index)
        },
        error: function (error) {
            console.log(error)
            layer.msg('异常！')
            layer.close(index)
        }
    })
}

// 默认用户信息
const defaultStaff = {
    staffId: '**',
    staffName: '***',
    staffMobile: '186****1234'
}

// 获奖框展示信息
function displayStaff(staff) {
    $("#staff-id-span").text(staff.staffId)
    $("#staff-name-span").text(staff.staffName)
    $("#staff-phone-span").text(staff.staffMobile)
}

var rollStaffTimer = null;

// 随机展示一个用户信息
function rollDisplayStaff() {
    const i = Math.ceil(Math.random() * staffList.length)
    if(i>0) {
        displayStaff(staffList[i-1])
    } else {
        displayStaff(staffList[0])
    }

}
function startRollStaff() {
    rollStaffTimer = setInterval(rollDisplayStaff,1000/speed)
}
function stopRollStaff() {
    window.clearInterval(rollStaffTimer)
}
// 抽奖，抽奖完成后判断是否显示下一级按钮
function draw_end() {
    $('#draw-end-btn').hide()
    $.ajax({
        url: baseUrl + '/draw',
        method: 'post',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({drawLevel: drawLevel}),
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                let staffInfo = res.data
                draw_done(staffInfo)
            } else {
                layer.msg('操作失败，请检查！')
            }
            speed = 1
        },
        error: function (error) {
            console.log(error)
            layer.msg('操作异常！')
            speed = 1
        }
    })
}

// 检查下一级的按钮是否展示
function checkShowNext() {
    $.ajax({
        url: baseUrl + '/getDrawInfo',
        method: 'get',
        dataType: 'json',
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                let resultList = res.data
                for (let item of resultList) {
                    if (item.number > item.drawNumber) {
                        if (drawLevel > item.level) {
                            console.log('目前级别：' + drawLevel)
                            console.log('数据库中级别：' + item.level)
                            console.log('可以切换下一级')
                            $('#draw-next-btn').show()
                            break
                        } else {
                            $('#draw-start-btn').show()
                            break
                        }
                    }
                }
            } else {
                layer.msg('操作失败，请检查！')
            }
        },
        error: function (error) {
            console.log(error)
            layer.msg('操作异常！')
        }
    })
}

// 更新抽奖剩余人数信息
function updateDrawNumber() {
    $.ajax({
        url: baseUrl + '/getDrawInfo',
        method: 'get',
        dataType: 'json',
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                let optionList = res.data
                for (let item of optionList) {
                    if(drawLevel === item.level) {
                        $('#draw-level-text').text('共' + item.number + '位，剩余' + (item.number-item.drawNumber) + '位')
                        break;
                    }
                }
            } else {
                layer.msg('操作失败，请检查！')
            }
        },
        error: function (error) {
            console.log(error)
            layer.msg('操作异常！')
        }
    })
}

// 处理抽奖级别信息
function initDraw() {
    $.ajax({
        url: baseUrl + '/getDrawInfo',
        method: 'get',
        dataType: 'json',
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                let optionList = res.data
                for (let item of optionList) {
                    if (item.number > item.drawNumber) {
                        drawLevel = item.level
                        $('#draw-level').text(item.name)
                        $('#draw-level-text').text('共' + item.number + '位，剩余' + (item.number-item.drawNumber) + '位')
                        break;
                    }
                }
            } else {
                layer.msg('操作失败，请检查！')
            }
        },
        error: function (error) {
            console.log(error)
            layer.msg('操作异常！')
        }
    })
}

/**
 * 初始化标题
 */
function initTitle() {
    $.ajax({
        url: baseUrl + '/getOption',
        method: 'post',
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({type: 'title'}),
        success: function (res) {
            console.log(res)
            if (res.code === 0) {
                let optionList = res.data
                for(const item of optionList) {
                    switch (item.code) {
                        case 'main': {
                            $('#main-title').text(item.value)
                            break;
                        }
                        /*case 'sub': {
                            if(item.value !== '') {
                                $('#sub-title').text(item.value)
                            }
                            break;
                        }*/
                    }
                }
            } else {
                layer.msg('操作失败，请检查！')
            }
        },
        error: function (error) {
            console.log(error)
            layer.msg('操作异常！')
        }
    })
}
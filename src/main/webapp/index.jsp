<%@ page language="java" import="java.util.*" contentType="text/html;charset=utf-8"%>
<html>
<head>
    <title>登陆页面</title>
    <style>
        *{
            margin: 0;
            padding: 0;
        }
        .user{
            border:1px solid #000;
            height: 200px;
            width: 400px;
        }
        .load{
            border:1px solid #000;
            height: 150px;
            width: 180px;
        }
    </style>
</head>
<body>
<%--用了ajax就不需要用form表单--%>
<div class="load">
    <input id="username" type="text" placeholder="请输入用户名">
    <input id="password" type="password" placeholder="请输入密码" >
    <button>登陆</button>
</div>
<br>
<br>
<br>
<br>
<br>
<br>
<div class="user">
    <table id="box1"></table>
    <table id="box2"></table>
    <table id="box3"></table>
    <table id="box4"></table>
    <table id="box5"></table>
    <table id="box6"></table>
</div>
</body>
<%--先放下面吧，虽然还不知道为啥--%>
<script src="js/jquery-1.12.4.js"></script>
<script>
    //不需要执行窗口执行，若执行必须在点击事件之后获取username和password
        var oBtn = document.querySelector("button");
        oBtn.onclick = function () {
            var username = $("#username").val();
            var password = $("#password").val();//注意是val不是value
            $.ajax({
                url:"/user/login.do",
                contentType:'application/json;charset=utf-8',
                data:{
                    "username":username,//必须用js获取它的值才能调用，不然是不能直接调用的
                    "password":password//注意名字必须和后端传参名字一样
                },
                type:"get",
                dataType: "json",/*后端返回的数据格式json*/
                success: function (sr) {
                    $('#box1').html("id:"+sr.data.id);
                    $('#box2').html("username:"+sr.data.username);
                    $('#box3').html("email:"+sr.data.email);
                    $('#box4').html("phone:"+sr.data.phone);
                    $('#box5').html("createTime:"+sr.data.createTime);
                    $('#box6').html("updateTime:"+sr.data.updateTime);
                }
            });
    }
</script>
</html>

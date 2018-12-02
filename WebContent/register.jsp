<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head></head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>会员注册</title>
<link rel="stylesheet" href="css/bootstrap.min.css" type="text/css" />
<script src="js/jquery-1.11.3.min.js" type="text/javascript"></script>
<script src="js/bootstrap.min.js" type="text/javascript"></script>
<!-- 引入自定义css文件 style.css -->
<link rel="stylesheet" href="css/style.css" type="text/css" />

<style>
body {
	margin-top: 20px;
	margin: 0 auto;
}

.carousel-inner .item img {
	width: 100%;
	height: 300px;
}

font {
	color: #3164af;
	font-size: 18px;
	font-weight: normal;
	padding: 0 10px;
}

.form-group{
	position: relative;
}
label.error{
	color: #f00;
	position: absolute;
	top: 0px;
	bottom: 0px;
	left: 100%;
	width: 120px;
	margin-top: 6px;
}

label[for="sex"]{
	position: relative;
	top: 5px;
	left: 5px;
}
label[for="validate-code"]{
	position: static;
	display: inline-block;
}
</style>
</head>
<body>

	<!-- 引入header.jsp -->
	<jsp:include page="/header.jsp"></jsp:include>

	<div class="container"
		style="width: 100%; background: url('image/regist_bg.jpg');">
		<div class="row">
			<div class="col-md-2"></div>
			<div class="col-md-8"
				style="background: #fff; padding: 40px 80px; margin: 30px; border: 7px solid #ccc;">
				<font>会员注册</font>USER REGISTER
				<form id="register-form" class="form-horizontal" style="margin-top: 5px;" action="${pageContext.request.contextPath }/user?method=register" method="POST">
					<div class="form-group">
						<label for="username" class="col-sm-2 control-label">用户名</label>
						<div class="col-sm-6">
							<input type="text" name="username" class="form-control" id="username"
								placeholder="请输入用户名">
						</div>
					</div>
					<div class="form-group">
						<label for="password" class="col-sm-2 control-label">密码</label>
						<div class="col-sm-6">
							<input type="password" name="password" class="form-control" id="password"
								placeholder="请输入密码">
						</div>
					</div>
					<div class="form-group">
						<label for="confirmpwd" class="col-sm-2 control-label">确认密码</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="confirmpwd"
								placeholder="请输入确认密码" name="confirmpwd">
						</div>
					</div>
					<div class="form-group">
						<label for="inputEmail3" class="col-sm-2 control-label">Email</label>
						<div class="col-sm-6">
							<input type="email" name="email" class="form-control" id="inputEmail3"
								placeholder="Email">
						</div>
					</div>
					<div class="form-group">
						<label for="usercaption" class="col-sm-2 control-label">姓名</label>
						<div class="col-sm-6">
							<input type="text" name="name" class="form-control" id="usercaption"
								placeholder="请输入姓名">
						</div>
					</div>
					<div class="form-group opt">
						<label for="inlineRadio1" class="col-sm-2 control-label">性别</label>
						<div class="col-sm-6">
							<label class="radio-inline"> <input type="radio"
								name="sex" id="inlineRadio1" value="male">
								男
							</label> <label class="radio-inline"> <input type="radio"
								name="sex" id="inlineRadio2" value="female">
								女
							</label>
							<label for="sex" generated="true" class="error" style="display:none;">性别必选</label>
						</div>
					</div>
					<div class="form-group">
						<label for="birthday" class="col-sm-2 control-label">出生日期</label>
						<div class="col-sm-6">
							<input type="date" id="birthday" name="birthday" class="form-control">
						</div>
					</div>

					<div class="form-group">
						<label for="validate-code" class="col-sm-2 control-label">验证码</label>
						<div class="col-sm-3">
							<input type="text" name="validate-code" class="form-control">
						</div>
						<div class="col-sm-2" style="padding-top: 2px;">
							<img style="width: 100%;height: 100%;" id="validate-code-img" src="${pageContext.request.contextPath }/checkImg" />
						</div>
						<label for="validate-code" generated="true" class="error" style="display:none;"></label>
					</div>

					<div class="form-group">
						<div class="col-sm-offset-2 col-sm-10">
							<input type="submit" width="100" value="注册" name="submit"
								style="background: url('./images/register.gif') no-repeat scroll 0 0 rgba(0, 0, 0, 0); height: 35px; width: 100px; color: white;">
						</div>
					</div>
				</form>
			</div>

			<div class="col-md-2"></div>

		</div>
	</div>

	<!-- 引入footer.jsp -->
	<jsp:include page="/footer.jsp"></jsp:include>

	<script src="${pageContext.request.contextPath }/js/jquery-1.11.3.min.js"></script>
	<script src="${pageContext.request.contextPath }/js/jquery.validate.min.js"></script>
	<script>
		$('#validate-code-img').on('click', function(e){
			$(this).attr('src', '${pageContext.request.contextPath }/checkImg?t='+Date.now());
		});

		// add username existence check method 
		$.validator.addMethod('checkUsername', function(val, ele, params){
			let isOccupied = false;
			$.ajax({
				url: '${pageContext.request.contextPath}/user?method=checkUsername',
				type: 'POST',
				async: false,
				data: {
					username: ele.value
				},
				dataType: 'json',
				success: function(data){
					isOccupied = data.isOccupied;
				},
				error: function(e){
					console.log(e);
				}
			});
			return !isOccupied;
		}, '用户名已被占用');
		
		// add username existence check method 
		$.validator.addMethod('checkValidateCode', function(val, ele, params){
			let isRight = false;
			$.ajax({
				url: '${pageContext.request.contextPath}/user&method=checkValidateCode',
				type: 'POST',
				async: false,
				data: {
					validateCode: ele.value
				},
				dataType: 'json',
				success: function(data){
					isRight = data.isRight;
				},
				error: function(e){
					console.log(e);
				}
			});
			return isRight;
		}, '验证码不正确');
		
		// validate form
		$('#register-form').validate({
			rules: {
				username: {
					required: true,
					checkUsername: true
				},
				password: {
					required: true,
					rangelength: [6, 16]
				},
				confirmpwd:{
					required: true,
					equalTo: '#password'
				},
				email:{
					required: true,
					email: true
				},
				name:{
					required: true
				},
				sex:{
					required: true
				},
				birthday:{
					required: true
				},
				'validate-code':{
					required: true,
					checkValidateCode: true
				}
			},
			messages: {
				username: {
					required: '用户名必填',
					checkUsername: '用户名已被占用'
				},
				password: {
					required: '密码必填',
					rangelength: '密码必须为6至16位'
				},
				confirmpwd:{
					required: '确认密码必填',
					equalTo: '密码不一致'
				},
				email:{
					required: '邮箱必填',
					email: '邮箱格式不合法'
				},
				name:{
					required: '姓名必填'
				},
				sex:{
					required: '性别必选'
				},
				birthday:{
					required: '生日必填'
				},
				'validate-code':{
					required: '验证码必填'
				}
			}
		});
	</script>
</body>
</html>





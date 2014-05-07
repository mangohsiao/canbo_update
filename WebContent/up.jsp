<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript" src="js/jquery-1.10.2.min.js">

</script>

<script type="text/javascript">
    	
    function callback(){
    	console.log("in callback().");
    	$.ajax({
			type:"POST",
			url:"FileUploadStatus",
			async:false,
			success:function(msg){
				console.log("in return.");
				$("#span_text").html("已上传："+msg);
				document.getElementById("table_width").width=msg;
			},
			error:function(){
				console.log("in failed.");
				$("#span_text").html("失败");
			}
		});
    }
    
    function formSubmit(){
    	setInterval("callback()", 100);//每隔100毫秒执行callback
    	document.form.submit();
    }
 </script>

</head>

<body>
<form action="mUpload" method="post" enctype="multipart/form-data" target="_parent" name="form">
    	<input type="file" name="file" >
    	<input type="button" onclick="formSubmit()" value="提交">
    </form>
    
    <button name="test" onclick="callback();">test</button>
    
    <span id="span_text"></span>
    <table width="300px;" border="0"><tr><td>
    <table id="table_width" height="20px;" style="background-color: gray;"><tr><td></td></tr></table>
    </td>
    </tr>
   </table>
</body>
</html>
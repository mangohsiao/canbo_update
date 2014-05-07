package com.emos.update.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileLock;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.*;

import com.emos.update.apkinfo.AnalysisApk;
import com.emos.update.common.DBconnector;


/**
 * Servlet implementation class MyUpload
 */
//@WebServlet("/upload")
public class MyUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyUpload() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();

		String filePath = getServletConfig().getInitParameter("folder_path");
		
		FileUpload fu=new FileUpload();
		fu.setMap(request);//解析request
		Map<String,FileItem> files=fu.getFiles();
		String fileName = fu.getFileName(files.get("file"));
		filePath += File.separator;
		
		File file=new File(filePath + fileName);
		try {
			FileItem it = files.get("file");
			it.write(file);
			it.getOutputStream().close();
		} catch (Exception e) {
			e.printStackTrace();
			out.println("<script>alert('write files failed.');history.back();</script>");
			return;
		}
		
		try {
			Map<String, String> map = AnalysisApk.getApkInfo(filePath + fileName);
			if(map!=null){
//				System.out.println(map.get(AnalysisApk.PACKAGE));
//				System.out.println(map.get(AnalysisApk.VERSION_CODE));
//				System.out.println(map.get(AnalysisApk.VERSION_NAME));
				/* insert into db */
				int rtvl = insert_data(map, filePath, fileName, "app desc");
				if(0 == rtvl){
					out.println("<script>alert('上传成功！package:" 
							+ map.get(AnalysisApk.PACKAGE) 
							+ map.get(AnalysisApk.VERSION_CODE)
							+ map.get(AnalysisApk.VERSION_NAME)
							+ "');history.back();</script>");
				}else if(-1 == rtvl){
					out.println("<script>alert('上传成功！apk解析错误,请确认apk完整性');history.back();</script>");
					/* delete the file not correct */
				}else if(-2 == rtvl){
					out.println("<script>alert('数据库中不存在该app，请注册添加');history.back();</script>");
				}else if(-3 == rtvl){
					out.println("<script>alert('rename file error.');history.back();</script>");
				}else if(-4 == rtvl){
					file.delete();
					out.println("<script>alert('file already exist.');history.back();</script>");
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("上传成功！文件解析错误");
			out.println("<script>alert('上传成功！文件解析错误');history.back();</script>");
			/* delete the file not correct */
			if(file.isFile() && file.exists()){
				file.delete();
				System.out.println("file exist deleted.!");
			}else {
				System.out.println("path : " + file.getAbsolutePath());
			}
		}
	}
    
	private int insert_data(Map<String, String> map, String filePath, String fileName, String update_desc) {
		// TODO Auto-generated method stub
		String package_name = map.get(AnalysisApk.PACKAGE);
		String ver_code = map.get(AnalysisApk.VERSION_CODE);
		String ver_name = map.get(AnalysisApk.VERSION_NAME);
		
		if(package_name==null||ver_code==null||ver_name==null){
			return -1;
		}
		
		int rtvl = -1;
		
		int app_id = -1;
		String select_id_sql = "SELECT `app_id` FROM `tbl_app` WHERE `package_name`='" + package_name + "'";

		try {
			Connection con = DBconnector.getConnection();
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery(select_id_sql);
			if(rs.next()){
				//app exist
				app_id = rs.getInt("app_id");
			}else{
				//app not exist
				//TODO delete files;
				return -2;
			}
			rs.close();

//			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
//			java.util.Date date=new java.util.Date();  
//			String str=sdf.format(date);  
			
			//rename_file
			String nFileName = package_name + "-" + ver_code + ".apk";	//new file name

			rtvl = renameFile(filePath,fileName,nFileName);
			if(0 != rtvl){
				return rtvl;
			}
			
			String file_path = filePath.replaceAll("\\\\", "@");
			file_path += nFileName;
			System.out.println(file_path);
			
			//calculate file sum
			String file_sum = "MD5";
			
			String insert_sql = "INSERT INTO tbl_files(`app_id`,`app_ver_code`,`ver_name`,`update_desc`,`file_sum`,`file_path`)"
					+ "VALUES('" + app_id + "','"+ ver_code+"','"+ver_name+"','"+ update_desc+"','"+ file_sum+"','"+ file_path+"');";
			System.out.println(insert_sql);
			
			int res = stmt.executeUpdate(insert_sql);
			if(res>0){
				System.out.println("insert!");
			}
			stmt.close();
			con.close();			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}

	public int renameFile(String path,String oldname,String newname){ 
        if(!oldname.equals(newname)){//新的文件名和以前文件名不同时,才有必要进行重命名 
            File oldfile=new File(path+oldname); 
            File newfile=new File(path+newname); 
            if(!oldfile.exists()){
                return -3;//重命名文件不存在
            }
            if(newfile.exists()){//若在该目录下已经有一个文件和新文件名相同，则不允许重命名 
                System.out.println(newname+"已经存在！"); 
            	return -4;
            }else{ 
                if(oldfile.renameTo(newfile)){
                	return 0;
                }else{
                	return -3;
                }
            } 
        }else{
            System.out.println("新文件名和旧文件名相同...");
            return -3;
        }
    }
}

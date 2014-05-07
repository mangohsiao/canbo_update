package com.emos.update.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.emos.update.common.*;


/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/getfile")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private String contentType = "application/x-msdownload";
    private String enc = "utf-8";
    private String fileRoot = "";
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		String tempStr = config.getInitParameter("contentType");
        if (tempStr != null && !tempStr.equals("")) {
            contentType = tempStr;
        }
        tempStr = config.getInitParameter("enc");
        if (tempStr != null && !tempStr.equals("")) {
            enc = tempStr;
        }
        tempStr = config.getInitParameter("fileRoot");
        if (tempStr != null && !tempStr.equals("")) {
            fileRoot = tempStr;
        }
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String file_id = request.getParameter("file_id");
		
		if(file_id==null){
			return;
		}
		
		/* search DB and download file */
		String select_sql = "SELECT `file_path`,`file_sum` FROM `canbo`.`tbl_files` WHERE `file_id`='" + file_id + "';";

		//get the file path of version & pkg_name
		String real_file_path = "";
		
		try {
			Connection con = DBconnector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select_sql);
			
			if (rs.next()) {
				real_file_path = rs.getString("file_path");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		

		if(real_file_path.equals("")){
//			response.getWriter().write("-1");
			return;
		}
		
		
//		String filepath = request.getParameter("filepath");
//      String fullFilePath = fileRoot + filepath;
//		String fullFilePath = "E:\\workspace_web\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\update\\Canbo.apk";
        /*读取文件*/
		String path = real_file_path.replaceAll("@", "\\\\");
        File file = new File(path);
        /*如果文件存在*/
        if (file.exists()) {
            String filename = URLEncoder.encode(file.getName(), enc);
            response.reset();
            response.setContentType(contentType);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            int fileLength = (int) file.length();
            response.setContentLength(fileLength);
            /*如果文件长度大于0*/
            if (fileLength != 0) {
                /*创建输入流*/
                InputStream inStream = new FileInputStream(file);
                byte[] buf = new byte[4096];
                /*创建输出流*/
                ServletOutputStream servletOS = response.getOutputStream();
                int readLength;
                while (((readLength = inStream.read(buf)) != -1)) {
                    servletOS.write(buf, 0, readLength);
                }
                inStream.close();
                servletOS.flush();
                servletOS.close();
            }
        }else {
			//not exist
    		System.out.println("FILE not exist.");
    		response.setStatus(204);
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

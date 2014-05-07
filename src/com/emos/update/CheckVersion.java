package com.emos.update;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import com.emos.update.common.Common;
import com.emos.update.common.DBconnector;

import sun.org.mozilla.javascript.internal.json.JsonParser;

/**
 * Servlet implementation class CheckVersion
 */
@WebServlet("/checkversion")
public class CheckVersion extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckVersion() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		int res = Common.CHECK_SUCCESS;
		int ver_code = -1;
		int file_id = -1;
		String update_desc = "";
		String ver_name = "";
		String file_sum = "";
		JSONObject json = new JSONObject();
		
		String package_name = request.getParameter("pkg_name");
		if(package_name==null){
			System.out.print("pkg_name is null.");
			res = Common.PACKAGE_NAME_NULL; // PACKAGE NAME == NULL
			try {
				json.put("RES", res);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			out.println(json.toString());
			out.flush();
			return;
		}
		
		String select_sql = "SELECT `newest_ver_code`,`file_id` FROM `tbl_app` WHERE `package_name`='" + package_name + "' LIMIT 1;";
		/* check the newest version */
		try {
			Connection con = DBconnector.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(select_sql);
			if (rs.next()) {
				ver_code = rs.getInt("newest_ver_code");
				file_id = rs.getInt("file_id");
			}else{
				res = Common.PACKAGE_NOT_EXIST; //Package not exist.
			}
			stmt.close();
			con.close();
		} catch (Exception e) {
			System.out.println("DataBase connection failed.");
			res = Common.DB_CONNECT_FAILED;
			e.printStackTrace();
		}
		
		if(file_id > 0){
			/* check the newest version */
			String select_file_sql = "SELECT * FROM `tbl_files` WHERE `file_id`='" + file_id + "';";
			try {
				Connection con = DBconnector.getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(select_file_sql);
				if (rs.next()) {
					update_desc = rs.getString("update_desc");
					file_sum = rs.getString("file_sum");
					ver_name = rs.getString("ver_name");
				}else {
					res = Common.FILE_NOT_EXIST; //File not exist.
				}
				stmt.close();
				con.close();
			} catch (Exception e) {
				res = Common.DB_CONNECT_FAILED;
				e.printStackTrace();				
			}
		}

		try {
			json.put("RES", res);
			json.put("ver_code", ver_code);
			json.put("update_desc", update_desc);
			json.put("file_id", file_id);
			json.put("ver_name", ver_name);
			json.put("file_sum", file_sum);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		out.println(json.toString());
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}

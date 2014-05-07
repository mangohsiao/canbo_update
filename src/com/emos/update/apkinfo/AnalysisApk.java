
package com.emos.update.apkinfo;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;




import android.content.res.AXmlResourceParser;
//import android.content.res.AXmlResourceParser;
import android.util.TypedValue; 

/**
 * ����APK�ļ���ȡ��APK�ļ��е� �������汾�ż�ͼ��
 */
public class AnalysisApk {
	public final static String VERSION_CODE = "versionCode";
	public final static String PACKAGE = "package";
	public final static String VERSION_NAME = "versionName";
	
	public static Map<String,String> getApkInfo(String filePath) throws IOException,Exception{
		return unZip(filePath, null);
	}
	
	/**
     * ��ѹ zip �ļ�(apk���Ե���һ��zip�ļ�)��ע�ⲻ�ܽ�ѹ rar �ļ�Ŷ��ֻ�ܽ�ѹ zip �ļ� ��ѹ rar �ļ� ����� 
     * java.io.IOException: Negative seek offset �쳣 create date:2009- 6- 9 author:Administrator
     * @param apkUrl
     *             zip �ļ���ע��Ҫ�����ڵ� zip �ļ�Ŷ�������ǰ� rar ��ֱ�Ӹ�Ϊ zip ��������� java.io.IOException:
     *             Negative seek offset �쳣
     * @param logoUrl
     * 			   ͼ�����ɵĵ�ַ
     * @throws IOException
     */
   public static Map<String,String> unZip(String apkUrl, String logoUrl) throws IOException,Exception
   {   
	  //[0]:�汾��;[1]����
	  Map<String, String> map = new HashMap<String, String>();
      byte b[] = new byte [1024];
      int length; 
      ZipFile zipFile = null;
      File origin_file = null;
      try {
    	  origin_file = new File(apkUrl);
          zipFile = new ZipFile(origin_file);       
          Enumeration enumeration = zipFile.entries();
          ZipEntry zipEntry = null ;
          while (enumeration.hasMoreElements()) {
             zipEntry = (ZipEntry) enumeration.nextElement();           
             if (zipEntry.isDirectory()) {
                
             } else {
                 if("AndroidManifest.xml".equals(zipEntry.getName()))
                 {
//                	 System.out.println("��AndroidManifest.xml");
             		try {
             			AXmlResourceParser parser = new AXmlResourceParser();
             			parser.open(zipFile.getInputStream(zipEntry));

             			while (true) {
							int type = parser.next();
             				if (type==XmlPullParser.END_DOCUMENT) {
             					break;
             				}
             				switch (type) {
							case XmlPullParser.START_TAG:
	                 			for (int j = 0; j < parser.getAttributeCount(); j++) {
	                 				
	                 				if("package".equals(parser.getAttributeName(j))){
	                 					
//	                 					System.out.println(parser.getAttributeValue(j));
	                 					map.put("package", getAttributeValue(parser,j));
	                 					
	                 				}else if("versionCode".equals(parser.getAttributeName(j))){

	                 					map.put("versionCode", getAttributeValue(parser,j));
	                 					
	                 				}else if("versionName".equals(parser.getAttributeName(j))){

	                 					map.put("versionName", getAttributeValue(parser,j));
	                 				}
	    						}								
								break;

							default:
								break;
							}
						}
             		}
             		catch (Exception e) {
             			e.printStackTrace();
             			System.out.println("XML��������");
             	    	throw e;
             		}
                 }
                 
//                 if("res/drawable-ldpi/icon.png".equals(zipEntry.getName())){
//               	  OutputStream outputStream = new FileOutputStream(logoUrl);
//                     InputStream inputStream = zipFile.getInputStream(zipEntry); 
//                     while ((length = inputStream.read(b)) > 0)
//                        outputStream.write(b, 0, length);
//                 }
             }
          }
      } catch (IOException e) {
          // TODO Auto-generated catch block
//          e.printStackTrace();
    	  if(zipFile!=null){
    		  zipFile.close();
    	  }
    	  if(origin_file!=null){
    		  
    	  }
    	  System.out.println("unzip����");
    	  throw e;
      }
	  zipFile.close();
      return map;
   }
   
   private static String getAttributeValue(AXmlResourceParser parser,int index) {
		int type=parser.getAttributeValueType(index);
		int data=parser.getAttributeValueData(index);
		if (type==TypedValue.TYPE_STRING) {
			return parser.getAttributeValue(index);
		}
		if (type==TypedValue.TYPE_ATTRIBUTE) {
			return String.format("?%s%08X",getPackage(data),data);
		}
		if (type==TypedValue.TYPE_REFERENCE) {
			return String.format("@%s%08X",getPackage(data),data);
		}
		if (type==TypedValue.TYPE_FLOAT) {
			return String.valueOf(Float.intBitsToFloat(data));
		}
		if (type==TypedValue.TYPE_INT_HEX) {
			return String.format("0x%08X",data);
		}
		if (type==TypedValue.TYPE_INT_BOOLEAN) {
			return data!=0?"true":"false";
		}
		if (type==TypedValue.TYPE_DIMENSION) {
			return Float.toString(complexToFloat(data))+
				DIMENSION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type==TypedValue.TYPE_FRACTION) {
			return Float.toString(complexToFloat(data))+
				FRACTION_UNITS[data & TypedValue.COMPLEX_UNIT_MASK];
		}
		if (type>=TypedValue.TYPE_FIRST_COLOR_INT && type<=TypedValue.TYPE_LAST_COLOR_INT) {
			return String.format("#%08X",data);
		}
		if (type>=TypedValue.TYPE_FIRST_INT && type<=TypedValue.TYPE_LAST_INT) {
			return String.valueOf(data);
		}
		return String.format("<0x%X, type 0x%02X>",data,type);
	}
   
   private static String getPackage(int id) {
		if (id>>>24==1) {
			return "android:";
		}
		return "";
	}
   
   /////////////////////////////////// ILLEGAL STUFF, DONT LOOK :)
	public static float complexToFloat(int complex) {
		return (float)(complex & 0xFFFFFF00)*RADIX_MULTS[(complex>>4) & 3];
	}
	
	private static final float RADIX_MULTS[]={
		0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
	};
	private static final String DIMENSION_UNITS[]={
		"px","dip","sp","pt","in","mm","",""
	};
	private static final String FRACTION_UNITS[]={
		"%","%p","","","","","",""
	};
}

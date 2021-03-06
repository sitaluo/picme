package com.picme.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.picme.service.ImageService;

public class DownloadImgThread implements Runnable{
	
	Logger logger = LoggerFactory.getLogger(DownloadImgThread.class);
	// 获取临时素材(视频不能使用https协议)
	public static final String GET_TMP_MATERIAL = "https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s";
	public static final String THUMBNAIL_PRE = "picme_thumb_";
	private String realPath;
	private String mediaId;
	private Integer imgId;
	
	private ImageService imageService;
	
	@Override
	public void run() {
		String[] imgFileNameAndPath = this.fetchTmpFile(this.mediaId, this.realPath);
		if(imgFileNameAndPath != null && imgFileNameAndPath[0] != null && imgFileNameAndPath[0].equals("")){
			imgFileNameAndPath = this.fetchTmpFile(this.mediaId, this.realPath);
		}
		
		if(imgFileNameAndPath != null && imgFileNameAndPath[0] != null && !imgFileNameAndPath[0].equals("")){
			imageService.update(imgId, imgFileNameAndPath[0],imgFileNameAndPath[1]);
			logger.debug("从微信服务器下载图片：mediaId:"+mediaId);
			logger.debug("从微信服务器下载图片：imgId:"+imgId + ",dbFileName:" + imgFileNameAndPath[0] + "thumb:"+imgFileNameAndPath[1]);
		}else{
			logger.debug("从微信服务器下载图片 失败：mediaId:"+mediaId);
		}
	}
	
	private String[] fetchTmpFile(String media_id,String realPath) {
		String dbFileName = "";
		String thumbImgName = "";
		//String[] imgFileNameAndPath = new String[]{dbFileName,thumbImgName};
		try {
			//request.getSession().getServletContext().getRealPath("static/upload/imgs/");
			String path = realPath;
			String token = WeiXinApiUtils.getAccessToken();
			String url = null;
			// 视频是http协议
			url = String.format(GET_TMP_MATERIAL, token, media_id);
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.connect();
			if (conn.getResponseCode() == 200){
				logger.debug("连接成功200:");
			}else{
				logger.debug("error:" +conn.getResponseCode());
				//return imgFileNameAndPath;
			}
			BufferedInputStream bis = new BufferedInputStream(
					conn.getInputStream());
			String content_disposition = conn
					.getHeaderField("content-disposition");
			// 微信服务器生成的文件名称
			String file_name = "";
			if(content_disposition != null){
				String[] content_arr = content_disposition.split(";");
				if (content_arr.length == 2) {
					String tmp = content_arr[1];
					int index = tmp.indexOf("\"");
					file_name = tmp.substring(index + 1, tmp.length() - 1);
				}
			}else{
				file_name = media_id + ".jpg";
				logger.debug("微信图片获取后缀名失败,使用默认"); 
				//throw new Exception("微信图片获取后缀名失败");
				//return imgFileNameAndPath;
			}
			// 生成不同文件名称
			dbFileName = "static/upload/imgs/"+file_name;
			thumbImgName = "static/upload/imgs/"+THUMBNAIL_PRE+file_name;
			
			File fileTempDir = new File(path);
			if(!fileTempDir.exists()){
				fileTempDir.mkdirs();
       	 	}
			File file = new File(path + file_name);
			
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			byte[] buf = new byte[2048];
			int length = bis.read(buf);
			while (length != -1) {
				bos.write(buf, 0, length);
				length = bis.read(buf);
			}
			bos.close();
			bis.close();
			
			File fileTemp = new File(path,file_name);
			Integer degree = ImageUtils.getRotateDegree(fileTemp);
			if(degree != 0){
				//需要旋转
				String rotateFileName = "rotate_" + file_name;//
				dbFileName = "static/upload/imgs/" + rotateFileName;
				ImageUtils.rotate(degree, fileTemp, path, rotateFileName);
				ImageUtils.thumbnail(new File(path + rotateFileName), path +THUMBNAIL_PRE+file_name);
			}else{
				ImageUtils.thumbnail(new File(path + file_name), path +THUMBNAIL_PRE+file_name);
			}
			
            logger.debug("微信图片下载并上传到服务器成功:"+ dbFileName); 
		} catch (MalformedURLException e) {
			logger.debug("",e);
		} catch (IOException e) {
			logger.debug("",e);
		} catch (Exception e) {
			logger.debug("",e);
		}
		String[] imgFileNameAndPath = new String[]{dbFileName,thumbImgName};
		return imgFileNameAndPath;
	}
	
	public DownloadImgThread(String realPath, String mediaId, Integer imgId,
			ImageService imageService) {
		super();
		this.realPath = realPath;
		this.mediaId = mediaId;
		this.imgId = imgId;
		this.imageService = imageService;
	}

	public String getRealPath() {
		return realPath;
	}


	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}


	public String getMediaId() {
		return mediaId;
	}


	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}


	public Integer getImgId() {
		return imgId;
	}


	public void setImgId(Integer imgId) {
		this.imgId = imgId;
	}

	public ImageService getImageService() {
		return imageService;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

}

package com.svt;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.UploadContext;

import com.ccookat.ItemDTO;
import com.ccookat.ReviewDAO;
import com.ccookat.ReviewDTO;
//import com.join.CustomInfo;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.util.DBConn;
import com.util.FileManager;
import com.util.MyPage;

public class ReviewServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void forward(HttpServletRequest request, HttpServletResponse response, String url)
			throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher(url);

		rd.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF-8");
		
		Connection conn = DBConn.getconnection();
		ReviewDAO rdao = new ReviewDAO(conn);
		
		MyPage myPage = new MyPage();
		
		String cp = req.getContextPath();
		String uri = req.getRequestURI();
		
		String url;
		
		
		// 이미지 파일저장경로
		String root = getServletContext().getRealPath("/"); 
		String path = root + "pds" + File.separator + "imageFile";
		
		File f =  new File(path);
		if(!f.exists()) {
			f.mkdirs();}
		
		
		
		//리스트 뿌리기
		
		if(uri.indexOf("list.do") != -1) {
					
		//페이징 작업
		String pageNum = req.getParameter("pageNum");
		int itemNum =Integer.parseInt(req.getParameter("itemNum"));
			
		int currentPage = 1;
		if(pageNum!=null) 
			currentPage = Integer.parseInt(pageNum);
		
		//처음 전체 데이터 갯수 구하기
		int dataCount = rdao.getDataCount();
		//하나의 페이지에 보일 페이지 갯수
		int numPerPage = 5;
		int totalPage = myPage.getPageCount(numPerPage, dataCount);
		
		//삭제시 페이지수가 줄었을때 처리하는 방법 
		if (currentPage > totalPage) {
			currentPage = totalPage;
		}

		//데이터베이스에서 가져올 rownum의 시작과 끝 구하기
		int start = (currentPage-1)*numPerPage+1;
		int end = currentPage*numPerPage; 
						
		start = (currentPage-1) * numPerPage + 1;
		end = currentPage * numPerPage;
		
		String listUrl = cp + "/main/item/detail.do";
		String pageIndexList = myPage.pageIndexList(currentPage, totalPage, listUrl);
		//리스트 나오게 하기
		List<ReviewDTO> reviewlists = rdao.getLists(start, end, itemNum);
	
		
		String deletePath = cp + "/main/review/delete.do";
		String imagePath = cp + "/pds/imageFile";
		
		int reviewtotalArticle = rdao.getDataCount();
		
		req.setAttribute("itemNum", itemNum);
		req.setAttribute("imagePath", imagePath);
		req.setAttribute("pageNum", pageNum);
		req.setAttribute("deletePath", deletePath);
		req.setAttribute("reviewlists", reviewlists);
		req.setAttribute("pageIndexList", pageIndexList);
		req.setAttribute("totalPage", totalPage);
		req.setAttribute("reviewtotalArticle", reviewtotalArticle);
		req.setAttribute("currentPage", currentPage);
		req.setAttribute("reviewtotalArticle", reviewtotalArticle);
		
		url = "/item/detail.jsp?pageNum="+pageNum;
		forward(req, resp, url);
		
	//생성
	}else if(uri.indexOf("created.do") != -1) {
	/*		HttpSession session = req.getSession();
			CustomInfo info = //이렇게 받을준비해서
					(CustomInfo)session.getAttribute("customInfo");
					
					if(info==null) {
				//여기 작성하기
				forward(req, resp, url);
				return;

					*/
		int itemNum = Integer.parseInt(req.getParameter("itemNum"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));

		
		req.setAttribute("itemNum", itemNum);
		req.setAttribute("pageNum", pageNum);

		
		url = "/review/created.jsp";
		forward(req, resp, url);
	} else if (uri.indexOf("created_ok.do") != -1) {

		
		//파일업로드	
	String encType = "UTF-8";
		int maxSize = 10 * 1024 * 1024;
		
		
		MultipartRequest mr = 
				new MultipartRequest(req, path, maxSize, encType,
						new DefaultFileRenamePolicy()); 
		
		int pageNum = Integer.parseInt(mr.getParameter("pageNum"));
		int itemNum = Integer.parseInt(mr.getParameter("itemNum"));
	
		ReviewDTO rdto = new ReviewDTO();
		ItemDTO idto = new ItemDTO();
		int maxNum = rdao.getMaxNum();
	
		/*Enumeration enums = req.getParameterNames();

		while(enums.hasMoreElements()) {
		 
			String key = (String)enums.nextElement();
		  String value = req.getParameter(key);
	  System.out.println("이넘: " + key + " : " + value+"<br>");
	 }*/
		
		rdto.setReviewNum(maxNum+1);
		rdto.setCustomerId(mr.getParameter("customerId"));
		rdto.setReviewTitle(mr.getParameter("reviewTitle"));
		rdto.setReviewContent(mr.getParameter("reviewContent"));
		rdto.setReviewImage(mr.getFilesystemName("upload"));
		rdto.setItemNum(Integer.parseInt(mr.getParameter("itemNum")));
		rdto.setReviewCreated(mr.getParameter("reviewCreated"));
		
		rdao.insertData(rdto);
		
		url = cp + "/main/item/detail.do?pageNum=" + pageNum + "&itemNum="+ itemNum ; // 리다이렉트는 가상의주소로
		resp.sendRedirect(url);
		//수정
	} else if (uri.indexOf("updated.do") != -1) {
		int reviewNum =Integer.parseInt(req.getParameter("reviewNum"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int itemNum = Integer.parseInt(req.getParameter("itemNum"));
		
			ReviewDTO rdto = rdao.getReadData(reviewNum);
			
			
			if (rdto == null) {
				url = cp + "main/item/detail.do?pageNum="+pageNum+"&itemNum="+rdto.getItemNum();
				resp.sendRedirect(url);}
			
			
			req.setAttribute("rdto", rdto);
			req.setAttribute("pageNum", pageNum);
			req.setAttribute("itemNum", itemNum);
			req.setAttribute("reviewNum", reviewNum);
			

			url = "/review/updated.jsp";
			forward(req, resp, url);
	
	
	} else if (uri.indexOf("updated_ok.do") != -1) {
		
		String encType = "UTF-8";
		int maxSize = 10 * 1024 * 1024;
		
		
		MultipartRequest mr = 
				new MultipartRequest(req, path, maxSize, encType,
						new DefaultFileRenamePolicy()); 
		
		int reviewNum =Integer.parseInt(mr.getParameter("reviewNum"));
		int pageNum = Integer.parseInt(mr.getParameter("pageNum"));
		
	
		ReviewDTO rdto = new ReviewDTO();
		
		rdto.setReviewNum(Integer.parseInt(mr.getParameter("reviewNum")));
		rdto.setReviewTitle(mr.getParameter("reviewTitle"));
		rdto.setReviewContent(mr.getParameter("reviewContent"));
		
		if(rdto.getReviewImage()!=null) {
			FileManager.doFileDelete(rdto.getReviewImage(), path);
		}
		rdto.setReviewImage(mr.getFilesystemName("upload"));
		
		rdao.updateData(rdto);
		
		url = cp + "/main/item/list.do?pageNum="+pageNum ;
		resp.sendRedirect(url);
	} else if (uri.indexOf("deleted.do") != -1) {
		
		int reviewNum = Integer.parseInt(req.getParameter("reviewNum"));
		int pageNum = Integer.parseInt(req.getParameter("pageNum"));
		int itemNum = Integer.parseInt(req.getParameter("itemNum"));
		
		ReviewDTO rdto = rdao.getReadData(reviewNum);
		
		FileManager.doFileDelete(rdto.getReviewImage(), path);
		
		rdao.deleteData(reviewNum);
		
		url = cp + "/main/item/list.do?pageNum="+pageNum+"&itemNum"+itemNum;
		resp.sendRedirect(url);
	}
		
}
	}

	

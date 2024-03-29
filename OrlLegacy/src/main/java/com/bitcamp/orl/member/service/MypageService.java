package com.bitcamp.orl.member.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServletRequest;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bitcamp.orl.member.dao.Dao;
import com.bitcamp.orl.member.domain.Member;
import com.bitcamp.orl.member.domain.MemberEditRequest;
import com.bitcamp.orl.member.util.AES256Util;

@Service
public class MypageService {
   final String PROFILE_URI ="/images/member/profile";

   private Dao dao;
   
   @Autowired
   private SqlSessionTemplate template;
   
 //암호화처리
 	@Autowired
 	private AES256Util aes256Util; 
 	

   public Member getMemberSelectByIdx(int memberIdx){

      Member member = null;
      dao = template.getMapper(Dao.class);

      member = dao.selectByIdx(memberIdx);

      return member;
   }

   public int editMember(
         HttpServletRequest request,
         Member member,
         MemberEditRequest memberEditRequest) {

      File newFile = null;
      int resultCnt = 0;

      try {
         Member editMember = member;

         if (memberEditRequest.getMemberPhoto() != null && !memberEditRequest.getMemberPhoto().isEmpty()) {
        	 if(selectThatFile(member.getMemberIdx(),request) != null) {
         		selectThatFile(member.getMemberIdx(),request).delete();
         	}
            newFile = saveProfileFile(request,memberEditRequest.getMemberPhoto());
            editMember.setMemberProfile(newFile.getName());
         }
         editMember.setMemberName(memberEditRequest.getMemberName());
         editMember.setMemberEmail(memberEditRequest.getMemberEmail());
         editMember.setMemberNickname(memberEditRequest.getMemberNickname());
         editMember.setMemberBirth(memberEditRequest.getBirth());

         dao = template.getMapper(Dao.class);
         resultCnt = dao.updateMember(member);

      } catch (IllegalStateException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
         if(newFile != null & newFile.exists()) {
            newFile.delete();
            System.out.println("파일 삭제");
         }
      }
      return resultCnt;
   }

   public int editPw(String oldPw,String newPw,String newPw2,Member member) {
	   int resultCnt = 0;
	   try {
		oldPw= aes256Util.encrypt(oldPw);
		if( member.getMemberPw().equals(oldPw)) {
			
			if(newPw.equals(newPw2)) {
				member.setMemberPw(aes256Util.encrypt(newPw));
				dao=template.getMapper(Dao.class);
				resultCnt=dao.updateMember(member);
			}
		}
	} catch (UnsupportedEncodingException | GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   
	   
	   return resultCnt;
   }
   
   
   
   public File saveProfileFile(
         HttpServletRequest request,
         MultipartFile file) {

      String path = request.getSession().getServletContext().getRealPath(PROFILE_URI);
      File newDir = new File(path);

      if(!newDir.exists()) {
         newDir.mkdir();
         System.out.println("저장 폴더를 생성했습니다.");
      }

      String newFileName = System.currentTimeMillis() + file.getOriginalFilename();
      File newFile = new File(newDir, newFileName);

      try {
         file.transferTo(newFile);
      } catch (IllegalStateException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return newFile;
   }
   
   public File selectThatFile(int memberIdx, HttpServletRequest request) {
	   	dao = template.getMapper(Dao.class);
	   	Member member = dao.selectByIdx(memberIdx);
	   	String fileName = member.getMemberProfile();
	   	String dirpath = request.getSession().getServletContext().getRealPath(PROFILE_URI);
	   	
	   	File thatFile = null;
    	if(!fileName.equals("default.jpg")) {
    		thatFile = new File(dirpath, fileName);
    	}
	   	
	   	return new File(dirpath, fileName);
	   }
   
}
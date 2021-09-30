package com.bitcamp.orl.feed.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bitcamp.orl.crew.domain.Crew;
import com.bitcamp.orl.feed.domain.FeedCreateRequest;
import com.bitcamp.orl.feed.domain.FeedGallery;
import com.bitcamp.orl.feed.domain.FeedLikeGallery;
import com.bitcamp.orl.feed.service.CreateFeedService;
import com.bitcamp.orl.feed.service.FeedGalleryService;
import com.bitcamp.orl.feed.service.UserFeedService;
import com.bitcamp.orl.member.domain.Member;
import com.bitcamp.orl.member.domain.MemberDto;

@Controller
public class UserFeedController {

   @Autowired
   private UserFeedService feedService;

   @Autowired
   private FeedGalleryService galleryService;

   @Autowired
   private CreateFeedService createService;

   // 피드 보여주기(남피드, 내피드 같은 요청)
   @RequestMapping("/feed/userfeed/{memberIdx}")
   public String getUserFeed(
         HttpServletRequest request, 
         @PathVariable("memberIdx") int memberIdx,
         Model model) {
   
      String feedpath = null;

      System.out.println("요청 받음");

      // 세션에 저장된 나의 memberIdx
      int myIdx = ((MemberDto) request.getSession().getAttribute("memberVo")).getMemberIdx();

      // memberIdx에 해당하는 member객체 가져오기
      Member member = feedService.getOneMember(memberIdx);

      if (member != null) {
         // 1) 팔로워 수 구하기
         int followerCount = feedService.getFollowerCount(memberIdx);

         // 2) 팔로잉 수 구하기
         int followingCount = feedService.getFollowingCount(memberIdx);

         // 3) 게시물 수 구하기
         int feedCount = feedService.getFeedCount(memberIdx);

         // 4 )팔로우 관계 --> 팔로우 시작하기, 팔로우 그만하기 버튼
         // 팔로우 하고 있으면 1 아니면 0
         int followRelation = feedService.getfollowRelation(myIdx, memberIdx);
         // System.out.println(followRelation);

         // 5) 사진 갤러리 기본 정렬 보여주기
         List<FeedGallery> feedGallery = galleryService.getFeedGallery(memberIdx);

         // 6) 사진 갤러리 좋아요 정렬 보여주기
         List<FeedLikeGallery> feedLikeGallery = galleryService.getFeedLikeGallery(memberIdx);

         // 7) 내가 가입한 크루 보여주기 
         List<Crew> myCrewList = galleryService.getMyCrewList(memberIdx);

         // model에 객체 전달
         model.addAttribute("member", member);
         model.addAttribute("followerCount", followerCount);
         model.addAttribute("followingCount", followingCount);
         model.addAttribute("feedCount", feedCount);
         model.addAttribute("followRelation", followRelation);
         model.addAttribute("feedGallery", feedGallery);
         model.addAttribute("feedLikeGallery", feedLikeGallery);
         model.addAttribute("myCrewList", myCrewList);

         feedpath = "feed/userFeed";

      } else {
         // member 객체가 없으면 nullpointerException 발생시키기
         throw new NullPointerException();
      }

      return feedpath;
   }

   // 피드 올리기 후에 !
   @RequestMapping(value = "/feed/userfeed/{memberIdx}", method = RequestMethod.POST)
   public String upload(
         @ModelAttribute("feedrequest") FeedCreateRequest feedrequest,
         HttpServletRequest request,
         @PathVariable("memberIdx") int memberIdx
         ) throws IllegalStateException, IOException {

      // 사진 올리기
      createService.insert(feedrequest, request);

      // 0915 수정 insert중복되는 거 막기 위해 get방식으로 redirect 시키기
      return "redirect:/feed/userfeed/" + memberIdx;
   }



   @ExceptionHandler(NullPointerException.class) 
   public String handleNullPointerException(NullPointerException e) {

      //에러 내용 출력 e.printStackTrace();

      return "error/pageNotFound"; 
   }


}
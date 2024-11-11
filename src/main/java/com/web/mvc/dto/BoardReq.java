package com.web.mvc.dto;


import com.web.mvc.domain.Board;
import com.web.mvc.domain.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BoardReq {
    private String title;//제목
    private String content;//내용
    private Long memberNo;//작성자

    public Board toBoard(BoardReq boardReq){

        return Board.builder()
                .title(boardReq.getTitle())
                .content(boardReq.getContent())
                .member(Member.builder().memberNo(boardReq.memberNo).build())
                .build();
    }

}

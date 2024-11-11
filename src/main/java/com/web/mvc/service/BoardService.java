package com.web.mvc.service;


import com.web.mvc.domain.Board;
import com.web.mvc.dto.BoardReq;
import com.web.mvc.dto.BoardRes;
import com.web.mvc.exception.BoardSearchNotException;
import com.web.mvc.exception.DMLException;

import java.util.List;

public interface BoardService {
    Board findBoard(Long id) throws BoardSearchNotException;

    List<BoardRes> findAllBoard()throws BoardSearchNotException ;
     Board addBoard(BoardReq board);

    Board updateBoard(Long id,BoardReq board)throws DMLException; ;

     String deleteBoard(Long id);
}

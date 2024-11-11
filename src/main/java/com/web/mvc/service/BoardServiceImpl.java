package com.web.mvc.service;

import com.web.mvc.domain.Board;
import com.web.mvc.dto.BoardReq;
import com.web.mvc.dto.BoardRes;
import com.web.mvc.exception.BoardSearchNotException;
import com.web.mvc.exception.DMLException;
import com.web.mvc.exception.ErrorCode;
import com.web.mvc.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService{
	private final BoardRepository boardRepository;

	@Transactional
	public Board addBoard(BoardReq boardReq) {
		System.out.println("boardReq = " + boardReq);

		Board  board = boardReq.toBoard(boardReq); // dto 를 엔티티로 변환
		System.out.println("board = " + board.toString());
		return boardRepository.save(board);
	}
	

	@Transactional(readOnly = true)
	public Board findBoard(Long id) throws BoardSearchNotException {
		return boardRepository.findById(id)
				.orElseThrow(()->new BoardSearchNotException(ErrorCode.NOTFOUND_NO));

	}
	
	@Transactional(readOnly = true)
	//public List<Board> findAllBoard() throws BoardSearchNotException {
	public List<BoardRes> findAllBoard() throws BoardSearchNotException {
		//List<Board> list = boardRepository.findAll();
		List<Board> list = boardRepository.join();// findAll 쓰면 n+1 문제가 생겨서 fetch join 이용

		//Lis<Board>
		System.out.println("--------------------------------------------");
		if(list ==null || list.isEmpty())
			throw new BoardSearchNotException(ErrorCode.NOTFOUND_NO);

		return list.stream().map(BoardRes::new).collect(Collectors.toList());
		/*
		mapper 를 사용하지 않고 Board 를 BoardRes 로 변환한다
		List<Board> -> List<BoardRes>
		 */
	}
	
	@Transactional
	public Board updateBoard(Long id,BoardReq board)  throws DMLException {

		Board boardEntity = boardRepository.findById(id)
				      .orElseThrow(()->new DMLException(ErrorCode.UPDATE_FAILED));
		
		boardEntity.setTitle(board.getTitle());
		boardEntity.setContent(board.getContent());
		return boardEntity;
	}
	
	@Transactional
	public String deleteBoard(Long id) {
		System.out.println("id = " + id);
		boardRepository.findById(id).orElseThrow(()->
				new DMLException(ErrorCode.DELETE_FAILED));

		boardRepository.deleteById(id);

		return "ok";
	}
}

package com.postcoment.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.postcoment.exception.ResourceNotFoundException;
import com.postcoment.model.Comment;
import com.postcoment.repository.CommentRepository;
import com.postcoment.repository.PostRepository;

@RestController
public class CommentController {
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private PostRepository postRepository;
	
	@GetMapping("/posts/{postId}/comments")
	public Page<Comment> getAllCommentsByPostId(@PathVariable (value = "postId") Long postId, Pageable pageable){
		return commentRepository.findByPostId(postId, pageable);
	}
	
	@PostMapping("/posts/{postId}/comments")
	public Comment createComment (@PathVariable (value = "postId") Long postId,
			@Valid @RequestBody Comment comment) {
		return postRepository.findById(postId).map(post -> {
			comment.setPost(post);
			comment.setText(comment.getText());
			return commentRepository.save(comment);
		}).orElseThrow(() -> new ResourceNotFoundException("PostId " + postId + " not found"));
	}
	
	@PutMapping("/posts/{postId}/comments/{commentid}")
	public Comment updateComment(@PathVariable (value = "postId") Long postId,
			@PathVariable (value = "commentId") Long commentId, @RequestBody Comment commentReq) {
		if (!postRepository.existsById(postId)) {
			throw new ResourceNotFoundException("PostId " + postId + " not found");
		}
		
		return commentRepository.findById(commentId).map(comment -> {
			comment.setText(commentReq.getText());
			return commentRepository.save(comment);
		}).orElseThrow(() -> new ResourceNotFoundException("Comment Id " + commentId + " not found"));
	}
	
	public ResponseEntity<?> deleteComment(@PathVariable (value = "postId") Long postId,
			@PathVariable (value = "commentId") Long commentId){
		return commentRepository.findByIdAndPostId(commentId, postId).map(comment ->{
			commentRepository.delete(comment);
			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId + " and postId " + postId));
	}
}

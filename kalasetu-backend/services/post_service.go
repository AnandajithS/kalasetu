package services

import (
	"context"
	"errors"
	"kalasetu/models"
	"kalasetu/repos"
)

var (
	ErrPostNotFound     = errors.New("post not found")
	ErrCommentNotFound  = errors.New("comment not found")
	ErrPostForbidden    = errors.New("you are not the owner of this post")
	ErrCommentForbidden = errors.New("you are not the owner of this comment")
)

type PostService interface {
	Create(ctx context.Context, userID int, input models.CreatePostInput) (*models.Post, error)
	GetByID(ctx context.Context, id int) (*models.Post, error)
	List(ctx context.Context) ([]models.Post, error)
	Update(ctx context.Context, userID, id int, input models.UpdatePostInput) (*models.Post, error)
	Delete(ctx context.Context, userID, id int) error
}

type postService struct {
	postRepo repos.PostRepository
}

func NewPostService(postRepo repos.PostRepository) PostService {
	return &postService{postRepo: postRepo}
}

func (s *postService) Create(ctx context.Context, userID int, input models.CreatePostInput) (*models.Post, error) {
	post, err := s.postRepo.Create(ctx, &models.Post{
		UserID:     userID,
		Content:    input.Content,
		MediaType:  input.MediaType,
		MediaURI:   input.MediaURI,
		CategoryID: input.CategoryID,
	})
	if err != nil {
		return nil, err
	}
	// Refetch so the response includes user/category names and counts.
	return s.postRepo.FindByID(ctx, post.ID)
}

func (s *postService) GetByID(ctx context.Context, id int) (*models.Post, error) {
	post, err := s.postRepo.FindByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if post == nil {
		return nil, ErrPostNotFound
	}
	return post, nil
}

func (s *postService) List(ctx context.Context) ([]models.Post, error) {
	return s.postRepo.List(ctx)
}

func (s *postService) Update(ctx context.Context, userID, id int, input models.UpdatePostInput) (*models.Post, error) {
	post, err := s.postRepo.FindByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if post == nil {
		return nil, ErrPostNotFound
	}
	if post.UserID != userID {
		return nil, ErrPostForbidden
	}
	if err := s.postRepo.Update(ctx, id, input); err != nil {
		return nil, err
	}
	return s.postRepo.FindByID(ctx, id)
}

func (s *postService) Delete(ctx context.Context, userID, id int) error {
	post, err := s.postRepo.FindByID(ctx, id)
	if err != nil {
		return err
	}
	if post == nil {
		return ErrPostNotFound
	}
	if post.UserID != userID {
		return ErrPostForbidden
	}
	return s.postRepo.Delete(ctx, id)
}

type CommentService interface {
	Create(ctx context.Context, userID int, input models.CreateCommentInput) (*models.Comment, error)
	ListByPost(ctx context.Context, postID int) ([]models.Comment, error)
	Update(ctx context.Context, userID, id int, content string) (*models.Comment, error)
	Delete(ctx context.Context, userID, id int) error
}

type commentService struct {
	commentRepo repos.CommentRepository
}

func NewCommentService(commentRepo repos.CommentRepository) CommentService {
	return &commentService{commentRepo: commentRepo}
}

func (s *commentService) Create(ctx context.Context, userID int, input models.CreateCommentInput) (*models.Comment, error) {
	comment, err := s.commentRepo.Create(ctx, &models.Comment{
		PostID:  input.PostID,
		UserID:  userID,
		Content: input.Content,
	})
	if err != nil {
		return nil, err
	}
	// Refetch so the response includes the commenter name.
	return s.commentRepo.FindByID(ctx, comment.ID)
}

func (s *commentService) ListByPost(ctx context.Context, postID int) ([]models.Comment, error) {
	return s.commentRepo.ListByPost(ctx, postID)
}

func (s *commentService) Update(ctx context.Context, userID, id int, content string) (*models.Comment, error) {
	comment, err := s.commentRepo.FindByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if comment == nil {
		return nil, ErrCommentNotFound
	}
	if comment.UserID != userID {
		return nil, ErrCommentForbidden
	}
	if err := s.commentRepo.Update(ctx, id, content); err != nil {
		return nil, err
	}
	return s.commentRepo.FindByID(ctx, id)
}

func (s *commentService) Delete(ctx context.Context, userID, id int) error {
	comment, err := s.commentRepo.FindByID(ctx, id)
	if err != nil {
		return err
	}
	if comment == nil {
		return ErrCommentNotFound
	}
	if comment.UserID != userID {
		return ErrCommentForbidden
	}
	return s.commentRepo.Delete(ctx, id)
}

type LikeService interface {
	Like(ctx context.Context, userID, postID int) error
	Unlike(ctx context.Context, userID, postID int) error
	ListByPost(ctx context.Context, postID int) ([]models.Author, error)
}

type likeService struct {
	likeRepo repos.LikeRepository
}

func NewLikeService(likeRepo repos.LikeRepository) LikeService {
	return &likeService{likeRepo: likeRepo}
}

func (s *likeService) Like(ctx context.Context, userID, postID int) error {
	return s.likeRepo.Like(ctx, userID, postID)
}

func (s *likeService) Unlike(ctx context.Context, userID, postID int) error {
	return s.likeRepo.Unlike(ctx, userID, postID)
}

func (s *likeService) ListByPost(ctx context.Context, postID int) ([]models.Author, error) {
	return s.likeRepo.ListUsersByPost(ctx, postID)
}

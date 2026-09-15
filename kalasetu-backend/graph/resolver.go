package graph

// This file will not be regenerated automatically.
//
// It serves as dependency injection for your app, add any dependencies you require
// here.

import "kalasetu/services"

type Resolver struct {
	eventService       services.EventService
	applicationService services.ApplicationService
	postService        services.PostService
	commentService     services.CommentService
	likeService        services.LikeService
	userService        services.UserService
}

func NewResolver(
	eventService services.EventService,
	applicationService services.ApplicationService,
	postService services.PostService,
	commentService services.CommentService,
	likeService services.LikeService,
	userService services.UserService,
) *Resolver {
	return &Resolver{
		eventService:       eventService,
		applicationService: applicationService,
		postService:        postService,
		commentService:     commentService,
		likeService:        likeService,
		userService:        userService,
	}
}

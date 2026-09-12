package graph

import (
	"context"
	"errors"
	"fmt"
	"kalasetu/graph/model"
	"kalasetu/middlewares"
	"kalasetu/models"
	"strconv"
	"time"
)

// requireUser returns the authenticated user id from the request context,
// injected by the OptionalJWT middleware, or a GraphQL auth error.
func requireUser(ctx context.Context) (int, error) {
	userID, err := middlewares.GetUserIDFromContext(ctx)
	if err != nil {
		return 0, errors.New("authentication required")
	}
	return userID, nil
}

func parseEventID(id string) (int, error) {
	parsed, err := strconv.Atoi(id)
	if err != nil {
		return 0, fmt.Errorf("invalid event id: %s", id)
	}
	return parsed, nil
}

func parsePostID(id string) (int, error) {
	parsed, err := strconv.Atoi(id)
	if err != nil {
		return 0, fmt.Errorf("invalid post id: %s", id)
	}
	return parsed, nil
}

func parseCommentID(id string) (int, error) {
	parsed, err := strconv.Atoi(id)
	if err != nil {
		return 0, fmt.Errorf("invalid comment id: %s", id)
	}
	return parsed, nil
}

func parseOptionalID(id *string) (*int, error) {
	if id == nil {
		return nil, nil
	}
	parsed, err := strconv.Atoi(*id)
	if err != nil {
		return nil, fmt.Errorf("invalid id: %s", *id)
	}
	return &parsed, nil
}

func toGraphEvent(e *models.Event) *model.Event {
	if e == nil {
		return nil
	}
	var hostID *string
	if e.HostID != nil {
		id := strconv.Itoa(*e.HostID)
		hostID = &id
	}
	var hostName *string
	if e.HostName != "" {
		hostName = &e.HostName
	}
	return &model.Event{
		ID:        strconv.Itoa(e.ID),
		Name:      e.Name,
		StartDate: e.StartDate,
		Duration:  e.Duration,
		HostID:    hostID,
		HostName:  hostName,
		CreatedAt: e.CreatedAt.Format(time.RFC3339),
	}
}

func toGraphEvents(events []models.Event) []*model.Event {
	result := make([]*model.Event, 0, len(events))
	for i := range events {
		e := events[i]
		result = append(result, toGraphEvent(&e))
	}
	return result
}

func toGraphApplication(app *models.Application) *model.Application {
	if app == nil {
		return nil
	}
	return &model.Application{
		ID:            strconv.Itoa(app.ID),
		OpportunityID: strconv.Itoa(app.OpportunityID),
		ApplierID:     strconv.Itoa(app.ApplierID),
		ResumeURL:     app.ResumeURL,
		Status:        app.Status,
		CreatedAt:     app.CreatedAt.Format(time.RFC3339),
	}
}

func toGraphApplications(apps []models.Application) []*model.Application {
	result := make([]*model.Application, 0, len(apps))
	for i := range apps {
		a := apps[i]
		result = append(result, toGraphApplication(&a))
	}
	return result
}

func toGraphPost(p *models.Post) *model.Post {
	if p == nil {
		return nil
	}
	var categoryID *string
	if p.CategoryID != nil {
		id := strconv.Itoa(*p.CategoryID)
		categoryID = &id
	}
	var categoryName *string
	if p.CategoryName != "" {
		categoryName = &p.CategoryName
	}
	return &model.Post{
		ID:           strconv.Itoa(p.ID),
		UserID:       strconv.Itoa(p.UserID),
		UserName:     p.UserName,
		Content:      p.Content,
		MediaType:    p.MediaType,
		MediaURI:     p.MediaURI,
		CategoryID:   categoryID,
		CategoryName: categoryName,
		LikeCount:    int32(p.LikeCount),
		CommentCount: int32(p.CommentCount),
		CreatedAt:    p.CreatedAt.Format(time.RFC3339),
	}
}

func toGraphPosts(posts []models.Post) []*model.Post {
	result := make([]*model.Post, 0, len(posts))
	for i := range posts {
		p := posts[i]
		result = append(result, toGraphPost(&p))
	}
	return result
}

func toGraphAuthor(a *models.Author) *model.Author {
	if a == nil {
		return nil
	}
	return &model.Author{
		ID:   strconv.Itoa(a.ID),
		Name: a.Name,
	}
}

func toGraphAuthors(authors []models.Author) []*model.Author {
	result := make([]*model.Author, 0, len(authors))
	for i := range authors {
		a := authors[i]
		result = append(result, toGraphAuthor(&a))
	}
	return result
}

func toGraphComment(c *models.Comment) *model.Comment {
	if c == nil {
		return nil
	}
	return &model.Comment{
		ID:        strconv.Itoa(c.ID),
		PostID:    strconv.Itoa(c.PostID),
		UserID:    strconv.Itoa(c.UserID),
		UserName:  c.UserName,
		Content:   c.Content,
		CreatedAt: c.CreatedAt.Format(time.RFC3339),
	}
}

func toGraphComments(comments []models.Comment) []*model.Comment {
	result := make([]*model.Comment, 0, len(comments))
	for i := range comments {
		c := comments[i]
		result = append(result, toGraphComment(&c))
	}
	return result
}

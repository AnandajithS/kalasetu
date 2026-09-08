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

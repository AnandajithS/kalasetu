package models

import "time"

type Event struct {
	ID        int        `json:"id"`
	Name      string     `json:"name"`
	StartDate string     `json:"start_date"` // "2006-01-02"
	Duration  string     `json:"duration"`   // Postgres interval literal, e.g. "2 days"
	HostID    *int       `json:"host_id"`
	HostName  string     `json:"host_name,omitempty"`
	CreatedAt time.Time  `json:"created_at"`
}

type CreateEventInput struct {
	Name      string `json:"name" binding:"required"`
	StartDate string `json:"start_date" binding:"required"`
	Duration  string `json:"duration" binding:"required"`
}

type UpdateEventInput struct {
	Name      *string `json:"name"`
	StartDate *string `json:"start_date"`
	Duration  *string `json:"duration"`
}

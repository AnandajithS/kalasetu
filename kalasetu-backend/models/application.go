package models

import "time"

type Application struct {
	ID            int       `json:"id"`
	OpportunityID int       `json:"opportunity_id"`
	ApplierID     int       `json:"applier_id"`
	ResumeURL     string    `json:"resume_url"`
	Status        string    `json:"status"`
	CreatedAt     time.Time `json:"created_at"`
}

type CreateApplicationInput struct {
	OpportunityID int
	ResumeURL     string
}

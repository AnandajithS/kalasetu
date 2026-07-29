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

type CreateApplicationRequest struct {
	OpportunityID int    `json:"opportunity_id" binding:"required"`
	ResumeURL     string `json:"resume_url" binding:"required"`
}

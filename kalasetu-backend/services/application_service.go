package services

import (
	"context"

	"kalasetu/models"
	"kalasetu/repos"
)

type ApplicationService interface {
	Create(ctx context.Context, userID int, req models.CreateApplicationRequest) error
}

type applicationService struct {
	applicationRepo repos.ApplicationRepository
}

func NewApplicationService(applicationRepo repos.ApplicationRepository) ApplicationService {
	return &applicationService{
		applicationRepo: applicationRepo,
	}
}

func (s *applicationService) Create(ctx context.Context, userID int, req models.CreateApplicationRequest) error {
	app := &models.Application{
		OpportunityID: req.OpportunityID,
		ApplierID:     userID,
		ResumeURL:     req.ResumeURL,
	}
	return s.applicationRepo.Create(ctx, app)
}

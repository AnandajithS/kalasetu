package services

import (
	"context"

	"kalasetu/models"
	"kalasetu/repos"
)

type ApplicationService interface {
	Create(ctx context.Context, userID int, input models.CreateApplicationInput) (*models.Application, error)
}

type applicationService struct {
	applicationRepo repos.ApplicationRepository
}

func NewApplicationService(applicationRepo repos.ApplicationRepository) ApplicationService {
	return &applicationService{
		applicationRepo: applicationRepo,
	}
}

func (s *applicationService) Create(
	ctx context.Context,
	userID int,
	input models.CreateApplicationInput,
) (*models.Application, error) {

	app := &models.Application{
		OpportunityID: input.OpportunityID,
		ApplierID:     userID,
		ResumeURL:     input.ResumeURL,
	}

	err := s.applicationRepo.Create(ctx, app)
	if err != nil {
		return nil, err
	}

	return app, nil
}

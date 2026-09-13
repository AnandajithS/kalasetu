package services

import (
	"context"

	"kalasetu/models"
	"kalasetu/repos"
)

type ApplicationService interface {
	Create(ctx context.Context, userID int, input models.CreateApplicationInput) (*models.Application, error)
	FindByID(ctx context.Context, id int) (*models.Application, error)
	UpdateStatus(ctx context.Context, id int, status string) error
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

func (s *applicationService) FindByID(ctx context.Context, id int) (*models.Application, error) {

	app, err := s.applicationRepo.FindByID(ctx, id)

	if err != nil {
		return nil, err
	}

	return app, nil

}

func (s *applicationService) UpdateStatus(ctx context.Context, id int, status string) error {
	err := s.applicationRepo.UpdateStatus(ctx, id, status)

	if err != nil {
		return err
	}

	return nil
}

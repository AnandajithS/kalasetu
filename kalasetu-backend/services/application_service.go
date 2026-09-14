package services

import (
	"context"
	"errors"
	"strings"

	"kalasetu/models"
	"kalasetu/repos"
)

var (
	ErrApplicationNotFound = errors.New("application not found")
	ErrAlreadyApplied       = errors.New("you have already applied to this opportunity")
	ErrResumeRequired      = errors.New("resume_url is required")
	ErrInvalidStatus       = errors.New("status must be pending, accepted, or rejected")
	ErrApplicationForbidden = errors.New("you do not have permission to access or update this application")
)

type ApplicationService interface {
	Create(ctx context.Context, userID int, input models.CreateApplicationInput) (*models.Application, error)
	FindByID(ctx context.Context, userID int, id int) (*models.Application, error)
	ListByApplier(ctx context.Context, applierID int) ([]models.Application, error)
	ListByOpportunity(ctx context.Context, userID int, opportunityID int) ([]models.Application, error)
	UpdateStatus(ctx context.Context, userID int, id int, status string) error
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
	if strings.TrimSpace(input.ResumeURL) == "" {
		return nil, ErrResumeRequired
	}

	// Check if already applied
	existing, err := s.applicationRepo.FindByOpportunityAndApplier(ctx, input.OpportunityID, userID)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return nil, ErrAlreadyApplied
	}

	app := &models.Application{
		OpportunityID: input.OpportunityID,
		ApplierID:     userID,
		ResumeURL:     strings.TrimSpace(input.ResumeURL),
	}

	err = s.applicationRepo.Create(ctx, app)
	if err != nil {
		return nil, err
	}

	return app, nil
}

func (s *applicationService) FindByID(ctx context.Context, userID int, id int) (*models.Application, error) {
	app, err := s.applicationRepo.FindByID(ctx, id)
	if err != nil {
		return nil, err
	}
	if app == nil {
		return nil, ErrApplicationNotFound
	}

	// Authorization check: User must be applier or opportunity host
	hostID, err := s.applicationRepo.GetOpportunityHostID(ctx, app.OpportunityID)
	if err == nil && hostID != userID && app.ApplierID != userID {
		return nil, ErrApplicationForbidden
	}

	return app, nil
}

func (s *applicationService) ListByApplier(ctx context.Context, applierID int) ([]models.Application, error) {
	return s.applicationRepo.ListByApplier(ctx, applierID)
}

func (s *applicationService) ListByOpportunity(ctx context.Context, userID int, opportunityID int) ([]models.Application, error) {
	// Authorization check: Only opportunity host can list applications for their opportunity
	hostID, err := s.applicationRepo.GetOpportunityHostID(ctx, opportunityID)
	if err != nil {
		return nil, err
	}
	if hostID != userID {
		return nil, ErrApplicationForbidden
	}

	return s.applicationRepo.ListByOpportunity(ctx, opportunityID)
}

func (s *applicationService) UpdateStatus(ctx context.Context, userID int, id int, status string) error {
	normalizedStatus := strings.ToLower(strings.TrimSpace(status))
	if normalizedStatus != "pending" && normalizedStatus != "accepted" && normalizedStatus != "rejected" {
		return ErrInvalidStatus
	}

	app, err := s.applicationRepo.FindByID(ctx, id)
	if err != nil {
		return err
	}
	if app == nil {
		return ErrApplicationNotFound
	}

	// Authorization check: Only opportunity host can update application status
	hostID, err := s.applicationRepo.GetOpportunityHostID(ctx, app.OpportunityID)
	if err != nil {
		return err
	}
	if hostID != userID {
		return ErrApplicationForbidden
	}

	return s.applicationRepo.UpdateStatus(ctx, id, normalizedStatus)
}

package services_test

import (
	"context"
	"testing"

	"kalasetu/models"
	"kalasetu/services"
)

type mockAppRepo struct {
	apps   map[int]*models.Application
	hosts  map[int]int // opportunityID -> hostID
	seq    int
}

func newMockAppRepo() *mockAppRepo {
	return &mockAppRepo{
		apps:  make(map[int]*models.Application),
		hosts: map[int]int{100: 42}, // Opportunity 100 hosted by User 42
	}
}

func (m *mockAppRepo) Create(ctx context.Context, app *models.Application) error {
	m.seq++
	app.ID = m.seq
	app.Status = "pending"
	m.apps[app.ID] = app
	return nil
}

func (m *mockAppRepo) FindByID(ctx context.Context, id int) (*models.Application, error) {
	app, ok := m.apps[id]
	if !ok {
		return nil, nil
	}
	return app, nil
}

func (m *mockAppRepo) FindByOpportunityAndApplier(ctx context.Context, opportunityID, applierID int) (*models.Application, error) {
	for _, app := range m.apps {
		if app.OpportunityID == opportunityID && app.ApplierID == applierID {
			return app, nil
		}
	}
	return nil, nil
}

func (m *mockAppRepo) ListByApplier(ctx context.Context, applierID int) ([]models.Application, error) {
	var list []models.Application
	for _, app := range m.apps {
		if app.ApplierID == applierID {
			list = append(list, *app)
		}
	}
	return list, nil
}

func (m *mockAppRepo) ListByOpportunity(ctx context.Context, opportunityID int) ([]models.Application, error) {
	var list []models.Application
	for _, app := range m.apps {
		if app.OpportunityID == opportunityID {
			list = append(list, *app)
		}
	}
	return list, nil
}

func (m *mockAppRepo) GetOpportunityHostID(ctx context.Context, opportunityID int) (int, error) {
	hostID, ok := m.hosts[opportunityID]
	if !ok {
		return 0, services.ErrApplicationNotFound
	}
	return hostID, nil
}

func (m *mockAppRepo) UpdateStatus(ctx context.Context, id int, status string) error {
	app, ok := m.apps[id]
	if !ok {
		return services.ErrApplicationNotFound
	}
	app.Status = status
	return nil
}

func TestApplicationService_CreateValidationAndDuplicate(t *testing.T) {
	repo := newMockAppRepo()
	svc := services.NewApplicationService(repo)
	ctx := context.Background()

	// Empty resume_url check
	_, err := svc.Create(ctx, 10, models.CreateApplicationInput{OpportunityID: 100, ResumeURL: "   "})
	if err != services.ErrResumeRequired {
		t.Fatalf("expected ErrResumeRequired, got %v", err)
	}

	// Valid creation
	app, err := svc.Create(ctx, 10, models.CreateApplicationInput{OpportunityID: 100, ResumeURL: "https://example.com/resume.pdf"})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if app.Status != "pending" {
		t.Fatalf("expected status 'pending', got '%s'", app.Status)
	}

	// Duplicate creation check
	_, err = svc.Create(ctx, 10, models.CreateApplicationInput{OpportunityID: 100, ResumeURL: "https://example.com/resume.pdf"})
	if err != services.ErrAlreadyApplied {
		t.Fatalf("expected ErrAlreadyApplied on duplicate submit, got %v", err)
	}
}

func TestApplicationService_UpdateStatusAuthorization(t *testing.T) {
	repo := newMockAppRepo()
	svc := services.NewApplicationService(repo)
	ctx := context.Background()

	// Artist (User 10) submits application to Opportunity 100 (Host is User 42)
	app, _ := svc.Create(ctx, 10, models.CreateApplicationInput{OpportunityID: 100, ResumeURL: "https://example.com/resume.pdf"})

	// Invalid status value test
	err := svc.UpdateStatus(ctx, 42, app.ID, "INVALID_STATUS")
	if err != services.ErrInvalidStatus {
		t.Fatalf("expected ErrInvalidStatus, got %v", err)
	}

	// Non-host (User 99) tries to update status -> ErrApplicationForbidden
	err = svc.UpdateStatus(ctx, 99, app.ID, "accepted")
	if err != services.ErrApplicationForbidden {
		t.Fatalf("expected ErrApplicationForbidden for non-host, got %v", err)
	}

	// Host (User 42) updates status -> Success
	err = svc.UpdateStatus(ctx, 42, app.ID, "accepted")
	if err != nil {
		t.Fatalf("unexpected error on host status update: %v", err)
	}

	// Verify status updated
	updatedApp, _ := svc.FindByID(ctx, 10, app.ID)
	if updatedApp.Status != "accepted" {
		t.Fatalf("expected status 'accepted', got '%s'", updatedApp.Status)
	}
}

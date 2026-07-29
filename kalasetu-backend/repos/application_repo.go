package repos

import (
	"context"
	"database/sql"
	"kalasetu/models"
)

type ApplicationRepository interface {
	Create(ctx context.Context, app *models.Application) error
	FindByID(ctx context.Context, id int) (*models.Application, error)
	UpdateStatus(ctx context.Context, id int, status string) error
}

type applicationRepository struct {
	db *sql.DB
}

func NewApplicationRepository(db *sql.DB) ApplicationRepository {
	return &applicationRepository{
		db: db,
	}
}

func (r *applicationRepository) Create(ctx context.Context, app *models.Application) error {
	query := `
		INSERT INTO applications (opportunity_id, applier_id, resume_url)
		VALUES ($1, $2, $3)
		RETURNING id, status, created_at
		`

	err := r.db.QueryRowContext(
		ctx, query,
		app.OpportunityID, app.ApplierID, app.ResumeURL).Scan(&app.ID, &app.Status, &app.CreatedAt)

	return err
}

func (r *applicationRepository) FindByID(ctx context.Context, id int) (*models.Application, error) {
	query := `
		SELECT id, opportunity_id, applier_id, resume_url, status, created_at
		FROM applications
		WHERE id = $1
		`
	app := &models.Application{}
	err := r.db.QueryRowContext(ctx, query, id).Scan(&app.ID, &app.OpportunityID, &app.ApplierID, &app.ResumeURL, &app.Status, &app.CreatedAt)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}

	return app, nil
}

func (r *applicationRepository) UpdateStatus(ctx context.Context, id int, status string) error {
	query := `
		UPDATE applications
		SET status = $1
		WHERE id = $2
	`
	result, err := r.db.ExecContext(ctx, query, status, id)

	if err != nil {
		return err
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return err
	}

	if rowsAffected == 0 {
		return sql.ErrNoRows
	}

	return nil

}

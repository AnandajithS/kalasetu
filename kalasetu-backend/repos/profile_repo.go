package repos

import (
	"context"
	"database/sql"
	"errors"
	"kalasetu/models"
)

type ProfileRepository interface {
	GetBasics(ctx context.Context, userID int) (*models.Profile, error)
	CountArtworks(ctx context.Context, userID int) (int, error)
	ListArtworkImages(ctx context.Context, userID int) ([]string, error)
	CountTotalLikes(ctx context.Context, userID int) (int, error)
	ListRecentPosts(ctx context.Context, userID int, limit int) ([]models.ProfilePost, error)
}

type profileRepository struct {
	db *sql.DB
}

func NewProfileRepository(db *sql.DB) ProfileRepository {
	return &profileRepository{db: db}
}

func (r *profileRepository) GetBasics(ctx context.Context, userID int) (*models.Profile, error) {
	query := `
		SELECT id, name, COALESCE(user_name, ''), COALESCE(location, ''),
		       COALESCE(bio, ''), COALESCE(profile_picture, ''), email
		FROM users
		WHERE id = $1
	`
	profile := &models.Profile{}
	err := r.db.QueryRowContext(ctx, query, userID).Scan(
		&profile.ID,
		&profile.Name,
		&profile.UserName,
		&profile.Location,
		&profile.Bio,
		&profile.ProfilePicture,
		&profile.Email,
	)
	if err != nil {
		if errors.Is(err, sql.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	return profile, nil
}

func (r *profileRepository) CountArtworks(ctx context.Context, userID int) (int, error) {
	query := `
		SELECT COUNT(*) FROM posts WHERE user_id = $1
	`
	var count int
	if err := r.db.QueryRowContext(ctx, query, userID).Scan(&count); err != nil {
		return 0, err
	}
	return count, nil
}

func (r *profileRepository) ListArtworkImages(ctx context.Context, userID int) ([]string, error) {
	query := `
		SELECT media_uri FROM posts
		WHERE user_id = $1 AND media_uri IS NOT NULL AND media_uri <> ''
		ORDER BY created_at DESC
	`
	rows, err := r.db.QueryContext(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	images := []string{}
	for rows.Next() {
		var uri string
		if err := rows.Scan(&uri); err != nil {
			return nil, err
		}
		images = append(images, uri)
	}
	return images, rows.Err()
}

func (r *profileRepository) CountTotalLikes(ctx context.Context, userID int) (int, error) {
	query := `
		SELECT COUNT(*)
		FROM likes l
		JOIN posts p ON l.parent_id = p.id AND l.parent_type = 'post'
		WHERE p.user_id = $1
	`
	var count int
	if err := r.db.QueryRowContext(ctx, query, userID).Scan(&count); err != nil {
		return 0, err
	}
	return count, nil
}

func (r *profileRepository) ListRecentPosts(ctx context.Context, userID int, limit int) ([]models.ProfilePost, error) {
	query := `
		SELECT p.id, p.content, COALESCE(p.media_type, ''), COALESCE(p.media_uri, ''),
		       p.created_at,
		       (SELECT COUNT(*) FROM likes    l WHERE l.parent_type = 'post' AND l.parent_id = p.id) AS like_count,
		       (SELECT COUNT(*) FROM comments c WHERE c.parent_type = 'post' AND c.parent_id = p.id) AS comment_count
		FROM posts p
		WHERE p.user_id = $1
		ORDER BY p.created_at DESC
		LIMIT $2
	`
	rows, err := r.db.QueryContext(ctx, query, userID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	posts := []models.ProfilePost{}
	for rows.Next() {
		var p models.ProfilePost
		if err := rows.Scan(
			&p.ID,
			&p.Content,
			&p.MediaType,
			&p.MediaURI,
			&p.CreatedAt,
			&p.LikeCount,
			&p.CommentCount,
		); err != nil {
			return nil, err
		}
		posts = append(posts, p)
	}
	return posts, rows.Err()
}
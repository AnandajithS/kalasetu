package services

import (
	"context"
	"errors"
	"kalasetu/models"
	"kalasetu/repos"
)

var ErrProfileNotFound = errors.New("profile not found")

const defaultRecentPostsLimit = 5

type ProfileService interface {
	GetProfile(ctx context.Context, userID int) (*models.Profile, error)
}

type profileService struct {
	profileRepo repos.ProfileRepository
}

func NewProfileService(profileRepo repos.ProfileRepository) ProfileService {
	return &profileService{profileRepo: profileRepo}
}

func (s *profileService) GetProfile(ctx context.Context, userID int) (*models.Profile, error) {
	profile, err := s.profileRepo.GetBasics(ctx, userID)
	if err != nil {
		return nil, err
	}
	if profile == nil {
		return nil, ErrProfileNotFound
	}

	profile.ArtworksCount, err = s.profileRepo.CountArtworks(ctx, userID)
	if err != nil {
		return nil, err
	}

	profile.ArtworksImages, err = s.profileRepo.ListArtworkImages(ctx, userID)
	if err != nil {
		return nil, err
	}

	profile.TotalLikes, err = s.profileRepo.CountTotalLikes(ctx, userID)
	if err != nil {
		return nil, err
	}

	profile.RecentPosts, err = s.profileRepo.ListRecentPosts(ctx, userID, defaultRecentPostsLimit)
	if err != nil {
		return nil, err
	}

	profile.Followers = 0
	profile.Following = 0
	profile.Skills = []string{}
	profile.Achievements = []models.Achievement{}

	return profile, nil
}
package services

import (
	"context"
	"errors"
	"kalasetu/models"
	"kalasetu/repos"
	"kalasetu/storage"
)

var ErrProfileNotFound = errors.New("profile not found")

const defaultRecentPostsLimit = 5

type ProfileService interface {
	GetProfile(ctx context.Context, userID int) (*models.Profile, error)
}

type profileService struct {
	profileRepo repos.ProfileRepository
	storage     storage.ObjectStorage
}

func NewProfileService(profileRepo repos.ProfileRepository, objectStorage storage.ObjectStorage) ProfileService {
	return &profileService{
		profileRepo: profileRepo,
		storage:     objectStorage,
	}
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

	artworksKeys, err := s.profileRepo.ListArtworkImages(ctx, userID)
	if err != nil {
		return nil, err
	}

	profile.ArtworksImages = make([]string, 0, len(artworksKeys))
	for _, key := range artworksKeys {
		if key != "" && s.storage != nil {
			url, err := s.storage.GetURL(ctx, key)
			if err == nil {
				profile.ArtworksImages = append(profile.ArtworksImages, url)
				continue
			}
		}
		profile.ArtworksImages = append(profile.ArtworksImages, key)
	}

	profile.TotalLikes, err = s.profileRepo.CountTotalLikes(ctx, userID)
	if err != nil {
		return nil, err
	}

	recentPosts, err := s.profileRepo.ListRecentPosts(ctx, userID, defaultRecentPostsLimit)
	if err != nil {
		return nil, err
	}

	if s.storage != nil {
		for i := range recentPosts {
			if recentPosts[i].MediaURI != "" {
				if url, err := s.storage.GetURL(ctx, recentPosts[i].MediaURI); err == nil {
					recentPosts[i].MediaURI = url
				}
			}
		}
	}
	profile.RecentPosts = recentPosts

	profile.Followers = 0
	profile.Following = 0
	profile.Skills = []string{}
	profile.Achievements = []models.Achievement{}

	return profile, nil
}
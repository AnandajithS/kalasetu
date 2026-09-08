package graph

// This file will not be regenerated automatically.
//
// It serves as dependency injection for your app, add any dependencies you require
// here.

import "kalasetu/services"

type Resolver struct {
	eventService       services.EventService
	userService        services.UserService
	applicationService services.ApplicationService
}

func NewResolver(eventService services.EventService, userService services.UserService, applicationService services.ApplicationService) *Resolver {
	return &Resolver{
		eventService:       eventService,
		userService:        userService,
		applicationService: applicationService,
	}
}

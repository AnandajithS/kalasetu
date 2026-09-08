package graph

// This file will not be regenerated automatically.
//
// It serves as dependency injection for your app, add any dependencies you require
// here.

import "kalasetu/services"

type Resolver struct {
	eventService services.EventService
}

func NewResolver(eventService services.EventService) *Resolver {
	return &Resolver{eventService: eventService}
}

package routes

import (
	"kalasetu/handlers"

	"github.com/gin-gonic/gin"
)

func RegisterApplicationRoutes(router *gin.RouterGroup, applicationHandler *handlers.ApplicationHandler, authMiddleware gin.HandlerFunc) {
	applicationGroup := router.Group("/applications")
	applicationGroup.Use(authMiddleware)
	{
		applicationGroup.POST("/", applicationHandler.Create)
	}
}

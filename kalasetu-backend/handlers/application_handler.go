package handlers

import (
	"kalasetu/models"
	"kalasetu/services"
	"net/http"

	"github.com/gin-gonic/gin"
)

type ApplicationHandler struct {
	applicationService services.ApplicationService
}

func NewApplicationHandler(applicationService services.ApplicationService) *ApplicationHandler {
	return &ApplicationHandler{
		applicationService: applicationService,
	}
}

func (h *ApplicationHandler) Create(c *gin.Context) {

	userID, exists := c.Get("user_id")

	if !exists {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "unauthorized"})
	}

	user, ok := userID.(int)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "invalid user id in context"})
		return
	}

	var input models.CreateApplicationRequest
	if err := c.ShouldBindJSON(&input); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if err := h.applicationService.Create(c.Request.Context(), user, input); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusCreated, gin.H{"message": "application created successfully"})

}

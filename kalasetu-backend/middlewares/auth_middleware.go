package middlewares

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
)

type contextKey string

const (
	UserIDCtxKey contextKey = "user_id"
	UserIDGinKey string     = "user_id"
)

// JWTAuthMiddleware validates the Authorization header JWT token
func JWTAuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Authorization header is missing"})
			c.Abort()
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if !(len(parts) == 2 && parts[0] == "Bearer") {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Authorization header format must be Bearer {token}"})
			c.Abort()
			return
		}

		tokenString := parts[1]
		accessSecret := os.Getenv("JWT_ACCESS_SECRET")
		if accessSecret == "" {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "JWT_ACCESS_SECRET environment variable is not set"})
			c.Abort()
			return
		}

		token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
			}
			return []byte(accessSecret), nil
		})

		if err != nil || !token.Valid {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid or expired token"})
			c.Abort()
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "invalid token claims"})
			c.Abort()
			return
		}

		userIdFloat, ok := claims["user_id"].(float64)
		if !ok {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "user_id not found in token claims"})
			c.Abort()
			return
		}
		userID := int(userIdFloat)

		// Set in Gin context (useful for REST handlers)
		c.Set(UserIDGinKey, userID)

		// Append to Go's standard request context
		ctx := context.WithValue(c.Request.Context(), UserIDCtxKey, userID)
		c.Request = c.Request.WithContext(ctx)

		c.Next()
	}
}

// GetUserIDFromContext retrieves the user ID from a standard Go context
func GetUserIDFromContext(ctx context.Context) (int, error) {
	val := ctx.Value(UserIDCtxKey)
	if val == nil {
		return 0, errors.New("user ID not found in context")
	}
	userID, ok := val.(int)
	if !ok {
		return 0, errors.New("user ID in context is not of type int")
	}
	return userID, nil
}

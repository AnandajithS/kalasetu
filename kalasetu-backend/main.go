package main

import (
	"kalasetu/app"

	"github.com/99designs/gqlgen/graphql/playground"
	"github.com/gin-gonic/gin"
)

func main() {
	App := app.NewApp()

	App.Router.POST("/api/v1/graphql", func(c *gin.Context) {
		App.Srv.ServeHTTP(c.Writer, c.Request)
	})

	App.Router.GET("/", func(c *gin.Context) {
		playground.Handler("GraphQL", "/api/v1/graphql").ServeHTTP(c.Writer, c.Request)
	})

	App.Router.Run()
}

package app

import (
	"kalasetu/config"
	"kalasetu/graph"
	"kalasetu/handlers"
	"kalasetu/migrations"
	"kalasetu/repos"
	"kalasetu/routes"
	"kalasetu/services"
	"log"
	"os"

	"github.com/99designs/gqlgen/graphql/handler"
	"github.com/99designs/gqlgen/graphql/handler/extension"
	"github.com/99designs/gqlgen/graphql/handler/lru"
	"github.com/99designs/gqlgen/graphql/handler/transport"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
	"github.com/vektah/gqlparser/v2/ast"
)

type App struct {
	Router *gin.Engine
	Srv    *handler.Server
	Port   string
}

const defaultPort = "8080"

func NewApp() *App {
	// Load .env file if it exists
	if err := godotenv.Load(); err != nil {
		log.Println("Note: .env file not found or failed to load. Falling back to system environment variables.")
	}

	r := gin.Default()

	port := os.Getenv("PORT")
	if port == "" {
		port = defaultPort
	}

	db, err := config.InitDB()
	if err != nil {
		log.Printf("Warning: Failed to connect to database: %v. Database operations will fail at runtime.", err)
	} else {
		// Run database migrations
		if err := migrations.RunMigrations(db); err != nil {
			log.Printf("Warning: Failed to run database migrations: %v", err)
		}
		log.Printf("Migrations done")
	}

	userRepo := repos.NewUserRepository(db)
	refreshTokenRepo := repos.NewRefreshTokenRepository(db)
	authService := services.NewAuthService(userRepo, refreshTokenRepo)
	authHandler := handlers.NewAuthHandler(authService)
	
	apiV1 := r.Group("/api/v1")
	routes.RegisterAuthRoutes(apiV1, authHandler)

	resolver := &graph.Resolver{}
	srv := gqlSetup(resolver)

	return &App{Router: r, Srv: srv, Port: port}
}

func gqlSetup(resolver *graph.Resolver) *handler.Server {
	srv := handler.New(graph.NewExecutableSchema(graph.Config{Resolvers: resolver}))

	srv.AddTransport(transport.Options{})
	srv.AddTransport(transport.GET{})
	srv.AddTransport(transport.POST{})

	srv.SetQueryCache(lru.New[*ast.QueryDocument](1000))

	srv.Use(extension.Introspection{})
	srv.Use(extension.AutomaticPersistedQuery{
		Cache: lru.New[string](100),
	})

	return srv
}

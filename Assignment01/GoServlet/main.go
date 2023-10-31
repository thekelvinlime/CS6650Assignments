package main

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

type AlbumInfo struct {
	Artist string `json:"artist,omitempty"`
	Title  string `json:"title,omitempty"`
	Year   string `json:"year,omitempty"`
}

type ImageMetaData struct {
	AlbumID   string `json:"albumID,omitempty"`
	ImageSize string `json:"imageSize,omitempty"`
}

func main() {
	router := gin.Default()
	router.GET("/albums/:id", getAlbumByID)
	router.POST("/albums", newAlbum)

	router.Run(":8082")
}

// postAlbums adds an album from JSON received in the request body.
func newAlbum(c *gin.Context) {
	id := c.PostForm("albumId")
	image, err := c.FormFile("image")
	if err != nil {
		c.IndentedJSON(http.StatusBadRequest, gin.H{
			"error": err.Error(),
		})
		return
	}

	if !strings.HasSuffix(strings.ToLower(image.Filename), ".png") && !strings.HasSuffix(strings.ToLower(image.Filename), ".jpg") {
		c.JSON(http.StatusBadRequest, gin.H{
			"error": "Not a .png or .jpg file!",
		})
		return
	}

	imageSize := image.Size

	imageMetaData := ImageMetaData{
		AlbumID:   id,
		ImageSize: fmt.Sprintf("%d", imageSize),
	}

	c.IndentedJSON(http.StatusOK, imageMetaData)
}

func getAlbumByID(c *gin.Context) {
	//id := c.Param("AlbumId")
	albumInfo := AlbumInfo{
		Artist: "Sex Pistols",
		Title:  "Never Mind the Bollocks",
		Year:   "1997",
	}
	c.IndentedJSON(http.StatusOK, albumInfo)
}

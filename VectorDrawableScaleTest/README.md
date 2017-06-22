This application tries to load a static png file as the golden image, and draws this png to a SurfaceView. Finally, reads the SurfaceView back via PixelCopy. Ideally, the read-back SurfaceView should be exactly the same as the initial png image.
  
* The PixelCopy API used in this app is PixelCopy.request(SurfaceView surfaceView, ... )  
  * API required to be 24+

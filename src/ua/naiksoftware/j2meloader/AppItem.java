package ua.naiksoftware.j2meloader;

/**
 * 
 * @author Naik
 */
public class AppItem {

    private int imageId;
    private String title;
	private String path;

    public AppItem(int imageId_, String title_) {
        imageId = imageId_;
        title = title_;
    }

	public void setPath(String p) {
		path = p;
	}

	public String getPath() {
		return path;
	}

    public void setTitle(String title_) {
        title = title_;
    }

    public String getTitle() {
        return title;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getImageId() {
        return imageId;
    }
}

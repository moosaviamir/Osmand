package net.osmand.plus.render;

import android.graphics.*;
import net.osmand.data.LatLon;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.resources.ResourceManager;
import net.osmand.plus.views.BaseMapLayer;
import net.osmand.plus.views.MapTileLayer;
import net.osmand.plus.views.OsmandMapTileView;

public class MapVectorLayer extends BaseMapLayer {

	private OsmandMapTileView view;
	private ResourceManager resourceManager;
	private Paint paintImg;
	
	private RectF destImage = new RectF();
	private final MapTileLayer tileLayer;
	private boolean visible = false;
	
	public MapVectorLayer(MapTileLayer tileLayer){
		this.tileLayer = tileLayer;
	}
	

	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public void initLayer(OsmandMapTileView view) {
		this.view = view;
		resourceManager = view.getApplication().getResourceManager();
		paintImg = new Paint();
		paintImg.setFilterBitmap(true);
		paintImg.setAlpha(getAlpha());
	}
	
	public boolean isVectorDataVisible() {
		return visible &&  view.getZoom() >= view.getSettings().LEVEL_TO_SWITCH_VECTOR_RASTER.get();
	}
	
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		if(!visible){
			resourceManager.getRenderer().clearCache();
		}
	}
	
	@Override
	public int getMaximumShownMapZoom() {
		return 23;
	}
	
	@Override
	public int getMinimumShownMapZoom() {
		return 1;
	}
	

	@Override
	public void onDraw(Canvas canvas, RotatedTileBox tilesRect, DrawSettings drawSettings) {
		if (!visible) {
			return;
		}
		if (!isVectorDataVisible() && tileLayer != null) {
			tileLayer.drawTileMap(canvas, tilesRect);
			resourceManager.getRenderer().interruptLoadingMap();
		} else {
			if (!view.isZooming()) {
				if (resourceManager.updateRenderedMapNeeded(tilesRect, drawSettings)) {
					// pixRect.set(-view.getWidth(), -view.getHeight() / 2, 2 * view.getWidth(), 3 * view.getHeight() / 2);
					final RotatedTileBox copy = tilesRect.copy();
					copy.increasePixelDimensions(copy.getPixWidth() / 3, copy.getPixHeight() / 4);
					resourceManager.updateRendererMap(copy);
				}

			}

			MapRenderRepositories renderer = resourceManager.getRenderer();
			drawRenderedMap(canvas, renderer.getBitmap(), renderer.getBitmapLocation(), tilesRect);
			drawRenderedMap(canvas, renderer.getPrevBitmap(), renderer.getPrevBmpLocation(), tilesRect);
		}
	}


	private boolean drawRenderedMap(Canvas canvas, Bitmap bmp, RotatedTileBox bmpLoc, RotatedTileBox currentViewport) {
		boolean shown = false;
		if (bmp != null && bmpLoc != null) {
			float rot = bmpLoc.getRotate();
			final LatLon lt = bmpLoc.getLeftTopLatLon();
			final LatLon rb = bmpLoc.getRightBottomLatLon();
			canvas.rotate(-rot, currentViewport.getCenterPixelX(), currentViewport.getCenterPixelY());
			final int x1 = currentViewport.getPixXFromLonNoRot(lt.getLongitude());
			final int y1 = currentViewport.getPixYFromLatNoRot(lt.getLatitude());
			final int x2 = currentViewport.getPixXFromLonNoRot(rb.getLongitude());
			final int y2 = currentViewport.getPixYFromLatNoRot(rb.getLatitude());
			destImage.set(x1, y1, x2, y2);
			if(!bmp.isRecycled()){
				canvas.drawBitmap(bmp, null, destImage, paintImg);
				shown = true;
			}
			canvas.rotate(rot, currentViewport.getCenterPixelX(), currentViewport.getCenterPixelY());
		}
		return shown;
	}

	
	@Override
	public void setAlpha(int alpha) {
		super.setAlpha(alpha);
		if (paintImg != null) {
			paintImg.setAlpha(alpha);
		}
	}	
	
	@Override
	public boolean onLongPressEvent(PointF point, RotatedTileBox tileBox) {
		return false;
	}

	@Override
	public boolean onSingleTap(PointF point, RotatedTileBox tileBox) {
		return false;
	}
}

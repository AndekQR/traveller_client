package com.client.traveller.data.network.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.client.traveller.R
import com.client.traveller.data.network.api.places.API_KEY
import com.client.traveller.data.network.api.places.PlacesApiService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.my_place_map_marker.view.*

class MyClusterItemRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<NearbyPlaceClusterItem>
) : DefaultClusterRenderer<NearbyPlaceClusterItem>(context.applicationContext, map, clusterManager) {



    override fun onClusterItemRendered(item: NearbyPlaceClusterItem?, marker: Marker?) {
        val baseView = (this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.my_place_map_marker, null
        )
        if (item?.place?.photos != null && item.place.photos.isNotEmpty()) {
            val placePhotoReference = item.place.photos.first().photoReference
            val placePhotoUrl = this.getPhotoUrl(placePhotoReference, 100)
            Log.e(javaClass.simpleName, placePhotoUrl)
            Glide
                .with(baseView)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(0.1f)
                .load(placePhotoUrl)
                .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    baseView.place_photo.setImageBitmap(resource)
                    val bitmap = this@MyClusterItemRenderer.getBitmapFromView(baseView)
                    Log.e(javaClass.simpleName, bitmap?.height.toString())
                    marker?.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                }

            })
        } else {
            baseView.place_photo.setImageResource(R.drawable.ic_default_home)
            val bitmap = this.getBitmapFromView(baseView)
            marker?.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
        }
    }




    private fun getBitmapFromView(view: View): Bitmap? {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        view.background?.draw(canvas)
        view.draw(canvas)
        return bitmap
    }

    private fun getPhotoUrl(reference: String, width: Int): String {
        return "${PlacesApiService.BASE_URL}photo?maxwidth=${width}&photoreference=${reference}&key=$API_KEY"
    }
}
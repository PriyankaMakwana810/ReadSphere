package com.tridya.readsphere.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.tridya.readsphere.R
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation
import jp.wasabeef.glide.transformations.CropSquareTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File

class GlideUtils(private val context: Context) {

    private val options: RequestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(Priority.HIGH)

    private var loadListener: ImageLoadListener? = null
    private var transformations: ArrayList<MultiTransformation<Bitmap>> = arrayListOf()

    private var transitionOptions: DrawableTransitionOptions? = null

    fun circleImage(borderSize: Int? = null, borderColor: Int = Color.BLACK): GlideUtils {
        if (borderSize == null) {
            options.circleCrop()
        } else {
            squareCropImage()
            transformations.add(
                MultiTransformation(
                    CropCircleWithBorderTransformation(
                        borderSize,
                        borderColor
                    )
                )
            )
        }
        return this
    }

    fun crossFade(duration: Int = 300): GlideUtils {
        transitionOptions = DrawableTransitionOptions().crossFade(duration)
        return this
    }

    fun squareCropImage(): GlideUtils {
        transformations.add(MultiTransformation(CropSquareTransformation()))
        return this
    }

    fun cornerRadius(
        radius: Int,
        margin: Int = 0,
        cornerType: RoundedCornersTransformation.CornerType = RoundedCornersTransformation.CornerType.ALL,
    ): GlideUtils {
        transformations.add(
            MultiTransformation(
                RoundedCornersTransformation(
                    radius,
                    margin,
                    cornerType
                )
            )
        )
        return this
    }


    fun size(height: Int = Target.SIZE_ORIGINAL, width: Int = Target.SIZE_ORIGINAL): GlideUtils {
        options.override(width, height)
        return this
    }

    fun size(size: Int = Target.SIZE_ORIGINAL): GlideUtils {
        options.override(size)
        return this
    }

    fun addListener(listener: ImageLoadListener): GlideUtils {
        loadListener = listener
        return this
    }

    private fun getListener(): RequestListener<Drawable?> {
        val listener: RequestListener<Drawable?> = object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean,
            ): Boolean {
                loadListener?.onLoadFailed(e?.localizedMessage)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean,
            ): Boolean {
                loadListener?.onLoadSuccess()
                return false
            }

        }
        return listener
    }

    private fun setTransformations() {
        if (!transformations.isNullOrEmpty())
            options.transform(MultiTransformation(transformations))
    }

    fun loadImage(image: String?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .centerCrop()
            .into(imageView!!)
    }

    fun loadImageLibrary(image: String?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)
        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.placeholder_cat)
            .placeholder(R.drawable.placeholder_cat)
            .centerCrop()
            .into(imageView!!)
    }

    fun loadImage(image: File?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .into(imageView!!)
    }

    fun loadImage(image: Uri?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .into(imageView!!)
    }

    fun loadImage(image: Bitmap?, imageView: ImageView?) {
        setTransformations()
        var glide = Glide.with(context)
            .asDrawable()
            .load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .into(imageView!!)
    }

    fun loadGif(image: Int?, imageView: ImageView?) {
        setTransformations()
        var glide = Glide.with(context)
            .asGif()
            .load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .into(imageView!!)
    }

    fun loadImage(image: Int?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .into(imageView!!)
    }

    fun loadImage(image: Drawable?, imageView: ImageView?) {
        setTransformations()
        val glide = Glide.with(context).load(image)

        if (transitionOptions != null) {
            glide.transition(transitionOptions!!)
        }

        glide.apply(options)
            .listener(getListener())
            .error(R.drawable.ic_book_black_24dp)
            .placeholder(R.drawable.ic_book_black_24dp)
            .into(imageView!!)
    }

    interface ImageLoadListener {
        fun onLoadFailed(message: String?)
        fun onLoadSuccess()
    }

}
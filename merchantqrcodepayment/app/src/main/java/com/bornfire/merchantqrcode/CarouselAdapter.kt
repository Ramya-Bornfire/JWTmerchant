package com.bornfire.merchantqrcode

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

class CarouselAdapter(
    private val context: Context,
    private val items: List<CarouselItem>,
    private val onItemClick: (Int) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.carousel_item, container, false)
        val imageView: ImageView = view.findViewById(R.id.imageView)

        // Load the image using Glide
        Glide.with(context)
            .load(items[position].image)
            .into(imageView)

        // Set click listener for the item
        view.setOnClickListener {
            onItemClick(position)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}

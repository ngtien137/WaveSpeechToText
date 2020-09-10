package com.lhd.wave_speech_demo.utils

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.base.baselibrary.adapter.BaseAdapter
import com.base.baselibrary.adapter.BaseListAdapter
import com.base.baselibrary.adapter.SuperAdapter
import com.base.baselibrary.utils.getAppDrawable
import com.base.baselibrary.utils.onDebouncedClick
import com.base.baselibrary.utils.onDebouncedClickGlobal
import com.base.baselibrary.views.custom.CustomViewPager
import com.base.baselibrary.views.rv_touch_helper.ItemTouchHelperExtension
import com.base.baselibrary.views.rv_touch_helper.VerticalDragTouchHelper
import com.bumptech.glide.Glide
import java.io.File


@BindingConversion
fun convertBooleanToVisibility(visible: Boolean): Int {
    return if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("pager_set_adapter")
fun ViewPager.setOwnAdapter(adapter: FragmentStatePagerAdapter?) {
    adapter?.let {
        setAdapter(adapter)
    }
}

@BindingAdapter("tv_apply_height")
fun TextView.applyHeightToNone(height: Float) {
    layoutParams.height = height.toInt()
}


@BindingAdapter("tv_apply_marquee")
fun TextView.applyMarquee(apply: Boolean?) {
    post {
        apply?.let {
            if (it) {
                ellipsize = TextUtils.TruncateAt.MARQUEE
                isSelected = true
            }
        }
    }
}

@BindingAdapter("tv_get_file_name_from_path")
fun TextView.setNameFromFile(path: String?) {
    path?.let {
        val file = File(it)
        text = file.name
    }
}

@BindingAdapter("sw_checked_listener")
fun CompoundButton.applyCheckedListener(checkedListener: CompoundButton.OnCheckedChangeListener?) {
    setOnCheckedChangeListener(checkedListener)
}

@BindingAdapter("translate_from_bottom")
fun View.translateFromBottom(isOpen: Boolean?) {
    post {
        if (translationY.toInt() == height && isOpen == true) {
            animate().translationY(0f)
        } else if (translationY.toInt() == 0 && isOpen != true) {
            animate().translationY(height.toFloat())
        }
    }
}

@BindingAdapter("translate_from_end")
fun View.translateFromEnd(isOpen: Boolean?) {
    post {
        if (translationX.toInt() == width && isOpen == true) {
            animate().translationY(0f)
        } else if (translationX.toInt() == 0 && isOpen != true) {
            animate().translationY(width.toFloat())
        }
    }
}

@BindingAdapter("debounceClick")
fun View.onDeboundClick(listener: View.OnClickListener?) {
    listener?.let {
        this.onDebouncedClick {
            listener.onClick(this)
        }
    }

}

@BindingAdapter("debounceClickGlobal")
fun View.onDebouncedGlobal(listener: View.OnClickListener?) {
    listener?.let {
        this.onDebouncedClickGlobal {
            listener.onClick(this)
        }
    }

}

@BindingAdapter("longClick")
fun View.onLongClickView(listener: View.OnLongClickListener) {
    setOnLongClickListener { v -> listener.onLongClick(v) }
}

@BindingAdapter("rv_apply_item_touch_helper")
fun RecyclerView.applyItemTouchHelper(itemTouchHelperExtension: ItemTouchHelperExtension?) {
    itemTouchHelperExtension?.attachToRecyclerView(this)
}

@BindingAdapter("rv_vertical_drag")
fun RecyclerView.enableVerticalDrag(enable: Boolean?) {
    enable?.let {
        adapter?.let {
            if (adapter is SuperAdapter<*>) {
                val callback = VerticalDragTouchHelper(adapter as SuperAdapter<*>)
                ItemTouchHelper(callback).attachToRecyclerView(this)
            }
        }
    }
}

@BindingAdapter("rv_snap_linear")
fun RecyclerView.attachLinearSnapHelper(snap: Boolean? = true) {
    if (snap == true) {
        LinearSnapHelper().attachToRecyclerView(this)
    }
}

@BindingAdapter("rv_set_adapter")
fun <T : Any> RecyclerView.applyAdapter(applyAdapter: BaseAdapter<T>?) {
    applyAdapter?.apply {
        adapter = applyAdapter
    }
}

@BindingAdapter("rv_set_adapter")
fun <T : RecyclerView.ViewHolder> RecyclerView.applyAdapter(applyAdapter: RecyclerView.Adapter<T>?) {
    applyAdapter?.apply {
        adapter = applyAdapter
    }
}

@BindingAdapter("rv_set_adapter")
fun <T : Any> RecyclerView.applyAdapter(applyAdapter: SuperAdapter<T>?) {
    applyAdapter?.apply {
        adapter = applyAdapter
    }
}

@BindingAdapter("rv_set_adapter")
fun <T : Any> RecyclerView.applyListAdapter(applyAdapter: BaseListAdapter<T>?) {
    applyAdapter?.apply {
        adapter = applyAdapter
    }
}

@BindingAdapter("rv_set_fix_size")
fun RecyclerView.setFixSize(set: Boolean?) {
    setHasFixedSize(set ?: false)
}

@BindingAdapter("img_set_drawable_id")
fun ImageView.setDrawableById(id: Int) {
    setImageResource(id)
}

@BindingAdapter("img_set_drawable_glide")
fun ImageView.setDrawableGlide(drawable: Drawable?) {
    drawable?.let {
        post {
            Glide.with(this).load(drawable).override(width, height).into(this)
        }
    }
}

@BindingAdapter("anim_visible")
fun View.startAnimVisible(visible: Int?) {
    if (visible == null) {
        return
    }
    if (visible == View.VISIBLE && this.visibility != View.VISIBLE) {
        this.visibility = visible
        alpha = 0f
        animate().alpha(1f)
    } else if ((visible == View.GONE && this.visibility != View.GONE) || (visible == View.INVISIBLE && this.visibility != View.INVISIBLE)) {
        alpha = 1f
        animate().alpha(0f)
        postDelayed({
            this.visibility = visible
        }, 300)
    }
}

@BindingAdapter("pager_swipe_able")
fun CustomViewPager.setSwipeAble(swipe: Boolean?) {
    swipe?.let {
        isAbleSwipe = it
    }
}

//Your binding write below


package cn.settile.fanboxviewer.Adapters.RecyclerView.Supporting

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.settile.fanboxviewer.Network.Bean.SupportingItem
import cn.settile.fanboxviewer.R
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class SupportingRecycleViewAdapter(private val dataSet:Array<SupportingItem>) : RecyclerView.Adapter<SupportingRecycleViewAdapter.SupportingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.component_item_supporting, parent, false)
        return SupportingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupportingViewHolder, position: Int) {
        holder.creatorName.text = dataSet[position].creator
        holder.title.text = dataSet[position].title
        holder.fee.text = dataSet[position].plan
        holder.desc.text = dataSet[position].desc
        holder.cover.setVisibility(View.VISIBLE)
        Picasso.get().load(dataSet[position].coverImgURL).into(holder.cover)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class SupportingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val creatorName : TextView
        val title : TextView
        val fee: TextView
        val desc: TextView
        val cover: ImageView
        init {
            creatorName = itemView.findViewById(R.id.creatorName)
            title = itemView.findViewById(R.id.supporting_title)
            fee = itemView.findViewById(R.id.supporting_fee)
            desc = itemView.findViewById(R.id.supporting_description)

            cover = itemView.findViewById(R.id.supporting_coverImg)
        }
    }

}
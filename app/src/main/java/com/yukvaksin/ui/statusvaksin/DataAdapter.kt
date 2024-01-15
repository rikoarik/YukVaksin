package com.yukvaksin.ui.statusvaksin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yukvaksin.R
import com.yukvaksin.ui.ModelInput


class DataAdapter(private val mContext: Context) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    private val modelInputList: ArrayList<ModelInput> = ArrayList()

    fun setDataAdapter(items: MutableList<ModelInput>) {
        modelInputList.clear()
        modelInputList.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_vaksin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = modelInputList[position]

        holder.tvDate.text = data.tanggalVaksin
        holder.tvNama.text = data.nama
        holder.tvNik.text = data.nik
        holder.tvTanggalLahir.text = data.tanggalLahir
    }

    override fun getItemCount(): Int {
        return modelInputList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvNik: TextView = itemView.findViewById(R.id.tvNik)
        val tvTanggalLahir: TextView = itemView.findViewById(R.id.tvTanggalLahir)
    }
}

package com.yukvaksin.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yukvaksin.R
import com.yukvaksin.ui.Constant
import com.yukvaksin.ui.InputDataActivity

class HomeAdapter(private val mContext: Context, private val hospitalList: List<ModelHome>) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hospital = hospitalList[position]

        holder.tvPlaceName.text = hospital.name
        holder.tvVicinity.text = ("Alamat : "+hospital.address)
        holder.tvInfo.text = ("Info : " + hospital.info)
        holder.tvPhone.text = ("Tlp : "+ hospital.phone)

        if (hospital.bedAvailability == 1) {
            holder.tvStatus.text = "Status : Available"
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.green))

            holder.cvListMain.setOnClickListener {
                Constant.namaRS = hospitalList[position].name;
                val intent = Intent(mContext, InputDataActivity::class.java)
                mContext.startActivity(intent)
            }
        } else {
            holder.tvStatus.text = "Status : Not Available"
            holder.tvStatus.setTextColor(ContextCompat.getColor(mContext, R.color.red))

            holder.cvListMain.setOnClickListener {
                Toast.makeText(mContext, "Tempat Tidur Tidak Tersedia Di Sini!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvListMain: CardView = itemView.findViewById(R.id.cvListMain)
        val tvPlaceName: TextView = itemView.findViewById(R.id.textViewHospitalName)
        val tvVicinity: TextView = itemView.findViewById(R.id.textViewHospitalAddress)
        val tvStatus: TextView = itemView.findViewById(R.id.textViewAvailabilityInfo)
        val tvInfo: TextView = itemView.findViewById(R.id.textViewLastUpdated)
        val tvPhone: TextView = itemView.findViewById(R.id.textViewPhone)
    }
}

package com.yukvaksin.ui

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object Constant {
    var namaRS: String? = null
    fun cekUmurPeserta(strTanggal: String?): Int {
        var umurPeserta = 0
        val formatDefault = SimpleDateFormat("dd-MM-yyyy")
        try {
            val dateNow: Int = Calendar.getInstance().get(Calendar.YEAR)
            val dateFormat: Date = formatDefault.parse(strTanggal)
            val calendar: Calendar = Calendar.getInstance()
            calendar.setTime(dateFormat)
            val umurSekarang: Int = calendar.get(Calendar.YEAR)
            umurPeserta = dateNow - umurSekarang
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return umurPeserta
    }
}
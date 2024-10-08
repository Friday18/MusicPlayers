package com.ssp.musicplayer.data

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.ssp.musicplayer.R

data class Track(
    val name: String,
    val desc: String,
    @RawRes val id:Int,
    @DrawableRes val image: Int
) {
    constructor() : this("", "", R.raw.aaj_ki_raat, R.drawable.aaj_ki_raat_img)
}


val songs = listOf(
    Track(
        name = "Aaj Ki Raat",
        desc =
        "Aaj ki raattt",
        id = R.raw.aaj_ki_raat,
        image = R.drawable.aaj_ki_raat_img
    ), Track(
        name = "Angaro",
        desc =
        "Angaro",
        id = R.raw.angaaron,
        image = R.drawable.angaro_img
    ), Track(
        name = "Hauli Hauli",
        desc =
        "Hauli Hauli",
        id = R.raw.hauli_hauli,
        image = R.drawable.hauli_haului_img
    ), Track(
        name = "Maar Udi",
        desc =
        "Maar Udi",
        id = R.raw.maar_udi,
        image = R.drawable.maar_udi_img
    ), Track(
        name = "Tu hai Champion",
        desc =
        "Tu hai Champion",
        id = R.raw.tu_hai_cham,
        image = R.drawable.tu_hai_champ_img
    )

)
package uz.gita.myplayer.data

data class MusicData (
    var id : Int,
    var title : String?,
    var artist : String?,
    var data : String?,
    var duration : Long,
    var album_id : Long
        )
package uz.gita.myplayer.presentation.ui.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import uz.gita.myplayer.databinding.ItemMusicBinding
import uz.gita.myplayer.utils.getMusicByPosition
import uz.gita.myplayer.utils.songArt

class MyCursorAdapter() : RecyclerView.Adapter<MyCursorAdapter.VH>() {
    var cursor : Cursor? = null
    var isClick = true
    var scope  = CoroutineScope(Dispatchers.IO)
    private var selectMusicListListener : ((Int)-> Unit)?= null

    inner class VH(val binding : ItemMusicBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                if (isClick){
                    isClick = false
                    selectMusicListListener?.invoke(absoluteAdapterPosition)
                    scope.launch {
                        delay(400)
                        isClick = true
                    }
                }
            }
        }
        fun bind(){
            val item = cursor?.getMusicByPosition(absoluteAdapterPosition)!!
            binding.apply {
                textArtistName.text = item.artist
                textMusicName.text = item.title
                imageMusic.setImageURI(binding.root.context!!.songArt(item.album_id))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMusicBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = cursor?.count ?: 0
    fun setSelectMusicListListener(block:((Int)->Unit)){
        selectMusicListListener = block
    }


}
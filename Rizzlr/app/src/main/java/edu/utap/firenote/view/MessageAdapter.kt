package edu.utap.firenote.view

import android.graphics.Color
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.marginRight
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.firenote.ModernViewModel
import edu.utap.firenote.databinding.MessageListRowBinding
import edu.utap.firenote.model.Message
import java.util.*


class MessageAdapter(private val viewModel: ModernViewModel)
    : ListAdapter<Message, MessageAdapter.VH>(Diff()) {
    // This class allows the adapter to compute what has changed
    class Diff : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.firestoreID == newItem.firestoreID
                    && oldItem.text == newItem.text
                    && oldItem.receiverUID == newItem.receiverUID
                    && oldItem.senderUID == newItem.senderUID
                    && oldItem.timeStamp == newItem.timeStamp
        }
    }

    // Puts the time first, which is most important.  But date is useful too
    private val dateFormat: DateFormat =
        SimpleDateFormat("hh:mm:ss MM-dd-yyyy", Locale.US)

    // https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder#getBindingAdapterPosition()
    // Getting the position of the selected item is unfortunately complicated
    // This always returns a valid index.
    private fun getPos(holder: VH) : Int {
        val pos = holder.bindingAdapterPosition
        // notifyDataSetChanged was called, so position is not known
        if( pos == RecyclerView.NO_POSITION) {
            return holder.absoluteAdapterPosition
        }
        return pos
    }

    inner class VH(private val messageListRowBinding: MessageListRowBinding) :
        RecyclerView.ViewHolder(messageListRowBinding.root) {

        init {

        }
        fun bind(holder: VH, position: Int) {
            val message = currentList.get(position)
            holder.messageListRowBinding.text.text = message.text
            //Log.d(javaClass.simpleName, "bind adapter ${bindingAdapterPosition}")
            message.timeStamp?.let {
                holder.messageListRowBinding.timestamp.text = dateFormat.format(it.toDate())
            }

            if(viewModel.getProfile().firestoreID == message.senderUID) {
                holder.messageListRowBinding.text.gravity = Gravity.RIGHT
                holder.messageListRowBinding.timestamp.gravity = Gravity.RIGHT
                holder.messageListRowBinding.myLayout.setBackgroundColor(Color.parseColor("#FFFFCCCC"))
                holder.messageListRowBinding.startBorder.layoutParams.width = 400
                holder.messageListRowBinding.endBorder.layoutParams.width = 50



            } else {
                holder.messageListRowBinding.endBorder.layoutParams.width = 400
                holder.messageListRowBinding.startBorder.layoutParams.width = 50

                holder.messageListRowBinding.text.gravity = Gravity.LEFT
                holder.messageListRowBinding.timestamp.gravity = Gravity.LEFT

                if(viewModel.getProfile().chatPartnerID1 == message.senderUID) {
                    holder.messageListRowBinding.myLayout.setBackgroundColor(Color.parseColor("#FFFFFF99"))
                } else {
                    holder.messageListRowBinding.myLayout.setBackgroundColor(Color.parseColor("#FF99FF99"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val messageListRowBinding = MessageListRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return VH(messageListRowBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(holder, position)
    }
}
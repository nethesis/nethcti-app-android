import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.nethesis.models.decorator.PresenceDecorator
import org.linphone.R
import org.linphone.interfaces.OnNethStatusSelected

class PresenceStatusAdapter(
    var status: MutableList<String> = arrayListOf(),
    var selected: String?,
    val onStatusItemListener: OnNethStatusSelected,
) : RecyclerView.Adapter<PresenceStatusAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.status_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.setContent(status.get(position), position) }
    override fun getItemCount() = status.size

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private var txtStatus: TextView? = null
        private var imgStatus: ImageView? = null

        private var status: String = ""
        private var position: Int? = null

        val context: Context
            get() = itemView.context

        init {
            imgStatus = itemView.findViewById(R.id.img_status)
            txtStatus = itemView.findViewById(R.id.txt_status)
            itemView.setOnClickListener(this)
        }

        fun setContent(status: String, _position: Int) {
            position = _position
            this.status = status

            txtStatus?.setTextColor(
                if (selected == status)
                    ContextCompat.getColor(context, R.color.presence_selected_item)
                else
                    ContextCompat.getColor(context, R.color.secondaryTextColor)
            )

            txtStatus?.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                if (selected == status) R.drawable.ic_item_selected else 0,
                0
            )

            PresenceDecorator(status).apply {
                ContextCompat.getDrawable(context, smallCircleStatusIcon)?.let {
                    imgStatus?.setImageDrawable(it)
                }

                txtStatus?.text = context.getString(statusLabel)
            }

        }

        override fun onClick(v: View?) {
            //if (this.status == selected) return
            onStatusItemListener.onNethStatusSelected(status, position?: -1)
        }

    }

}
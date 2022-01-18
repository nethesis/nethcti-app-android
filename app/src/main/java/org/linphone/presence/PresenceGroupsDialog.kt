package org.linphone.presence

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.linphone.R

class PresenceGroupsDialog(
    context: Context
) : BottomSheetDialog(context) {

    var txtAsd: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheet = layoutInflater.inflate(R.layout.presence_groups_bottom_sheet_fragment, null)
        setContentView(bottomSheet)

    }

    override fun onCreatePanelView(featureId: Int): View? {
        val view = super.onCreatePanelView(featureId)

        return view
    }

    companion object {
        public const val TAG: String = "ModalBottomSheet"
    }

}
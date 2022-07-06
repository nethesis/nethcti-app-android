package org.linphone.presence

import PresenceStatusAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.nethesis.models.decorator.PresenceDecorator.PRESENCE_STATUS_CALL_FORWARD
import it.nethesis.models.decorator.PresenceDecorator.PRESENCE_STATUS_DISABLED
import it.nethesis.models.presence.PresenceStatusRequest
import it.nethesis.models.presence.PresenceStatusWithCodeRequest
import it.nethesis.webservices.RetrofitGenerator
import it.nethesis.webservices.UserRestAPI
import okhttp3.ResponseBody
import org.linphone.BuildConfig.DEBUG
import org.linphone.LinphoneManager
import org.linphone.NethCTIApplication.Companion.dayNightThemeColor
import org.linphone.R
import org.linphone.core.ProxyConfig
import org.linphone.interfaces.OnNethStatusSelected
import org.linphone.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PresenceStatusActivity : AppCompatActivity(), Callback<MutableList<String>>,
    View.OnClickListener, OnNethStatusSelected, DialogInterface.OnShowListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var mProxyConfig: ProxyConfig? = null

    private var userRestAPI: UserRestAPI? = null

    private var questionCard: CardView? = null
    private var imgClose: ImageView? = null
    private var recyclerStatusRecycler: RecyclerView? = null
    private var txtAlertMessage: TextView? = null
    private var rlvProgressContent: RelativeLayout? = null

    private var swpStatus: SwipeRefreshLayout? = null

    private var status: MutableList<String> = mutableListOf()
    private var selectedStatus: String? = null
    private var newStatus: String? = null

    private var insertCellphoneAlertDialog: AlertDialog? = null
    private var phoneNumberEditText: EditText? = null

    private var delay = false

    private var setPresenceCallback = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            rlvProgressContent?.visibility = GONE
            if (response.isSuccessful) {
                updateRecyclerAndTerminate()
            } else {
                //TODO messaggio di errore
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            setUiStatusFailureWithMessage(getString(R.string.neth_no_connection))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presence_status)
        initUi()

        // Get the current user proxy config, useful for logout by now.
        mProxyConfig = LinphoneManager.getUserProxyConfig(0)

        //messaggio -> neth_no_status
        intent.getStringExtra(STATUS_SELECTED)?.apply {
            selectedStatus = this
        }

        val domain = SharedPreferencesManager.getDomain(this)
        val authToken = SharedPreferencesManager.getAuthtoken(this)

        userRestAPI = RetrofitGenerator.createService(UserRestAPI::class.java, domain)
        userRestAPI?.getPresenceStatusList(authToken)?.enqueue(this)
        rlvProgressContent?.visibility = VISIBLE

        setResult(RESULT_CANCELED)

    }

    private fun initUi() {
        imgClose = findViewById(R.id.img_close)
        imgClose?.setOnClickListener(this)

        questionCard = findViewById(R.id.question_card)
        questionCard?.setCardBackgroundColor(
            if (dayNightThemeColor)
                getColor(R.color.black_color)
            else
                getColor(R.color.white_color)
        )

        txtAlertMessage = findViewById(R.id.txt_alert_message)
        recyclerStatusRecycler = findViewById(R.id.recycler_status)
        rlvProgressContent = findViewById(R.id.rlv_progress_content)

        swpStatus = findViewById(R.id.swp_status)
        swpStatus?.setOnRefreshListener(this)

        val layoutManager = LinearLayoutManager(this@PresenceStatusActivity)
        recyclerStatusRecycler?.layoutManager = layoutManager

    }

    override fun onRefresh() {
        val authToken = SharedPreferencesManager.getAuthtoken(this)
        userRestAPI?.getPresenceStatusList(authToken)?.enqueue(this)
    }

    override fun onResponse(
        call: Call<MutableList<String>>,
        response: Response<MutableList<String>>
    ) {
        rlvProgressContent?.visibility = GONE
        when (response.code()) {
            200 -> response.body()?.apply {
                setUiStatusOk()

                status = this

                if (status.isEmpty())
                    setUiStatusFailureWithMessage(getString(R.string.neth_no_status))
                else
                    setUiStatusOk()

                // Stato sempre presente e gestito solo localmente
                add(PRESENCE_STATUS_DISABLED)

                //val mtl = mutableListOf<String>("test", "test", "test", "test", "test", "test", "test", "test")
                recyclerStatusRecycler?.apply {
                    adapter =  PresenceStatusAdapter(
                        status,
                        selectedStatus,
                        this@PresenceStatusActivity
                    )
                    adapter?.notifyDataSetChanged()
                }
            }
            401 -> setUiStatusFailureWithMessage(getString(R.string.neth_session_expired))
            else -> setUiStatusFailureWithMessage(getString(R.string.neth_something_wrong))
        }

    }

    override fun onFailure(call: Call<MutableList<String>>, t: Throwable) {
        setUiStatusFailureWithMessage(getString(R.string.neth_no_connection))
    }

    private fun setUiStatusOk() {
        terminateAllProgress()
        hideAlertMessage()
    }

    private fun setUiStatusFailureWithMessage(message: String) {
        terminateAllProgress()
        showAlertMessage(message)
    }

    private fun showAlertMessage(message: String) {
        recyclerStatusRecycler?.visibility = GONE
        txtAlertMessage?.visibility = VISIBLE
        txtAlertMessage?.text = message
    }

    private fun hideAlertMessage() {
        recyclerStatusRecycler?.visibility = VISIBLE
        txtAlertMessage?.visibility = GONE
    }

    private fun terminateAllProgress() {
        swpStatus?.isRefreshing = false
        rlvProgressContent?.visibility = GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_close -> {
                if (rlvProgressContent?.visibility == VISIBLE) return
                finish()
                overridePendingTransition(0,R.anim.fade_out)
            }
        }
    }

    override fun onNethStatusSelected(newStatus: String?, position: Int) {
        this.newStatus = newStatus

        val sdkActiveStatusChanged = setSdkActiveStatusAndReturnIfChanged(
            newStatus == PRESENCE_STATUS_DISABLED
        )

        delay = sdkActiveStatusChanged && (newStatus != PRESENCE_STATUS_DISABLED)

        when (newStatus) {
            PRESENCE_STATUS_CALL_FORWARD -> setStatusCallforward()
            PRESENCE_STATUS_DISABLED -> updateRecyclerAndTerminate()
            else -> setNethStatus()
        }
    }

    fun setSdkActiveStatusAndReturnIfChanged(toDisable: Boolean): Boolean {
        return if (toDisable) disableSdk() else enableSdk()
    }

    fun disableSdk(): Boolean {
        mProxyConfig?.apply {
            if (!registerEnabled()) {
                if (DEBUG)
                    Log.i(TAG, "Account SDK già disabilitato")
                return false
            }
            setSdkDisableStatus(true)
            return true
        }
        return false
    }

    fun enableSdk(): Boolean {
        mProxyConfig?.apply {
            if (registerEnabled()) {
                if (DEBUG)
                    Log.i(TAG, "Account SDK già abilitato")
                return false
            }
            setSdkDisableStatus(false)
            return true
        }
        return false
    }

    private fun setSdkDisableStatus(disable: Boolean) {
        mProxyConfig?.apply {
            edit()
            enableRegister(!disable)
            done()
        }
        if (DEBUG)
            Log.i(TAG , "Account SDK ${if (disable) "Disabilitato" else "Abilitato"}")
    }

    // Gestisce "callforward"
    private fun setStatusCallforward() {
        if (insertCellphoneAlertDialog?.isShowing == true) return

        val child: View = layoutInflater.inflate(R.layout.phone_number_edit_text, null)
        phoneNumberEditText = child.findViewById(R.id.edtText_insert_phone_number)

        insertCellphoneAlertDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.presence_alert_title))
            .setMessage(getString(R.string.presence_alert_message))
            .setView(child)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.presence_alert_ok), null)
            .setNegativeButton(getString(R.string.presence_alert_cancel)) { dialog, _ -> dialog.cancel() }
            .create()
        insertCellphoneAlertDialog?.setOnShowListener(this)
        insertCellphoneAlertDialog?.show()
    }

    private fun buildAlertDialog() : AlertDialog.Builder {
        return AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle(resources.getString(R.string.attention))
            .setMessage(resources.getString(R.string.incorrect_number_message))
            .setCancelable(false)
            .setPositiveButton(resources.getString(R.string.ok)) { dialog , _ ->
                dialog.dismiss()
            }
    }

    override fun onShow(dialog: DialogInterface?) {
        val button = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        button.setOnClickListener { view ->
            phoneNumberEditText?.text?.apply {
                val phoneNumber = phoneNumberEditText?.text.toString()
                if (phoneNumber.isNotEmpty() && phoneNumber.checkForwardingNumber()) {
                    setNethStatus(phoneNumber)
                    insertCellphoneAlertDialog?.dismiss()
                } else {
                    val alertDialog = buildAlertDialog()
                    alertDialog.show()
                }
            }
        }
    }

    // Gestisce gli altri stati
    private fun setNethStatus(number: String? = null) {
        val authToken = SharedPreferencesManager
            .getAuthtoken(this@PresenceStatusActivity)

        if (number.isNullOrEmpty())
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    userRestAPI
                        ?.setPresence(authToken, PresenceStatusRequest(newStatus))
                        ?.enqueue(setPresenceCallback)
                },
                if (delay) REQUEST_DELAY else 0
            )
        else {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    userRestAPI
                        ?.setPresence(authToken, PresenceStatusWithCodeRequest(newStatus, number))
                        ?.enqueue(setPresenceCallback)
                },
                if (delay) REQUEST_DELAY else 0
            )
        }

        if (DEBUG)
            Log.i(
                TAG,
                "setNethStatus ${if (delay) "with" else "without"} delay"
            )

        rlvProgressContent?.visibility = VISIBLE
    }

    // Aggiorna graficamente la lista degli stati
    private fun updateRecyclerAndTerminate() {
        (recyclerStatusRecycler?.adapter as PresenceStatusAdapter)
            .selected = newStatus

        recyclerStatusRecycler?.adapter?.apply {
            //notifica vecchia posizione
            selectedStatus?.let {
                status.indexOf(it)
            }?.apply { notifyItemChanged(this) }

            selectedStatus = newStatus

            //notifica nuova posizione
            newStatus?.let {
                status.indexOf(it)
            }?.apply { notifyItemChanged(this) }

            terminateSuccessfully()

        }
    }

    private fun terminateSuccessfully() {
        setResult(RESULT_OK)
        finish()
        overridePendingTransition(0,R.anim.fade_out)
    }

    private fun String.checkForwardingNumber(): Boolean =
         this.matches("[0-9]+".toRegex())

    companion object {
        const val STATUS_SELECTED = "status_selected"
        const val TAG = "PresenceStatusActivity"
        const val REQUEST_DELAY = 1000L
    }

}
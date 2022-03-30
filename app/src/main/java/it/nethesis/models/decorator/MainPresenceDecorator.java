package it.nethesis.models.decorator;


import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import org.linphone.R;


public class MainPresenceDecorator {

    private final String _mainPresence;

    public MainPresenceDecorator(String _mainPresence) {
        this._mainPresence = _mainPresence;
    }

    @DrawableRes
    public int getStatusSquareBackground() {
        if (_mainPresence == null)
            return R.drawable.ic_square_status_nd;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.drawable.ic_square_status_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_square_status_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_square_status_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_square_status_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_square_status_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.drawable.ic_square_status_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.drawable.ic_square_status_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_square_status_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.drawable.ic_square_status_nd;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_square_status_disabled;
        }
        return R.drawable.ic_square_status_nd;
    }

    @DrawableRes
    public int getStatusSquareSelectorBackground() {
        if (_mainPresence == null)
            return R.drawable.ic_square_status_nd_selector;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.drawable.ic_square_status_do_not_disturb_selector;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_square_status_voicemail_selector;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_square_status_cellphone_selector;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_square_status_call_forward_selector;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_square_status_online_selector;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.drawable.ic_square_status_incoming_selector;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.drawable.ic_square_status_busy_selector;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_square_status_offline_selector;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.drawable.ic_square_status_nd_selector;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_square_status_disabled_selector;
        }
        return R.drawable.ic_square_status_nd_selector;
    }

    @ColorRes
    public int getStatusBackgroundColor() {
        if (_mainPresence == null)
            return R.color.precense_status_call_nd;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.color.precense_status_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.color.precense_status_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.color.precense_status_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.color.precense_status_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.color.precense_status_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.color.precense_status_call_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.color.precense_status_call_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.color.precense_status_call_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.color.precense_status_call_nd;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.color.precense_status_call_disabled;
        }
        return R.color.precense_status_call_nd;
    }

    @StringRes
    public int getStatusLabel() {
        if (_mainPresence == null)
            return R.string.presence_satus_nd;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.string.presence_satus_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.string.presence_satus_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.string.presence_satus_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.string.presence_satus_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.string.presence_satus_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.string.presence_satus_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.string.presence_satus_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.string.presence_satus_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.string.presence_satus_nd;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.string.presence_status_disabled;
        }
        return R.string.presence_satus_nd;
    }

    @DrawableRes
    public int getSmallCircleStatusIcon() {
        if (_mainPresence == null)
            return R.drawable.ic_presence_status_nd;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.drawable.ic_presence_status_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_presence_status_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_presence_status_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_presence_status_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_presence_status_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.drawable.ic_presence_status_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.drawable.ic_presence_status_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_presence_status_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.drawable.ic_presence_status_nd;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_presence_status_disabled;
        }
        return R.drawable.ic_presence_status_nd;
    }

    @DrawableRes
    public int getBackgroundCircleStatusIcon() {
        if (_mainPresence == null)
            return R.drawable.ic_presence_status_border_nd;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.drawable.ic_presence_status_border_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_presence_status_border_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_presence_status_border_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_presence_status_border_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_presence_status_border_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.drawable.ic_presence_status_border_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.drawable.ic_presence_status_border_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_presence_status_border_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.drawable.ic_presence_status_border_nd;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_presence_status_border_disabled;
        }
        return R.drawable.ic_presence_status_border_nd;
    }

    @DrawableRes //Todo da testare
    public int getBackgroundCircleStatusMeIcon() {
        if (_mainPresence == null)
            return 0;
        switch (_mainPresence) {
            case MAIN_PRESENCE_STATUS_DND:
                return R.drawable.ic_presence_header_status_do_not_disturb;
            case MAIN_PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_presence_header_status_voicemail;
            case MAIN_PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_presence_header_status_cellphone;
            case MAIN_PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_presence_header_status_call_forward;
            case MAIN_PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_presence_header_status_online;
            case MAIN_PRESENCE_STATUS_RINGING:
                return R.drawable.ic_presence_header_status_incoming;
            case MAIN_PRESENCE_STATUS_BUSY:
                return R.drawable.ic_presence_header_status_busy;
            case MAIN_PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_presence_header_status_offline;
            case MAIN_PRESENCE_STATUS_N_D:
                return R.drawable.ic_presence_header_status_offline;
            case MAIN_PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_presence_header_status_disabled;
        }
        return 0;
    }

    private static final String MAIN_PRESENCE_STATUS_DND = "dnd";
    private static final String MAIN_PRESENCE_STATUS_VOICEMAIL = "voicemail";
    private static final String MAIN_PRESENCE_STATUS_CELLPHONE = "cellphone";
    private static final String MAIN_PRESENCE_STATUS_CALL_FORWARD = "callforward";
    private static final String MAIN_PRESENCE_STATUS_ONLINE = "online";
    private static final String MAIN_PRESENCE_STATUS_RINGING = "ringing";
    private static final String MAIN_PRESENCE_STATUS_BUSY = "busy";
    public static final String MAIN_PRESENCE_STATUS_OFFLINE = "offline";
    public static final String MAIN_PRESENCE_STATUS_N_D = "n_d";
    public static final String MAIN_PRESENCE_STATUS_DISABLED = "disabled";
}

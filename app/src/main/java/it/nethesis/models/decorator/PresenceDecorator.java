package it.nethesis.models.decorator;


import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.linphone.R;


public class PresenceDecorator {

    private final String _presence;

    public PresenceDecorator(String presence) {
        this._presence = presence;
    }

    @DrawableRes
    public int getStatusSquareSelectorBackground() {
        if (_presence == null)
            return R.drawable.ic_square_status_nd_selector;
        switch (_presence) {
            case PRESENCE_STATUS_DND:
                return R.drawable.ic_square_status_do_not_disturb_selector;
            case PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_square_status_voicemail_selector;
            case PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_square_status_cellphone_selector;
            case PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_square_status_call_forward_selector;
            case PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_square_status_online_selector;
            case PRESENCE_STATUS_RINGING:
                return R.drawable.ic_square_status_incoming_selector;
            case PRESENCE_STATUS_BUSY:
                return R.drawable.ic_square_status_busy_selector;
            case PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_square_status_offline_selector;
            case PRESENCE_STATUS_N_D:
                return R.drawable.ic_square_status_nd_selector;
            case PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_square_status_disabled_selector;
        }
        return R.drawable.ic_square_status_nd_selector;
    }

    @DrawableRes
    public int getSmallCircleStatusIcon() {
        if (_presence == null)
            return R.drawable.ic_presence_status_nd;
        switch (_presence) {
            case PRESENCE_STATUS_DND:
                return R.drawable.ic_presence_status_do_not_disturb;
            case PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_presence_status_voicemail;
            case PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_presence_status_cellphone;
            case PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_presence_status_call_forward;
            case PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_presence_status_online;
            case PRESENCE_STATUS_RINGING:
                return R.drawable.ic_presence_status_incoming;
            case PRESENCE_STATUS_BUSY:
                return R.drawable.ic_presence_status_busy;
            case PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_presence_status_offline;
            case PRESENCE_STATUS_N_D:
                return R.drawable.ic_presence_status_nd;
            case PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_presence_status_disabled;
        }
        return R.drawable.ic_presence_status_nd;
    }

    @StringRes
    public int getStatusLabel() {
        if (_presence == null)
            return R.string.presence_satus_nd;
        switch (_presence) {
            case PRESENCE_STATUS_DND:
                return R.string.presence_satus_do_not_disturb;
            case PRESENCE_STATUS_VOICEMAIL:
                return R.string.presence_satus_voicemail;
            case PRESENCE_STATUS_CELLPHONE:
                return R.string.presence_satus_cellphone;
            case PRESENCE_STATUS_CALL_FORWARD:
                return R.string.presence_satus_call_forward;
            case PRESENCE_STATUS_ONLINE:
                return R.string.presence_satus_online;
            case PRESENCE_STATUS_RINGING:
                return R.string.presence_satus_incoming;
            case PRESENCE_STATUS_BUSY:
                return R.string.presence_satus_busy;
            case PRESENCE_STATUS_OFFLINE:
                return R.string.presence_satus_offline;
            case PRESENCE_STATUS_N_D:
                return R.string.presence_satus_nd;
            case PRESENCE_STATUS_DISABLED:
                return R.string.presence_status_disabled;
        }
        return R.string.presence_satus_nd;
    }

    @DrawableRes //Todo da testare
    public int getBackgroundCircleStatusMeIcon() {
        if (_presence == null)
            return 0;
        switch (_presence) {
            case PRESENCE_STATUS_DND:
                return R.drawable.ic_presence_header_status_do_not_disturb;
            case PRESENCE_STATUS_VOICEMAIL:
                return R.drawable.ic_presence_header_status_voicemail;
            case PRESENCE_STATUS_CELLPHONE:
                return R.drawable.ic_presence_header_status_cellphone;
            case PRESENCE_STATUS_CALL_FORWARD:
                return R.drawable.ic_presence_header_status_call_forward;
            case PRESENCE_STATUS_ONLINE:
                return R.drawable.ic_presence_header_status_online;
            case PRESENCE_STATUS_RINGING:
                return R.drawable.ic_presence_header_status_incoming;
            case PRESENCE_STATUS_BUSY:
                return R.drawable.ic_presence_header_status_busy;
            case PRESENCE_STATUS_OFFLINE:
                return R.drawable.ic_presence_header_status_offline;
            case PRESENCE_STATUS_N_D:
                return R.drawable.ic_presence_header_status_nd;
            case PRESENCE_STATUS_DISABLED:
                return R.drawable.ic_presence_header_status_disabled;
        }
        return 0;
    }

    public static final String PRESENCE_STATUS_DND = "dnd";
    public static final String PRESENCE_STATUS_VOICEMAIL = "voicemail";
    public static final String PRESENCE_STATUS_CELLPHONE = "cellphone";
    public static final String PRESENCE_STATUS_CALL_FORWARD = "callforward";
    public static final String PRESENCE_STATUS_ONLINE = "online";
    public static final String PRESENCE_STATUS_RINGING = "ringing";
    public static final String PRESENCE_STATUS_BUSY = "busy";
    public static final String PRESENCE_STATUS_OFFLINE = "offline";
    public static final String PRESENCE_STATUS_N_D = "n_d";
    public static final String PRESENCE_STATUS_DISABLED = "disabled";
}

package org.linphone.presence;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.linphone.NethCTIApplication;
import org.linphone.R;
import org.linphone.interfaces.OnUserActionListener;
import it.nethesis.models.NethProfile;
import it.nethesis.models.NethUser;
import it.nethesis.models.decorator.MainPresenceDecorator;
import it.nethesis.models.presence.PresenceUser;

class PresenceActionsBottomDialog extends BottomSheetDialog implements View.OnClickListener {

    public static final int ACTION_CLOSE_POPUP = -1;
    public static final int ACTION_CALL = 0;
    public static final int ACTION_HANGUP = 1;
    public static final int ACTION_RECORD = 2;
    public static final int ACTION_INTRUDE = 3;
    public static final int ACTION_BOOK = 4;
    public static final int ACTION_PICKUP = 5;
    public static final int ACTION_SPY = 6;

    private ImageView star;
    private boolean starInitState;
    //private int bottomSheetState = -1;

    private PresenceUser _presenceUser;
    private NethUser _nethUser;
    private OnUserActionListener onUserActionListener;

    private ImageView imgClose;
    private ImageView imgDlgBackgroundCircleBorder;
    private ImageView imgDlgSmallStatusICon;
    private TextView txtDlgInitials;
    private TextView txtDlgPresenceStatus;
    private TextView txtDlgUsername;
    private TextView txtMainExtensionId;

    private TextView btnCall;
    private TextView btnClose;
    private TextView btnRecord;
    private TextView btnIntrude;
    private TextView btnBook;
    private TextView btnPickup;
    private TextView btnSpy;

    private LinearLayout linearContentActions;

    private BottomSheetBehavior mBehavior;

    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback =
            new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                if (mBehavior == null) return;
                mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            /*bottomSheetState = newState;
            // React to state change
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                //STATE_COLLAPSED
                Log.e("newState", "STATE_COLLAPSED");
            }else{
                //STATE_EXPANDED;
                Log.e("newState", "STATE_EXPANDED");
            }*/
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            // React to dragging events
            /*Log.e("slideOffset", slideOffset + "");
            if (bottomSheetState == BottomSheetBehavior.STATE_DRAGGING) {
                if (slideOffset < -0.01) dismiss();
            }*/
        }
    };

    public PresenceActionsBottomDialog(
            @NonNull Context context,
            PresenceUser presenceUser,
            NethUser nethUser,
            OnUserActionListener onUserActionListener
    ) {
        super(context);

        _presenceUser = presenceUser;
        _nethUser = nethUser;
        this.onUserActionListener = onUserActionListener;

        View view = View.inflate(
                getContext(),
                R.layout.presence_detail_bottom_sheet_dialog,
                null
        );

        initUi(view);
        setDayNightThemeColor();
        initUiContent();
        setContentView(view);
        initListeners();

        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.addBottomSheetCallback(bottomSheetCallback);
        mBehavior.setPeekHeight(10000);
    }

    private void initListeners() {
        imgClose.setOnClickListener(this);
        star.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        btnIntrude.setOnClickListener(this);
        btnSpy.setOnClickListener(this);
        btnPickup.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
    }

    private void initUi(View view) {
        linearContentActions = view.findViewById(R.id.lnr_content_actions);
        imgClose = view.findViewById(R.id.img_close);

        txtDlgUsername = view.findViewById(R.id.txt_dlg_username);
        txtDlgInitials = view.findViewById(R.id.txt_dlg_user_initials);
        imgDlgSmallStatusICon =  view.findViewById(R.id.img_dlg_small_status_icon);
        imgDlgBackgroundCircleBorder =
                view.findViewById(R.id.img_dlg_background_border_presence_status);
        txtDlgPresenceStatus = view.findViewById(R.id.txt_dlg_status);
        txtMainExtensionId = view.findViewById(R.id.txt_main_extension_id);

        starInitState = _presenceUser.checkIfSelected(getContext());
        star = view.findViewById(R.id.img_favorite);
        star.setSelected(starInitState);

        btnCall = view.findViewById(R.id.btn_call);
        btnClose = view.findViewById(R.id.btn_close);
        btnBook = view.findViewById(R.id.btn_book);
        btnIntrude = view.findViewById(R.id.btn_intrude);
        btnSpy = view.findViewById(R.id.btn_spy);
        btnPickup = view.findViewById(R.id.btn_pickup);
        btnRecord = view.findViewById(R.id.btn_record);
    }

    private void setDayNightThemeColor() {
        boolean darkMode = NethCTIApplication
                .Companion
                .isNightTheme();

        int color = darkMode
                ? R.color.ic_presence_color_gray_text_selector_dark
                : R.color.ic_presence_color_gray_text_selector;

        btnCall.setTextColor(getContext().getColorStateList(color));
        btnClose.setTextColor(getContext().getColorStateList(color));
        btnRecord.setTextColor(getContext().getColorStateList(color));
        btnIntrude.setTextColor(getContext().getColorStateList(color));
        btnBook.setTextColor(getContext().getColorStateList(color));
        btnPickup.setTextColor(getContext().getColorStateList(color));
        btnSpy.setTextColor(getContext().getColorStateList(color));

        int backgroundColor = darkMode ? R.color.black_color : R.color.white_color;
        linearContentActions.setBackgroundColor(backgroundColor);
        linearContentActions.setBackgroundTintList(getContext().getResources().getColorStateList(backgroundColor, null));

    }

    private void initUiContent() {
        MainPresenceDecorator decorator = new MainPresenceDecorator(_presenceUser.mainPresence);
        txtDlgUsername.setText(_presenceUser.name);
        txtDlgInitials.setText(_presenceUser.getInitials());
        txtMainExtensionId.setText(_presenceUser.endpoints.getMainExtensionLocalizedId(getContext()));

        imgDlgSmallStatusICon.setImageResource(decorator.getSmallCircleStatusIcon());
        imgDlgBackgroundCircleBorder.setImageResource(decorator.getBackgroundCircleStatusIcon());

        txtDlgPresenceStatus.setText(decorator.getStatusLabel());
        Drawable backgroundPresenceSquare = ContextCompat
                .getDrawable(getContext(), decorator.getStatusSquareBackground());
        txtDlgPresenceStatus.setBackground(backgroundPresenceSquare);
        txtDlgPresenceStatus.setPadding(8, 3, 8, 3);

        setCallActions();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (starInitState == star.isSelected()) return;
        onUserActionListener.onFavoritePressed(star.isSelected(), _presenceUser);
    }

    private void setCallActions(){
        // permesso "recallOnBusy" && mainPresence dell'utente bersaglio in "busy"
        boolean isBookEnable = _nethUser.haveRecallOnBusyPermission()
                && PresenceUser.getActionBusy(_presenceUser.mainPresence);

        // permesso "spy" && mainPresence dell'utente bersaglio in "busy"
        boolean isSpyEnable = _nethUser.havePermission(NethProfile.PERMISSION_SPY)
                && PresenceUser.getActionBusy(_presenceUser.mainPresence);

        // permesso "intrude" && mainPresence dell'utente bersaglio in "busy"
        boolean isIntrudeEnable = _nethUser.havePermission(NethProfile.PERMISSION_INTRUDE)
                && PresenceUser.getActionBusy(_presenceUser.mainPresence);

        // permesso "ad_recording" && mainPresence dell'utente bersaglio in "busy"
        boolean isRecordEnable = _nethUser.havePermission(NethProfile.PERMISSION_RECORD)
                && PresenceUser.getActionBusy(_presenceUser.mainPresence);

        // permesso "pickup" && mainPresence dell'utente bersaglio in "incoming"
        boolean isPickupEnable = _nethUser.havePermission(NethProfile.PERMISSION_PICKUP)
                && PresenceUser.getActionIncoming(_presenceUser.mainPresence);

        // permesso "hangup" && mainPresence dell'utente bersaglio in "incoming || busy"
        boolean isCloseEnable = _nethUser.havePermission(NethProfile.PERMISSION_HANGUP)
                && (PresenceUser.getActionBusy(_presenceUser.mainPresence) || PresenceUser.getActionIncoming(_presenceUser.mainPresence));

        // utente bersaglio in "online || cellphone || voicemail || callforward"
        boolean isCallEnable = PresenceUser.getActionCall(_presenceUser.mainPresence);

        btnBook.setEnabled(isBookEnable);
        btnSpy.setEnabled(isSpyEnable);
        btnIntrude.setEnabled(isIntrudeEnable);
        btnRecord.setEnabled(isRecordEnable);
        btnPickup.setEnabled(isPickupEnable);
        btnClose.setEnabled(isCloseEnable);
        btnCall.setEnabled(isCallEnable);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case (R.id.img_favorite):
                star.setSelected(!star.isSelected());
                break;
            case (R.id.img_close):
                onUserActionListener.onActionButtonPressed(ACTION_CLOSE_POPUP, null);
                break;
            case (R.id.btn_call):
                onUserActionListener.onActionButtonPressed(ACTION_CALL, _presenceUser);
                break;
            case (R.id.btn_close):
                onUserActionListener.onActionButtonPressed(ACTION_HANGUP, _presenceUser);
                break;
            case (R.id.btn_record):
                onUserActionListener.onActionButtonPressed(ACTION_RECORD, _presenceUser);
                break;
            case (R.id.btn_intrude):
                onUserActionListener.onActionButtonPressed(ACTION_INTRUDE, _presenceUser);
                break;
            case (R.id.btn_book):
                onUserActionListener.onActionButtonPressed(ACTION_BOOK, _presenceUser);
                break;
            case (R.id.btn_pickup):
                onUserActionListener.onActionButtonPressed(ACTION_PICKUP, _presenceUser);
                break;
            case (R.id.btn_spy):
                onUserActionListener.onActionButtonPressed(ACTION_SPY, _presenceUser);
                break;
        }
    }
}

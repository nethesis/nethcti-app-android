package org.linphone.presence;

import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_BOOK;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_HANGUP;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_INTRUDE;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_PICKUP;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_RECORD;
import static org.linphone.presence.PresenceActionsBottomDialog.ACTION_SPY;

import android.content.Context;
import org.linphone.interfaces.OnActionResul;
import org.linphone.utils.SharedPreferencesManager;
import java.util.HashMap;
import it.nethesis.models.AstproxyExtensions;
import it.nethesis.models.Conversation;
import it.nethesis.models.Extension;
import it.nethesis.models.NethUser;
import it.nethesis.models.actionCall.CallerAndCalled;
import it.nethesis.models.actionCall.RecordActionRequest;
import it.nethesis.models.actionCall.RedirectRequest;
import it.nethesis.models.actionCall.HangupActionRequest;
import it.nethesis.models.actionCall.PickupActionRequest;
import it.nethesis.models.presence.PresenceUser;
import it.nethesis.webservices.ActionCallRestAPI;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PresenceActionManager {

    private final Context _context;
    private final OnActionResul _onActionResul;
    private final ActionCallRestAPI _actionCallRestAPI;

    public PresenceActionManager(
            Context context,
            OnActionResul onActionResul,
            ActionCallRestAPI actionCallRestAPI
    ) {
        _context = context;
        _onActionResul = onActionResul;
        _actionCallRestAPI = actionCallRestAPI;
    }


    private Conversation getValidConversation(
            PresenceUser target,
            HashMap<String, AstproxyExtensions> extensions
    ) {
        for (Extension extension : target.endpoints.extension) {
            AstproxyExtensions astproxyExtensions = extensions.get(extension.id);
            if (astproxyExtensions == null
                    || astproxyExtensions.conversations == null
                    || astproxyExtensions.conversations.isEmpty()) continue;
            Conversation conversation = (Conversation) astproxyExtensions
                    .conversations
                    .values()
                    .toArray()[0];
            if (conversation.id != null && !conversation.id.isEmpty())
                return conversation;
        }
        return null;
    }

    /** Book */
    public void sendBookRequest(NethUser nethUser, PresenceUser target) {
        _actionCallRestAPI.recallOnBusy(
                SharedPreferencesManager.getAuthtoken(_context),
                new CallerAndCalled(
                        nethUser.endpoints.getMainExtensionId(),
                        target.endpoints.getMainExtensionId()
                )
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    _onActionResul.onSuccess();
                } else {
                    _onActionResul.onFailure(ACTION_BOOK);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                _onActionResul.onFailure(ACTION_BOOK);
            }
        });

    }

    /** Spy */
    public void sendSpyRequest(NethUser nethUser, PresenceUser target) {
        String token = SharedPreferencesManager.getAuthtoken(_context);
        _actionCallRestAPI.getExtensions(token).enqueue(
                new Callback<HashMap<String, AstproxyExtensions>>() {
                    @Override
                    public void onResponse(
                            Call<HashMap<String, AstproxyExtensions>> call,
                            Response<HashMap<String, AstproxyExtensions>> response
                    ) {
                        if (response.isSuccessful()) {
                            String convId = "";
                            String endpointId = "";
                            try {
                                Conversation conversation = getValidConversation(target, response.body());
                                convId = conversation != null ? conversation.id : "";
                                if (conversation != null) {
                                    endpointId = conversation.owner;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RedirectRequest redirectRequest = new RedirectRequest(
                                    convId,
                                    endpointId,
                                    nethUser.endpoints.getMobileExtensionId()
                            );
                            _actionCallRestAPI.startSpy(
                                    token,
                                    redirectRequest
                            ).enqueue(spyCallback);
                        } else _onActionResul.onFailure(ACTION_SPY);

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, AstproxyExtensions>> call, Throwable t) {
                        _onActionResul.onFailure(ACTION_SPY);
                    }
                }
        );
    }

    private final Callback<ResponseBody> spyCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                _onActionResul.onSuccess();
            } else {
                _onActionResul.onFailure(ACTION_SPY);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            _onActionResul.onFailure(ACTION_HANGUP);
        }
    };

    /** Intrude */
    public void sendIntrudeRequest(NethUser nethUser, PresenceUser target) {
        String token = SharedPreferencesManager.getAuthtoken(_context);
        _actionCallRestAPI.getExtensions(token).enqueue(
                new Callback<HashMap<String, AstproxyExtensions>>() {
                    @Override
                    public void onResponse(
                            Call<HashMap<String, AstproxyExtensions>> call,
                            Response<HashMap<String, AstproxyExtensions>> response
                    ) {
                        if (response.isSuccessful()) {
                            String convId = "";
                            String endpointId = "";
                            try {
                                Conversation conversation = getValidConversation(target, response.body());
                                convId = conversation != null ? conversation.id : "";
                                if (conversation != null) {
                                    endpointId = conversation.owner;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RedirectRequest redirectRequest = new RedirectRequest(
                                    convId,
                                    endpointId,
                                    nethUser.endpoints.getMobileExtensionId()
                            );
                            _actionCallRestAPI.intrude(
                                    token,
                                    redirectRequest
                            ).enqueue(intrudeCallback);
                        } else _onActionResul.onFailure(ACTION_INTRUDE);

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, AstproxyExtensions>> call, Throwable t) {
                        _onActionResul.onFailure(ACTION_INTRUDE);
                    }
                }
        );
    }

    private final Callback<ResponseBody> intrudeCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                _onActionResul.onSuccess();
            } else {
                _onActionResul.onFailure(ACTION_INTRUDE);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            _onActionResul.onFailure(ACTION_INTRUDE);
        }
    };

    /** Record Start*/
    public void sendStartRecordRequest(PresenceUser target) {
        String token = SharedPreferencesManager.getAuthtoken(_context);
        _actionCallRestAPI.getExtensions(token).enqueue(
                new Callback<HashMap<String, AstproxyExtensions>>() {
                    @Override
                    public void onResponse(
                            Call<HashMap<String, AstproxyExtensions>> call,
                            Response<HashMap<String, AstproxyExtensions>> response
                    ) {
                        if (response.isSuccessful()) {
                            String convId = "";
                            String endpointId = "";
                            try {
                                Conversation conversation = getValidConversation(target, response.body());
                                convId = conversation != null ? conversation.id : "";
                                if (conversation != null) {
                                    endpointId = conversation.owner;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RecordActionRequest recordActionRequest = new RecordActionRequest(
                                    convId,
                                    "extension",
                                    endpointId
                            );
                            _actionCallRestAPI.unmuteRecord(
                                    token,
                                    recordActionRequest
                            ).enqueue(recordStartCallback);
                        } else _onActionResul.onFailure(ACTION_RECORD);

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, AstproxyExtensions>> call, Throwable t) {
                        _onActionResul.onFailure(ACTION_RECORD);
                    }
                }
        );
    }

    private final Callback<ResponseBody> recordStartCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                _onActionResul.onSuccess();
            } else {
                _onActionResul.onFailure(ACTION_RECORD);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            _onActionResul.onFailure(ACTION_RECORD);
        }
    };

    /** TODO: Record End */
    public void sendRecordEndRequest(PresenceUser target) {
        String token = SharedPreferencesManager.getAuthtoken(_context);
        _actionCallRestAPI.getExtensions(token).enqueue(
                new Callback<HashMap<String, AstproxyExtensions>>() {
                    @Override
                    public void onResponse(
                            Call<HashMap<String, AstproxyExtensions>> call,
                            Response<HashMap<String, AstproxyExtensions>> response
                    ) {
                        if (response.isSuccessful()) {
                            String convId = "";
                            String endpointId = "";
                            try {
                                Conversation conversation = getValidConversation(target, response.body());
                                convId = conversation != null ? conversation.id : "";
                                if (conversation != null) {
                                    endpointId = conversation.owner;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            RecordActionRequest recordActionRequest = new RecordActionRequest(
                                    convId,
                                    "extension",
                                    endpointId
                            );
                            _actionCallRestAPI.unmuteRecord(
                                    token,
                                    recordActionRequest
                            ).enqueue(recordEndCallback);
                        } else _onActionResul.onFailure(ACTION_RECORD);

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, AstproxyExtensions>> call, Throwable t) {
                        _onActionResul.onFailure(ACTION_RECORD);
                    }
                }
        );
    }

    private final Callback<ResponseBody> recordEndCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                _onActionResul.onSuccess();
            } else {
                _onActionResul.onFailure(ACTION_RECORD);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            _onActionResul.onFailure(ACTION_RECORD);
        }
    };

    /** Pickup */
    public void sendPickupRequest(NethUser nethUser, PresenceUser target) {
        _actionCallRestAPI.pickup(
                SharedPreferencesManager.getAuthtoken(_context),
                new PickupActionRequest(
                        target.endpoints.getMainExtensionId(),
                        nethUser.endpoints.getMobileExtensionId()
                )
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    _onActionResul.onSuccess();
                } else {
                    _onActionResul.onFailure(ACTION_PICKUP);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                _onActionResul.onFailure(ACTION_PICKUP);
            }
        });

    }

    /** Hangup */
    public void sendHangupRequest(NethUser nethUser, PresenceUser target) {
        String token = SharedPreferencesManager.getAuthtoken(_context);
        _actionCallRestAPI.getExtensions(token).enqueue(
                new Callback<HashMap<String, AstproxyExtensions>>() {
                    @Override
                    public void onResponse(
                            Call<HashMap<String, AstproxyExtensions>> call,
                            Response<HashMap<String, AstproxyExtensions>> response
                    ) {
                        if (response.isSuccessful()) {
                            String convId = "";
                            String endpointId = "";
                            try {
                                Conversation conversation = getValidConversation(target, response.body());
                                convId = conversation != null ? conversation.id : "";
                                if (conversation != null)
                                    endpointId = conversation.owner;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            _actionCallRestAPI.hangup(
                                    token,
                                    new HangupActionRequest(convId, endpointId)
                            ).enqueue(hangupCallback);
                        } else _onActionResul.onFailure(ACTION_HANGUP);

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, AstproxyExtensions>> call, Throwable t) {
                        _onActionResul.onFailure(ACTION_HANGUP);
                    }
                }
        );
    }

    private final Callback<ResponseBody> hangupCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                _onActionResul.onSuccess();
            } else {
                _onActionResul.onFailure(ACTION_HANGUP);
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            _onActionResul.onFailure(ACTION_HANGUP);
        }
    };

}

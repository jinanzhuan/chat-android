package com.hyphenate.chatuidemo.common;

import com.hyphenate.easeui.constants.EaseConstant;

public interface DemoConstant extends EaseConstant {
    String EXTRA_CONFERENCE_ID = "confId";
    String EXTRA_CONFERENCE_PASS = "password";
    String EXTRA_CONFERENCE_INVITER = "inviter";
    String EXTRA_CONFERENCE_IS_CREATOR = "is_creator";
    String EXTRA_CONFERENCE_GROUP_ID = "group_id";

    String OP_INVITE = "invite";
    String OP_REQUEST_TOBE_SPEAKER = "request_tobe_speaker";
    String OP_REQUEST_TOBE_AUDIENCE = "request_tobe_audience";

    String EM_CONFERENCE_OP = "em_conference_op";
    String EM_CONFERENCE_ID = "em_conference_id";
    String EM_CONFERENCE_PASSWORD = "em_conference_password";
    String EM_CONFERENCE_TYPE = "em_conference_type";
    String EM_MEMBER_NAME = "em_member_name";

    String MSG_ATTR_CONF_ID = "conferenceId";
    String MSG_ATTR_CONF_PASS = EXTRA_CONFERENCE_PASS;
    String MSG_ATTR_EXTENSION = "msg_extension";

    String NEW_FRIENDS_USERNAME = "item_new_friends";
    String GROUP_USERNAME = "item_groups";
    String CHAT_ROOM = "item_chatroom";
    String CHAT_ROBOT = "item_robots";

}
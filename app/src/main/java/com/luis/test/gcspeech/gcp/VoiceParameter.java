package com.luis.test.gcspeech.gcp;

import org.json.JSONObject;

/**
 * Author: Changemyminds.
 * Date: 2018/12/27.
 * Description:
 * Reference:
 */
public interface VoiceParameter {
    String getJSONHeader();
    JSONObject toJSONObject();
}

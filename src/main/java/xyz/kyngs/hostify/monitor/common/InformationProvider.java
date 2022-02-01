package xyz.kyngs.hostify.monitor.common;

import com.google.gson.JsonObject;

public interface InformationProvider {

    void populate(JsonObject object);

}

/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.notification.requests;

import com.example.notification.PluginRequest;
import com.example.notification.RequestExecutor;
import com.example.notification.executors.StageStatusRequestExecutor;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StageStatusRequest {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DefaultDateTypeAdapter(DATE_PATTERN))
            .create();

    public Pipeline pipeline;

    public static StageStatusRequest fromJSON(String json) {
        return GSON.fromJson(json, StageStatusRequest.class);
    }

    public RequestExecutor executor(PluginRequest pluginRequest) {
        return new StageStatusRequestExecutor(this, pluginRequest);
    }

    public static class Pipeline {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("group")
        public String group;

        @SerializedName("build-cause")
        public List<BuildCause> buildCause;

        @SerializedName("stage")
        public Stage stage;
    }

    public static class BuildCause {
        @SerializedName("material")
        public Map material;

        @SerializedName("changed")
        public Boolean changed;

        @SerializedName("modifications")
        public List<Modification> modifications;
    }

    public static class Stage {
        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public String counter;

        @SerializedName("approval-type")
        public String approvalType;

        @SerializedName("approved-by")
        public String approvedBy;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("create-time")
        public Date createTime;

        @SerializedName("last-transition-time")
        public Date lastTransitionTime;
        public List<Job> jobs;
    }

    public static class Job {
        @SerializedName("name")
        public String name;

        @SerializedName("schedule-time")
        public Date scheduleTime;

        @SerializedName("complete-time")
        public Date completeTime;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

        @SerializedName("agent-uuid")
        public String agentUuid;
    }

    public static class Modification {
        @SerializedName("revision")
        public String revision;

        @SerializedName("modified-time")
        public Date modifiedTime;

        @SerializedName("data")
        public Map data;
    }

    // Copy of the adapter from gson, to deal with dates rendered as a blank string, instead of a null
    static final class DefaultDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

        private final DateFormat enUsFormat;
        private final DateFormat localFormat;

        DefaultDateTypeAdapter(String datePattern) {
            this(new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
        }

        DefaultDateTypeAdapter(DateFormat enUsFormat, DateFormat localFormat) {
            this.enUsFormat = enUsFormat;
            this.localFormat = localFormat;
        }

        // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
        // See issue 162
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            synchronized (localFormat) {
                String dateFormatAsString = enUsFormat.format(src);
                return new JsonPrimitive(dateFormatAsString);
            }
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (!(json instanceof JsonPrimitive)) {
                throw new JsonParseException("The date should be a string value");
            }
            Date date = deserializeToDate(json);
            if (typeOfT == Date.class) {
                return date;
            } else if (typeOfT == Timestamp.class) {
                return new Timestamp(date.getTime());
            } else if (typeOfT == java.sql.Date.class) {
                return new java.sql.Date(date.getTime());
            } else {
                throw new IllegalArgumentException(getClass() + " cannot deserialize to " + typeOfT);
            }
        }

        private Date deserializeToDate(JsonElement json) {
            synchronized (localFormat) {
                if (json.getAsString().isEmpty()) {
                    return null;
                }
                try {
                    return localFormat.parse(json.getAsString());
                } catch (ParseException ignored) {
                }
                try {
                    return enUsFormat.parse(json.getAsString());
                } catch (ParseException ignored) {
                }
                try {
                    return ISO8601Utils.parse(json.getAsString(), new ParsePosition(0));
                } catch (ParseException e) {
                    throw new JsonSyntaxException(json.getAsString(), e);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(DefaultDateTypeAdapter.class.getSimpleName());
            sb.append('(').append(localFormat.getClass().getSimpleName()).append(')');
            return sb.toString();
        }
    }
}

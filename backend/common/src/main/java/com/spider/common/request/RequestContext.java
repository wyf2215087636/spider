package com.spider.common.request;

public final class RequestContext {
    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> ACTOR_HOLDER = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID_HOLDER.get();
    }

    public static void setActor(String actor) {
        ACTOR_HOLDER.set(actor);
    }

    public static String getActor() {
        String actor = ACTOR_HOLDER.get();
        return actor == null || actor.isBlank() ? "system" : actor;
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
        ACTOR_HOLDER.remove();
    }
}

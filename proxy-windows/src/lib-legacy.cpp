#include <cstring>
#include <deque>
#include <mutex>
#include <vector>

#include "event.hpp"
#include "input.hpp"
#include "lib-legacy.hpp"
#include "protocol.hpp"

extern std::mutex g_event_queue_mutex;
extern std::deque<ProxyMessage> g_event_queue;

extern "C" {
JNIEXPORT void JNICALL
Java_top_fifthlight_touchcontroller_common_platform_win32_Interface_init(
    JNIEnv* env, jclass clazz, jlong window_handle) {
    try {
        init(reinterpret_cast<HWND>(window_handle));
    } catch (const InitializeError& e) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), e.what());
    }
}

JNIEXPORT jint JNICALL
Java_top_fifthlight_touchcontroller_common_platform_win32_Interface_pollEvent(
    JNIEnv* env, jclass clazz, jbyteArray buffer) {
    std::optional<std::vector<uint8_t>> event =
        touchcontroller::event::poll_event();
    if (!event.has_value()) {
        return 0;
    }

    std::vector<uint8_t> msg_buffer = event.value();

    jsize buffer_length = env->GetArrayLength(buffer);
    if (msg_buffer.size() > static_cast<size_t>(buffer_length)) {
        env->ThrowNew(env->FindClass("java/lang/Exception"), "Buffer overflow");
        return 0;
    }

    env->SetByteArrayRegion(buffer, 0, static_cast<jsize>(msg_buffer.size()),
                            reinterpret_cast<const jbyte*>(msg_buffer.data()));
    return static_cast<jint>(msg_buffer.size());
}

JNIEXPORT void JNICALL
Java_top_fifthlight_touchcontroller_common_platform_win32_Interface_pushEvent(
    JNIEnv* env, jclass clazz, jbyteArray buffer, jint length) {
    std::vector<uint8_t> data;
    data.resize(length);
    env->GetByteArrayRegion(buffer, 0, length,
                            reinterpret_cast<jbyte*>(data.data()));

    ProxyMessage message;
    if (touchcontroller::protocol::deserialize_event(message, data)) {
        switch (message.type) {
            case ProxyMessage::Vibrate: {
                break;
            }
            case ProxyMessage::KeyboardShow: {
                // TODO
                break;
            }
            default:
                break;
        }
    }
}
}
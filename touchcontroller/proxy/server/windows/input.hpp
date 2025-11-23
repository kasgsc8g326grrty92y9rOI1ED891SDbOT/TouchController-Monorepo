// input.hpp
#pragma once
#include <windows.h>

#include <deque>
#include <mutex>
#include <stdexcept>

#include "touchcontroller/proxy/server/common/protocol.hpp"

struct InitializeError : public std::runtime_error {
    using std::runtime_error::runtime_error;
};

struct EventError : public std::runtime_error {
    using std::runtime_error::runtime_error;
};

extern std::mutex g_event_queue_mutex;
extern std::deque<ProxyMessage> g_event_queue;

void init(HWND handle);

#ifndef TGNET_ACCOUNT_INSTANCE_REGISTRY_H
#define TGNET_ACCOUNT_INSTANCE_REGISTRY_H

#include <cassert>
#include <cstdint>
#include <map>
#include <memory>
#include <mutex>

// Stable instance addresses and account IDs, allocated only when first needed.
template <typename T>
class AccountInstanceRegistry {
public:
    T &get(int32_t account) {
        assert(account >= 0);
        std::lock_guard<std::mutex> lock(mutex);
        auto &instance = instances[account];
        if (!instance) {
            instance = std::make_unique<T>(account);
        }
        return *instance;
    }

private:
    std::mutex mutex;
    std::map<int32_t, std::unique_ptr<T>> instances;
};

#endif

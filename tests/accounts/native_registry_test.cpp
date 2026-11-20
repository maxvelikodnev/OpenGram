#include "AccountInstanceRegistry.h"
#include <atomic>
#include <cassert>
#include <iostream>
#include <thread>
#include <vector>

struct AccountConnection {
    explicit AccountConnection(int32_t account) : account(account) { ++created; }
    int32_t account;
    static std::atomic<int> created;
};
std::atomic<int> AccountConnection::created{0};

int main() {
    AccountInstanceRegistry<AccountConnection> registry;
    auto *first = &registry.get(0);
    for (int account = 0; account < 10000; ++account) {
        assert(registry.get(account).account == account);
        if (account != 0) assert(&registry.get(account) != first);
    }
    assert(&registry.get(0) == first);
    assert(AccountConnection::created == 10000);
    std::vector<std::thread> workers;
    std::vector<AccountConnection *> results(24);
    for (int i = 0; i < 24; ++i) {
        workers.emplace_back([&, i] { results[i] = &registry.get(50000); });
    }
    for (auto &worker : workers) worker.join();
    for (auto *result : results) assert(result == results[0]);
    assert(AccountConnection::created == 10001);
    std::cout << "PASS: native account identity, dynamic capacity, stable addresses and concurrent creation\n";
}

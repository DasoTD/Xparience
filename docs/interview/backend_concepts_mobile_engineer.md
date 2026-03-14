# Flutter Mobile Engineer Interview Questions: Backend Concepts & Scenario-Based Questions

> **Context:** These questions are tailored for a Flutter mobile engineer working on **Xparience** — a dating app with real-time chat, AI-powered matching, virtual dates, OTP verification, and subscription tiers.
>
> **Tech Stack:** Flutter (Dart) · Spring Boot 4.0.3 · PostgreSQL · Riverpod · STOMP WebSocket · Dio · Java 25

---

## Table of Contents

1. [Race Conditions](#1-race-conditions)
2. [Row Locking](#2-row-locking)
3. [Idempotency](#3-idempotency)
4. [Concurrency](#4-concurrency)
5. [Transactions](#5-transactions)
6. [Database Indexing](#6-database-indexing)
7. [Caching](#7-caching)
8. [Rate Limiting](#8-rate-limiting)
9. [Scenario-Based Questions](#9-scenario-based-questions)

---

## 1. Race Conditions

### Definition

A **race condition** occurs when two or more operations compete to read and modify shared data at the same time, and the final result depends on the unpredictable order in which they finish. The "race" is who gets there first — and the loser overwrites correct state with stale data.

### What It Means from a Mobile/Flutter Engineer's Perspective

On the Flutter side, a race condition most commonly appears when:

- The user taps a button multiple times before the first API response returns.
- Two async operations (e.g., a Riverpod provider refresh and a manual tap) both trigger the same network call at the same time.
- The UI re-renders with stale cached state while a new response is still in-flight.

Flutter is single-threaded (one UI isolate), but **async/await** and **multiple concurrent Futures** can still race against each other. Riverpod helps mitigate this, but the risk remains when state is not properly guarded.

### How It Can Go Wrong in Xparience

**Scenario — Match Accept/Reject:**
Both User A and User B are shown each other as a daily match. On the backend, accepting a match is a two-step operation: check whether the other person has already accepted, then create the mutual match record. If User A's accept request and User B's accept request arrive at the same **exact** millisecond, two threads can both read "no mutual match yet", then both independently try to insert the mutual match row — resulting in a duplicate match or a constraint violation crash.

**Flutter-side symptom:** User A taps "Accept", the UI shows a spinner, but then receives a `500 Internal Server Error` or gets stuck because the server response is delayed by a deadlock. If a double-tap occurs, the same accept call fires twice.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Technique | Purpose |
|-----------|---------|
| Disable button on first tap (loading state) | Prevent duplicate requests from the same user |
| Use `AsyncNotifier` in Riverpod and check `state.isLoading` | Guard against concurrent calls to the same provider |
| Cancel in-flight requests with `CancelToken` (Dio) | Abort the older request if a newer one is triggered |
| Optimistic UI update + rollback on error | Show instant feedback while protecting against duplicate side effects |

### Code Example — Preventing Double-Tap Race in Match Action

```dart
// match_action_notifier.dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:dio/dio.dart';

class MatchActionNotifier extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  Future<void> respondToMatch({
    required String matchId,
    required bool accepted,
  }) async {
    // Guard: if already loading, ignore the duplicate tap
    if (state.isLoading) return;

    state = const AsyncLoading();
    state = await AsyncValue.guard(
      () => ref.read(matchRepositoryProvider).respondToMatch(
            matchId: matchId,
            accepted: accepted,
          ),
    );
  }
}

final matchActionProvider =
    AsyncNotifierProvider<MatchActionNotifier, void>(MatchActionNotifier.new);
```

```dart
// match_card_widget.dart
final matchAction = ref.watch(matchActionProvider);

ElevatedButton(
  // Button is disabled while the request is in-flight
  onPressed: matchAction.isLoading
      ? null
      : () => ref
          .read(matchActionProvider.notifier)
          .respondToMatch(matchId: match.id, accepted: true),
  child: matchAction.isLoading
      ? const CircularProgressIndicator.adaptive()
      : const Text('Accept'),
)
```

---

## 2. Row Locking

### Definition

**Row locking** is a database mechanism that prevents two concurrent transactions from modifying the same database row at the same time. When a transaction locks a row, other transactions must wait until the lock is released before they can read or write that row. This serialises access to shared data without locking the whole table.

In PostgreSQL (used by Xparience), `SELECT ... FOR UPDATE` acquires a row-level exclusive lock.

### What It Means from a Mobile/Flutter Engineer's Perspective

The Flutter engineer doesn't write SQL — but they must understand row locking because it directly causes:

- **Delayed API responses:** A locked row means the backend is waiting. The Flutter app's Dio request may time out if the lock takes too long.
- **Retry logic:** If the backend returns `409 Conflict` or `503 Service Unavailable` due to lock contention, the Flutter layer needs a sensible retry strategy.
- **UX degradation:** A user upgrading their subscription while another device also taps "Upgrade" may see one request hang until the lock clears.

### How It Can Go Wrong in Xparience

**Scenario — Subscription Upgrade:**
A user opens Xparience on both their phone and tablet. They tap "Upgrade to Premium" on both devices within 200 ms. On the backend, both requests hit the `user_subscriptions` row. The Spring Boot service uses `SELECT ... FOR UPDATE` to read the current subscription tier before applying the Stripe charge. Device A's transaction locks the row first. Device B's transaction waits. If Device B's Dio request times out (default: 30 s) before the lock releases and the first transaction commits, Device B receives a timeout error — even though the upgrade succeeded on Device A.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Strategy | Implementation |
|----------|---------------|
| Retry with exponential back-off | Retry the failed request 2–3 times with increasing delays |
| Idempotency key on POST requests | Backend can detect duplicates and return the original result |
| Show "Processing…" state for slow operations | Never leave the user staring at a blank screen |
| Invalidate provider cache on success | Refresh subscription state so all screens see the updated tier |

### Code Example — Retry with Exponential Back-off (Dio Interceptor)

```dart
// retry_interceptor.dart
import 'package:dio/dio.dart';

class RetryInterceptor extends Interceptor {
  final Dio dio;
  final int maxRetries;
  final Duration baseDelay;

  RetryInterceptor({
    required this.dio,
    this.maxRetries = 3,
    this.baseDelay = const Duration(milliseconds: 500),
  });

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final extra = err.requestOptions.extra;
    final retryCount = (extra['retryCount'] as int?) ?? 0;

    final isRetryable = err.response?.statusCode == 503 ||
        err.response?.statusCode == 409 ||
        err.type == DioExceptionType.connectionTimeout;

    if (isRetryable && retryCount < maxRetries) {
      final delay = baseDelay * (1 << retryCount); // 500ms, 1s, 2s
      await Future.delayed(delay);

      final newOptions = err.requestOptions
        ..extra['retryCount'] = retryCount + 1;

      try {
        final response = await dio.fetch(newOptions);
        return handler.resolve(response);
      } catch (e) {
        return handler.next(err);
      }
    }

    handler.next(err);
  }
}
```

---

## 3. Idempotency

### Definition

An operation is **idempotent** if performing it multiple times produces the same result as performing it once. For example, `DELETE /matches/123` should return `200 OK` (or `204 No Content`) whether it's called once or ten times — the match is deleted either way.

### What It Means from a Mobile/Flutter Engineer's Perspective

Mobile networks are unreliable. A request may be sent successfully but the response may never arrive (timeout, dropped connection). The Flutter app cannot tell whether the server processed the request or not. If the app naively retries, it risks:

- Charging a subscription twice.
- Sending an OTP twice.
- Accepting a match twice.

Idempotency keys (a unique UUID sent in the request header or body) let the backend detect and deduplicate retry attempts, making retries safe.

### How It Can Go Wrong in Xparience

**Scenario — Stripe Subscription Upgrade:**
User taps "Upgrade to Premium". Flutter fires `POST /api/subscriptions/upgrade`. The phone loses cellular signal mid-flight. Dio times out after 30 s. The user, seeing no feedback, taps "Upgrade" again. The backend processes **both** requests and charges the user twice through Stripe.

**Scenario — AI Icebreaker (3/day limit):**
Flutter sends `POST /api/ai/icebreaker`. The response arrives but is lost due to a socket error. The app retries. If the backend is not idempotent, it generates a second icebreaker and decrements the daily counter twice, exhausting two of the three daily uses.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Technique | Detail |
|-----------|--------|
| Generate a UUID per user action | Created once when the user initiates the action; reused on every retry |
| Send `Idempotency-Key: <uuid>` header | Standard practice for payment and mutation endpoints |
| Store the key locally until success | If the app is killed and relaunched before success, the same key is resent |
| Cache successful responses by key | Avoid re-triggering side effects on re-renders |

### Code Example — Idempotency Key Injection via Dio Interceptor

```dart
// idempotency_interceptor.dart
import 'package:dio/dio.dart';
import 'package:uuid/uuid.dart';

/// Attaches a unique idempotency key to all non-GET requests.
/// The key is regenerated only when the user triggers a new action,
/// not on automatic retries (handled by RetryInterceptor).
class IdempotencyInterceptor extends Interceptor {
  static const _header = 'Idempotency-Key';

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final isWriteMethod = ['POST', 'PUT', 'PATCH']
        .contains(options.method.toUpperCase());

    if (isWriteMethod && !options.headers.containsKey(_header)) {
      options.headers[_header] = const Uuid().v4();
    }

    handler.next(options);
  }
}
```

```dart
// Usage in subscription upgrade
Future<void> upgradeSubscription(SubscriptionTier tier) async {
  // The idempotency key is attached automatically by the interceptor.
  // Retries via RetryInterceptor reuse the same RequestOptions (same key).
  await _dio.post(
    '/api/subscriptions/upgrade',
    data: {'tier': tier.name},
  );
}
```

---

## 4. Concurrency

### Definition

**Concurrency** means multiple tasks are in progress at the same time. They may not literally execute simultaneously (that's *parallelism*), but they are interleaved — each task makes progress while others are paused. In a server context, concurrent requests are handled by different threads. In Flutter, concurrency is achieved with `async/await` and `Future`s inside the single-threaded event loop, with heavy computation offloaded to `Isolate`s.

### What It Means from a Mobile/Flutter Engineer's Perspective

Flutter's UI runs on the main isolate. All `async`/`await` code still runs on this single thread — the event loop schedules microtasks and I/O callbacks. This means:

- Long-running synchronous code **blocks** the UI (janky scrolling, frame drops).
- Multiple concurrent `Future`s can interleave and access shared state.
- `compute()` or `Isolate.spawn()` are needed for CPU-heavy tasks (e.g., image processing, JSON parsing of large payloads).

### How It Can Go Wrong in Xparience

**Scenario — Profile Image Upload:**
When a user uploads their verification selfie, the app must: (1) compress the image, (2) upload to Cloudinary via `POST /api/verification/selfie`, and (3) poll for the face-match result. If all three are triggered concurrently without coordination and the user navigates away, the upload `Future` may complete and try to update a disposed provider, throwing a `StateError`.

**Scenario — Daily Match Refresh:**
At 9 AM, a background task and the user's manual "refresh" pull both call `GET /api/matches/daily`. Two concurrent responses arrive and both try to set the same Riverpod state, resulting in a brief flicker between two different match lists.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Strategy | Detail |
|----------|--------|
| `AsyncNotifier` guards concurrent calls | `state.isLoading` check rejects duplicate triggers |
| `Completer` / `Mutex` | Serialise access to a shared resource |
| `compute()` for heavy work | Runs in a separate isolate, keeps UI thread free |
| Cancel tokens on navigation | Prevent disposed providers from receiving stale results |

### Code Example — Concurrent Image Compress + Upload with `compute`

```dart
// image_upload_service.dart
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:flutter_image_compress/flutter_image_compress.dart';
import 'package:dio/dio.dart';

Future<String> uploadSelfie(File imageFile) async {
  // Step 1: Compress in a separate isolate — never block the UI thread
  final compressed = await compute(_compressImage, imageFile.path);

  // Step 2: Upload to backend
  final formData = FormData.fromMap({
    'file': await MultipartFile.fromBytes(
      compressed,
      filename: 'selfie.jpg',
    ),
  });

  final response = await _dio.post(
    '/api/verification/selfie',
    data: formData,
    onSendProgress: (sent, total) {
      // Update upload progress UI
      _uploadProgress.value = sent / total;
    },
  );

  return response.data['imageUrl'] as String;
}

// Runs inside compute's isolate — safe to do CPU-heavy work here
Future<Uint8List> _compressImage(String path) async {
  final result = await FlutterImageCompress.compressWithFile(
    path,
    minWidth: 800,
    minHeight: 800,
    quality: 85,
  );
  return result!;
}
```

---

## 5. Transactions

### Definition

A **database transaction** is a sequence of operations that are treated as a single, atomic unit of work. Either **all** operations succeed (commit) or **none** of them take effect (rollback). Transactions follow **ACID** properties:

| Property | Meaning |
|----------|---------|
| **A**tomicity | All-or-nothing |
| **C**onsistency | Data stays valid before and after |
| **I**solation | Concurrent transactions don't interfere |
| **D**urability | Committed changes survive crashes |

### What It Means from a Mobile/Flutter Engineer's Perspective

The Flutter engineer doesn't manage database transactions directly, but they must understand them because:

- A backend failure mid-transaction means **partial data** may appear briefly if isolation is not set correctly.
- The API response for a transactional operation is binary: full success or full failure. The Flutter app must handle both cases cleanly.
- If the backend rolls back, the Flutter app must revert any optimistic UI updates.

### How It Can Go Wrong in Xparience

**Scenario — Accepting a Virtual Date Invite:**
Accepting a date invite involves: (1) updating `date_invites.status = ACCEPTED`, (2) creating a `virtual_dates` record, and (3) sending a push notification. If steps 1 and 2 succeed but step 3 fails (e.g., push notification service is down), a transaction without proper boundary will leave the invite accepted but no virtual date created. The user's screen would show "Invite Accepted" but the date would never appear in their schedule.

**Scenario — Match + Conversation Creation:**
When a mutual match is confirmed, the backend must atomically create the `matches` record **and** the `conversations` record. If these happen in separate transactions and the second fails, the users have a match but no chat — leading to a broken experience.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Strategy | Detail |
|----------|--------|
| Treat API responses as atomic | Only update UI on full `200 OK`; roll back optimistic updates on any error |
| Distinguish 4xx vs 5xx errors | 4xx = user error (show message); 5xx = server/transaction failure (retry or show generic error) |
| Re-fetch after critical mutations | After a transactional action, invalidate the relevant Riverpod provider to re-sync state |
| Show actionable error messages | "Something went wrong — your invite was not accepted. Please try again." |

### Code Example — Optimistic Update with Rollback on Transaction Failure

```dart
// date_invite_notifier.dart
class DateInviteNotifier extends AsyncNotifier<DateInvite> {
  @override
  FutureOr<DateInvite> build() =>
      ref.read(dateRepositoryProvider).getInvite(inviteId);

  Future<void> acceptInvite(String inviteId) async {
    final previousState = state;

    // Optimistic update: immediately show "ACCEPTED" in the UI
    state = AsyncData(
      state.requireValue.copyWith(status: InviteStatus.accepted),
    );

    try {
      await ref.read(dateRepositoryProvider).acceptInvite(inviteId);
      // Re-fetch to get the full virtual date object created by the transaction
      ref.invalidateSelf();
    } catch (e) {
      // Transaction failed — roll back the optimistic update
      state = previousState;
      rethrow;
    }
  }
}
```

---

## 6. Database Indexing

### Definition

A **database index** is a data structure (typically a B-tree) that the database maintains alongside a table to allow fast lookups by a specific column (or set of columns), without scanning every row. Think of it like the index at the back of a book — you jump straight to the page instead of reading every page.

Without an index on a frequently queried column, PostgreSQL performs a **sequential scan** (reads every row), which is catastrophically slow as the table grows.

### What It Means from a Mobile/Flutter Engineer's Perspective

The Flutter engineer typically doesn't create indexes, but they feel the absence of them as:

- **Slow API responses:** A `GET /api/matches/daily` that triggers a full table scan on a 1M-row `users` table returns in 4 s instead of 40 ms.
- **UI jank:** Long-polling or paginated list views appear frozen while waiting for slow queries.
- **Timeouts:** Dio's default 30-second timeout may fire before the unindexed query completes under load.

When profiling app performance, slow network calls that can't be explained by payload size are often symptoms of missing indexes.

### How It Can Go Wrong in Xparience

**Scenario — Daily Match Refresh:**
`GET /api/matches/daily` filters matches by `user_id`, `match_date`, and `status`. Without a composite index on `(user_id, match_date, status)`, every request triggers a full scan of the `matches` table. With 500,000 users each getting 5 daily matches, that's 2.5M rows to scan per request. The Flutter app's `FutureProvider` that drives the match list card would remain in the `loading` state for seconds.

**Scenario — Chat Message History:**
`GET /api/chat/{conversationId}/messages?page=0` paginates messages. Without an index on `(conversation_id, created_at DESC)`, loading chat history becomes progressively slower as conversations grow.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Technique | Detail |
|-----------|--------|
| Skeleton loading screens | Show placeholder cards while data loads; don't block interaction |
| Pagination with `cursor`-based loading | Load 20 messages at a time; never request the full history |
| Client-side caching | Cache the last fetch in Riverpod state; reuse on fast tab switches |
| Time API response latency | Log `stopwatch.elapsed` in Dio interceptor; flag anything over 500 ms for investigation |

### Code Example — Paginated Chat Messages with Infinite Scroll

```dart
// messages_notifier.dart
class MessagesNotifier extends StateNotifier<AsyncValue<List<Message>>> {
  MessagesNotifier(this._repo, this._conversationId)
      : super(const AsyncLoading()) {
    loadNextPage();
  }

  final MessageRepository _repo;
  final String _conversationId;

  int _page = 0;
  bool _hasMore = true;
  bool _loading = false;

  Future<void> loadNextPage() async {
    if (!_hasMore || _loading) return;
    _loading = true;

    try {
      final newMessages = await _repo.fetchMessages(
        conversationId: _conversationId,
        page: _page,
        pageSize: 20,
      );

      final existing =
          state.maybeWhen(data: (msgs) => msgs, orElse: () => <Message>[]);

      state = AsyncData([...existing, ...newMessages]);

      if (newMessages.length < 20) _hasMore = false;
      _page++;
    } catch (e, st) {
      state = AsyncError(e, st);
    } finally {
      _loading = false;
    }
  }
}
```

---

## 7. Caching

### Definition

**Caching** stores the result of an expensive operation (a database query, an API call, a computed value) in a faster storage layer (memory, Redis, local device storage) so that future requests can be served from the cache instead of repeating the expensive work.

Caches come with a trade-off: **freshness vs speed**. Stale data is fast but wrong; fresh data is correct but slow.

### What It Means from a Mobile/Flutter Engineer's Perspective

Flutter engineers implement caching at two levels:

1. **In-memory (Riverpod state):** Provider results are held in memory while the app is running. They are refreshed when `ref.invalidate()` is called or when a provider's lifecycle ends.
2. **Persistent (Hive / SharedPreferences / SQLite):** Data survives app restarts, useful for offline mode.

Caching avoids unnecessary network calls, reduces battery usage, and makes the app feel instant on fast navigations (e.g., going back to the matches screen without a reload).

### How It Can Go Wrong in Xparience

**Scenario — AI Icebreaker Limit Display:**
The app caches the user's icebreaker count (`icebreakerUsedToday`) in Riverpod state. The user generates 3 icebreakers. The cache correctly shows 3/3 used. But the daily reset happens at midnight on the server. When the user reopens the app the next morning, the cached value still shows 3/3 — the UI greys out the icebreaker button — even though the server has already reset the count to 0/3.

**Scenario — Subscription Tier Stale Cache:**
After a successful subscription upgrade, the app shows the old `FREE` badge because the `subscriptionProvider` was not invalidated. Buttons for Premium features remain locked.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Strategy | Detail |
|----------|--------|
| Cache invalidation on mutation | Call `ref.invalidate(icebreakerProvider)` after generating an icebreaker |
| TTL-based refresh | Re-fetch subscription data every 15 minutes or on app resume |
| Stale-while-revalidate pattern | Show cached data immediately, silently refresh in background |
| Persist + validate on startup | Load cached data from SharedPreferences, immediately trigger a background re-fetch |

### Code Example — Stale-While-Revalidate for Subscription Tier

```dart
// subscription_provider.dart
final subscriptionProvider =
    AsyncNotifierProvider<SubscriptionNotifier, SubscriptionInfo>(
  SubscriptionNotifier.new,
);

class SubscriptionNotifier extends AsyncNotifier<SubscriptionInfo> {
  static const _cacheKey = 'subscription_info';
  static const _ttl = Duration(minutes: 15);
  DateTime? _lastFetched;

  @override
  FutureOr<SubscriptionInfo> build() async {
    // 1. Return cached data immediately (stale-while-revalidate)
    final cached = await _loadFromPrefs();
    if (cached != null) {
      // 2. Trigger background refresh if TTL expired
      if (_isCacheStale()) {
        Future.microtask(_refresh);
      }
      return cached;
    }
    // 3. No cache — fetch and store
    return _refresh();
  }

  Future<SubscriptionInfo> _refresh() async {
    final info = await ref.read(subscriptionRepositoryProvider).getMyPlan();
    _lastFetched = DateTime.now();
    await _saveToPrefs(info);
    return info;
  }

  bool _isCacheStale() =>
      _lastFetched == null ||
      DateTime.now().difference(_lastFetched!) > _ttl;

  Future<SubscriptionInfo?> _loadFromPrefs() async {
    final prefs = await SharedPreferences.getInstance();
    final json = prefs.getString(_cacheKey);
    if (json == null) return null;
    return SubscriptionInfo.fromJson(jsonDecode(json));
  }

  Future<void> _saveToPrefs(SubscriptionInfo info) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_cacheKey, jsonEncode(info.toJson()));
  }
}
```

---

## 8. Rate Limiting

### Definition

**Rate limiting** is the practice of restricting how many requests a client can make to a server within a given time window. It protects the server from abuse, prevents a single user from monopolising resources, and enforces business rules (e.g., "maximum 3 AI icebreakers per day").

Common strategies: **token bucket**, **leaky bucket**, **fixed window**, **sliding window**.

### What It Means from a Mobile/Flutter Engineer's Perspective

The Flutter engineer must handle rate-limit responses gracefully:

- **HTTP 429 Too Many Requests** — the standard status code for rate limiting.
- The response usually includes a `Retry-After` header indicating how many seconds to wait.
- Retrying immediately after a 429 makes things worse — exponential back-off is required.
- The UI should communicate the limit clearly ("You've used all 3 icebreakers today. Come back tomorrow!").

### How It Can Go Wrong in Xparience

**Scenario — AI Icebreaker (3/day limit):**
The server enforces a 3-per-day limit, resetting at midnight. If the Flutter app doesn't cache the used count, the user can spam the "Generate Icebreaker" button. The first 3 requests succeed; the 4th returns `429`. If the app has no handler for 429, Dio throws a `DioException` and the app shows a generic "Something went wrong" error — no explanation that the daily limit was reached.

**Scenario — OTP Resend Spam:**
`POST /api/auth/otp/resend` is limited to 1 request per 60 seconds. If the user taps "Resend OTP" rapidly, the second tap hits a 429. Without a cooldown timer in the Flutter UI, the user will keep tapping and each tap will fire a network call that is rejected.

### How the Mobile Engineer Ensures/Handles It on the Flutter Side

| Technique | Detail |
|-----------|--------|
| Handle 429 in Dio interceptor | Parse `Retry-After` header; schedule a retry after that delay |
| Disable button after action | Start a countdown timer on the button for rate-limited endpoints |
| Show remaining quota | Display "2/3 icebreakers used today" using the count returned by the API |
| Pre-emptive client-side throttle | Debounce rapid taps before the request even fires |

### Code Example — OTP Resend Cooldown Button

```dart
// otp_screen.dart
class OtpScreen extends ConsumerStatefulWidget { ... }

class _OtpScreenState extends ConsumerState<OtpScreen> {
  int _cooldownSeconds = 0;
  Timer? _timer;

  void _startCooldown(int seconds) {
    setState(() => _cooldownSeconds = seconds);
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (_cooldownSeconds <= 1) {
        t.cancel();
        setState(() => _cooldownSeconds = 0);
      } else {
        setState(() => _cooldownSeconds--);
      }
    });
  }

  Future<void> _resendOtp() async {
    try {
      await ref.read(authServiceProvider).resendOtp(phone: widget.phone);
      _startCooldown(60); // Backend enforces 60 s; UI mirrors it
    } on DioException catch (e) {
      if (e.response?.statusCode == 429) {
        final retryAfter =
            int.tryParse(e.response?.headers.value('retry-after') ?? '60') ??
                60;
        _startCooldown(retryAfter);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Please wait $_cooldownSeconds seconds before requesting a new OTP.',
            ),
          ),
        );
      } else {
        rethrow;
      }
    }
  }

  @override
  Widget build(BuildContext context) => TextButton(
        onPressed: _cooldownSeconds > 0 ? null : _resendOtp,
        child: Text(
          _cooldownSeconds > 0
              ? 'Resend in ${_cooldownSeconds}s'
              : 'Resend OTP',
        ),
      );
}
```

---

## 9. Scenario-Based Questions

> Each scenario is grounded in a real Xparience feature. Full question and answer pairs follow.

---

### Scenario 1 — OTP Verification: Double-Tap Race Condition

**Question:**
> A user on a slow 3G connection taps "Verify OTP" twice in quick succession. Both requests reach the backend. The OTP token is single-use — after the first verification, the backend marks it as used and returns `200 OK`. The second request reads the (now-used) token and returns `400 Bad Request: OTP already used`. How do you prevent this on the Flutter side, and what should the user see?

**Answer:**

The root cause is a race condition created by the double-tap. The fix is a combination of UI guard and a single-request guarantee:

1. **Disable the button immediately on first tap** — the button enters a loading state and ignores subsequent taps until the response arrives.
2. **Use `AsyncNotifier` to gate calls** — check `state.isLoading` before firing the request.
3. **Show a clear error only for real failures** — if the `400` somehow arrives (e.g., the user force-quit and relaunched before the token expired), display "This OTP has already been used. Please request a new one."

```dart
// otp_notifier.dart
class OtpNotifier extends AsyncNotifier<void> {
  @override
  FutureOr<void> build() {}

  Future<void> verifyOtp({
    required String phone,
    required String code,
  }) async {
    if (state.isLoading) return; // ← Drop duplicate taps
    state = const AsyncLoading();
    state = await AsyncValue.guard(
      () => ref.read(authRepositoryProvider).verifyOtp(
            phone: phone,
            code: code,
          ),
    );
  }
}
```

**User experience:** The button shows a spinner after the first tap. If the OTP is correct, the user is navigated forward. If already used, a banner explains what happened with a link to request a new OTP.

---

### Scenario 2 — Match Accept/Reject: Row Locking & Mutual Match

**Question:**
> User A (deviceA) and User B (deviceB) both have each other in their daily matches. At 10:05:00.123 AM, both tap "Accept" simultaneously. The backend must check whether the other person has already accepted, then create a mutual match. How does row locking come into play, and what does the Flutter app experience?

**Answer:**

The backend uses `SELECT ... FOR UPDATE` on the match row to prevent two threads from both reading "no mutual match yet" and both inserting duplicates. One transaction wins the lock, creates the mutual match, and commits. The second transaction acquires the lock after the first commits, sees "mutual match already exists," and simply returns the existing match.

**Flutter-side experience:**
- The losing request may take longer (it blocked on the lock).
- Both users should receive `200 OK` — a well-designed backend returns the created/existing match in either case.
- The Flutter app should not assume the first 200 is from its own insert vs. a duplicate detection.

```dart
// match_result_handler.dart
Future<void> handleMatchResponse(Response<dynamic> response) async {
  final matchData = MatchResponse.fromJson(response.data);

  if (matchData.isMutualMatch) {
    // Both accepted — navigate to the new conversation
    ref.read(routerProvider).push('/chat/${matchData.conversationId}');
    ref.invalidate(dailyMatchesProvider); // Refresh match list
  } else {
    // Only one side has accepted so far
    ref.read(matchFeedbackProvider.notifier).setWaiting(matchData.matchId);
  }
}
```

---

### Scenario 3 — Real-Time Chat (WebSocket): Concurrent Message Delivery

**Question:**
> While on a poor connection, a user sends message M1. Before M1's HTTP ACK arrives, they send M2. Both messages are queued in the STOMP WebSocket client. The connection drops and reconnects. M2 arrives at the server first due to a re-queue issue. How do you handle out-of-order delivery in the Flutter chat UI?

**Answer:**

Out-of-order delivery is a classic concurrency problem in real-time messaging. The solution is client-side sequence/timestamp ordering:

1. Assign a **local sequence ID** to every message before it is sent.
2. Use the server-assigned `createdAt` timestamp for display ordering.
3. Hold messages in a sorted list (sorted by `createdAt`) — new incoming messages are inserted in the correct position, not appended blindly.
4. Show **sent vs. delivered vs. read** status using the ACK from the server.

```dart
// chat_message_sorter.dart
List<Message> insertSorted(List<Message> existing, Message newMsg) {
  final updated = [...existing, newMsg];
  updated.sort((a, b) => a.createdAt.compareTo(b.createdAt));
  return updated;
}

// In the STOMP subscription handler:
_stompClient.subscribe(
  destination: '/user/queue/messages',
  callback: (StompFrame frame) {
    final incoming = Message.fromJson(jsonDecode(frame.body!));
    ref.read(chatProvider(conversationId).notifier).insertMessage(incoming);
  },
);
```

```dart
// chat_notifier.dart
void insertMessage(Message message) {
  final current = state.requireValue;
  // Deduplicate by message ID (idempotency for re-delivered messages)
  if (current.any((m) => m.id == message.id)) return;
  state = AsyncData(insertSorted(current, message));
}
```

---

### Scenario 4 — Virtual Date Invite: Transactional Atomicity

**Question:**
> User A sends a virtual date invite to User B. The backend must: (1) insert into `date_invites`, (2) insert into `notifications`, and (3) send a push via FCM. FCM is unavailable for 10 seconds. Should the transaction roll back? What should the Flutter app show User A?

**Answer:**

This is a **mixed transaction** problem. Database operations (1 and 2) should be atomic, but the FCM push (step 3) is an **external side effect** that cannot be rolled back if it succeeds. The recommended backend pattern:

- Steps 1 + 2 in a single `@Transactional` database transaction.
- Step 3 (FCM) runs **after** commit, asynchronously (fire-and-forget or a retry queue).

**Flutter-side behaviour:**
- The Flutter app receives `201 Created` as soon as the DB transaction commits (i.e., after steps 1+2).
- The app optimistically shows "Invite sent!" without waiting for FCM delivery confirmation.
- User B will receive the push when FCM recovers; if FCM permanently fails, User B sees the invite when they next open the app (via `GET /api/dates/pending`).

```dart
// date_invite_screen.dart
Future<void> sendDateInvite(DateInviteRequest request) async {
  state = const AsyncLoading();
  try {
    final invite = await ref
        .read(dateRepositoryProvider)
        .sendInvite(request); // POST /api/dates/invite

    // ← 201 received after DB commit; FCM runs async on backend
    state = AsyncData(invite);
    ref.read(snackbarProvider).show('Date invite sent! 🎉');
  } on DioException catch (e) {
    state = AsyncError(e, StackTrace.current);
    ref.read(snackbarProvider).show(
          e.response?.statusCode == 409
              ? 'You already have a pending invite with this user.'
              : 'Failed to send invite. Please try again.',
        );
  }
}
```

---

### Scenario 5 — Subscription Upgrade (Stripe): Idempotency & Double-Charge

**Question:**
> A user taps "Upgrade to Premium (₦9,999/month)". The Dio request is sent. The phone loses Wi-Fi. Dio raises a `DioExceptionType.connectionTimeout` after 30 s. The user taps "Upgrade" again. How do you ensure they are not charged twice?

**Answer:**

Use a per-action **idempotency key**:

1. When the user taps "Upgrade", generate a `UUID` and store it in the notifier's local state.
2. Send the UUID as the `Idempotency-Key` header on every attempt.
3. On retry (timeout → user taps again), the same UUID is reused.
4. The backend (and Stripe via `idempotencyKey` parameter) recognises the duplicate and returns the original charge result without re-processing.

```dart
// subscription_upgrade_notifier.dart
class SubscriptionUpgradeNotifier extends AsyncNotifier<SubscriptionInfo> {
  String? _idempotencyKey;

  @override
  FutureOr<SubscriptionInfo> build() =>
      ref.read(subscriptionRepositoryProvider).getMyPlan();

  Future<void> upgrade(SubscriptionTier tier) async {
    if (state.isLoading) return;

    // Generate key once per user action; reuse on retries
    _idempotencyKey ??= const Uuid().v4();

    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final info = await ref
          .read(subscriptionRepositoryProvider)
          .upgrade(tier: tier, idempotencyKey: _idempotencyKey!);

      _idempotencyKey = null; // Reset only after confirmed success
      return info;
    });
  }
}
```

```dart
// subscription_repository.dart
Future<SubscriptionInfo> upgrade({
  required SubscriptionTier tier,
  required String idempotencyKey,
}) async {
  final response = await _dio.post(
    '/api/subscriptions/upgrade',
    data: {'tier': tier.name},
    options: Options(
      headers: {'Idempotency-Key': idempotencyKey},
    ),
  );
  return SubscriptionInfo.fromJson(response.data);
}
```

---

### Scenario 6 — Profile Image Upload: Concurrency & Upload State

**Question:**
> A user uploads their profile photo. The upload takes 8 seconds on a slow connection. Mid-upload, they navigate away to the chat screen. When they return to the profile screen, they tap "Upload" again with a new photo. Now two uploads are in-flight. How do you handle this?

**Answer:**

The solution is to **cancel the first upload** when the second begins, and to keep upload state in a Riverpod notifier so it survives navigation:

1. Use `CancelToken` from Dio on every upload request.
2. Store the active `CancelToken` in the notifier.
3. When a new upload begins, call `previousToken.cancel()` first.
4. The notifier outlives the widget (scoped at router/app level) — the upload continues in background even if the user navigates away.

```dart
// profile_image_notifier.dart
class ProfileImageNotifier extends AsyncNotifier<String?> {
  CancelToken? _activeToken;

  @override
  FutureOr<String?> build() => null;

  Future<void> uploadPhoto(File photo) async {
    // Cancel any in-flight upload
    _activeToken?.cancel('Replaced by new upload');
    _activeToken = CancelToken();

    state = const AsyncLoading();

    final compressed = await compute(_compressImage, photo.path);

    state = await AsyncValue.guard(() async {
      final formData = FormData.fromMap({
        'file': await MultipartFile.fromBytes(compressed, filename: 'photo.jpg'),
      });

      final res = await _dio.post(
        '/api/profile/photo',
        data: formData,
        cancelToken: _activeToken,
      );
      return res.data['imageUrl'] as String;
    });
  }
}
```

---

### Scenario 7 — AI Icebreaker (3/day Limit): Rate Limiting & Caching

**Question:**
> A user has 2/3 icebreakers left. They generate one — now 3/3. The app caches this locally. At midnight, the backend resets the counter to 0/3. The user opens the app at 12:05 AM — the UI still shows "0 icebreakers remaining" (the greyed-out button). How do you fix the stale cache?

**Answer:**

The client must not trust a locally cached count indefinitely. The fix:

1. **Store a `resetDate`** alongside the count in the cache.
2. On app resume and on screen mount, check if `resetDate` is in the past — if so, treat the cache as stale and re-fetch.
3. Display the count from the server response after every icebreaker generation.

```dart
// icebreaker_provider.dart
final icebreakerProvider =
    AsyncNotifierProvider<IcebreakerNotifier, IcebreakerStatus>(
  IcebreakerNotifier.new,
);

@freezed
class IcebreakerStatus with _$IcebreakerStatus {
  const factory IcebreakerStatus({
    required int usedToday,
    required int dailyLimit,
    required DateTime resetAt,
  }) = _IcebreakerStatus;

  factory IcebreakerStatus.fromJson(Map<String, dynamic> json) =>
      _$IcebreakerStatusFromJson(json);
}

class IcebreakerNotifier extends AsyncNotifier<IcebreakerStatus> {
  @override
  FutureOr<IcebreakerStatus> build() => _fetch();

  Future<IcebreakerStatus> _fetch() =>
      ref.read(aiRepositoryProvider).getIcebreakerStatus();
      // GET /api/ai/icebreaker/status

  Future<String> generate(String matchId) async {
    final result =
        await ref.read(aiRepositoryProvider).generateIcebreaker(matchId);
    // Always re-fetch status from server after generating
    ref.invalidateSelf();
    return result.suggestion;
  }

  /// Called on AppLifecycleState.resumed
  void refreshIfStale() {
    final status = state.valueOrNull;
    if (status == null || DateTime.now().isAfter(status.resetAt)) {
      ref.invalidateSelf();
    }
  }
}
```

```dart
// app_lifecycle_observer.dart — wire up in MaterialApp
class AppLifecycleObserver extends ConsumerWidget {
  const AppLifecycleObserver({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return AppLifecycleListener(
      onResume: () =>
          ref.read(icebreakerProvider.notifier).refreshIfStale(),
      child: child,
    );
  }
}
```

---

### Scenario 8 — Daily Match Refresh at 9 AM: Concurrency & Cache Invalidation

**Question:**
> The app has a background task that triggers `GET /api/matches/daily` at 9 AM via a WorkManager job. At exactly 9 AM, the user also taps "Refresh" in the UI. Both calls fire simultaneously. How do you prevent a flicker or duplicate network calls in Riverpod?

**Answer:**

Use Riverpod's `ref.invalidate()` / `ref.refresh()` — they are idempotent. If a provider is already refreshing, a second `invalidate()` call is ignored until the first completes. The key practices:

1. **Single source of truth:** Both the background job and the UI pull state through the same `dailyMatchesProvider`.
2. **Background job uses `ref.invalidate()`:** This schedules a refresh; the provider handles deduplication.
3. **UI shows a `RefreshIndicator`** that calls `ref.invalidate()` on pull-to-refresh — same mechanism.

```dart
// daily_matches_provider.dart
final dailyMatchesProvider =
    AsyncNotifierProvider<DailyMatchesNotifier, List<MatchSummary>>(
  DailyMatchesNotifier.new,
);

class DailyMatchesNotifier extends AsyncNotifier<List<MatchSummary>> {
  @override
  FutureOr<List<MatchSummary>> build() =>
      ref.read(matchRepositoryProvider).getDailyMatches();
      // GET /api/matches/daily
}

// In WorkManager callback (background task):
void onDailyRefreshTask(WidgetRef ref) {
  ref.invalidate(dailyMatchesProvider);
  // Riverpod's AsyncNotifier deduplicates if already loading
}

// In UI (pull-to-refresh):
RefreshIndicator(
  onRefresh: () async {
    ref.invalidate(dailyMatchesProvider);
    await ref.read(dailyMatchesProvider.future);
  },
  child: matchListView,
)
```

---

### Scenario 9 — Concurrent Logins: JWT & Session Invalidation

**Question:**
> A user logs into Xparience on their phone. They then log in on a tablet. The backend issues a new JWT for the tablet and invalidates the phone's session (single-session policy). The phone's Riverpod state still has the old JWT in memory. The next API call from the phone returns `401 Unauthorized`. What happens in Flutter, and how should it be handled?

**Answer:**

The `401` should be caught by a **Dio auth interceptor** that:

1. Detects the 401 response.
2. Attempts a **silent token refresh** using the stored refresh token (`POST /api/auth/refresh`).
3. If the refresh also returns 401 (refresh token revoked), clears all local auth state and navigates to the login screen.
4. If the refresh succeeds, retries the original request with the new access token.

```dart
// auth_interceptor.dart
class AuthInterceptor extends QueuedInterceptor {
  final Ref _ref;
  AuthInterceptor(this._ref);

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    if (err.response?.statusCode != 401) return handler.next(err);

    try {
      // Attempt silent refresh
      final newToken = await _ref
          .read(authRepositoryProvider)
          .refreshToken(); // POST /api/auth/refresh

      _ref.read(authStateProvider.notifier).updateToken(newToken);

      // Retry original request
      final retried = await _ref
          .read(dioProvider)
          .fetch(err.requestOptions
            ..headers['Authorization'] = 'Bearer $newToken');
      return handler.resolve(retried);
    } catch (_) {
      // Refresh failed — session fully invalidated
      await _ref.read(authStateProvider.notifier).logout();
      _ref.read(routerProvider).go('/login');
      handler.next(err);
    }
  }
}
```

---

### Scenario 10 — Face Verification Selfie Upload: Transactions & Rollback

**Question:**
> A user uploads their face verification selfie. The backend: (1) uploads the image to Cloudinary, (2) calls the face-match API, (3) writes the result to `user_verifications`. Step 3 fails due to a DB timeout. Steps 1 and 2 have already succeeded. Should the backend roll back the Cloudinary upload? What does the Flutter app show?

**Answer:**

Cloudinary uploads are **external side effects** — they cannot be rolled back via a DB transaction. This is the classic "outbox pattern" problem. The recommended approach:

- The backend **does not** attempt to delete the Cloudinary image on DB failure (the deletion itself could fail).
- Instead, the backend stores the Cloudinary URL in a transient log and retries step 3 asynchronously (via a retry queue or scheduled job).
- The API returns `202 Accepted` rather than `200 OK` to indicate "processing in progress".

**Flutter-side UX:**
- Show "Verification in progress…" state rather than success or failure.
- Poll `GET /api/verification/status` every 10 seconds.
- Display the result once the status is `VERIFIED` or `REJECTED`.

```dart
// verification_status_poller.dart
final verificationStatusProvider =
    StreamProvider.autoDispose<VerificationStatus>((ref) async* {
  final repo = ref.read(verificationRepositoryProvider);

  while (true) {
    final status = await repo.getStatus(); // GET /api/verification/status

    yield status;

    if (status == VerificationStatus.verified ||
        status == VerificationStatus.rejected) {
      break; // Terminal state — stop polling
    }

    await Future.delayed(const Duration(seconds: 10));
  }
});

// In the verification waiting screen:
final statusAsync = ref.watch(verificationStatusProvider);

statusAsync.when(
  loading: () => const VerificationPendingWidget(),
  error: (e, _) => ErrorRetryWidget(onRetry: () => ref.invalidate(verificationStatusProvider)),
  data: (status) => switch (status) {
    VerificationStatus.pending => const VerificationPendingWidget(),
    VerificationStatus.verified => const VerificationSuccessWidget(),
    VerificationStatus.rejected => const VerificationRejectedWidget(),
  },
);
```

---

### Scenario 11 — STOMP WebSocket Reconnection: Concurrency in Real-Time Chat

**Question:**
> A user is mid-conversation when the WebSocket connection drops. The STOMP client reconnects after 5 seconds. During those 5 seconds, 3 new messages were sent by the other user. How does the Flutter app recover the missed messages?

**Answer:**

WebSocket connections do not guarantee delivery of messages sent while disconnected. The recovery strategy:

1. **On reconnect:** Fire `GET /api/chat/{conversationId}/messages?since={lastKnownTimestamp}` to fetch any messages missed during the outage.
2. **Merge and deduplicate** with the in-memory message list (by `messageId`).
3. **Show a "Reconnecting…" banner** while disconnected so the user knows their messages may not be delivered.

```dart
// stomp_chat_client.dart
late StompClient _client;
DateTime? _lastMessageTime;

void _onConnect(StompFrame frame) {
  _client.subscribe(
    destination: '/user/queue/messages',
    callback: _handleIncoming,
  );
  // Fetch missed messages after every reconnection
  _fetchMissedMessages();
}

Future<void> _fetchMissedMessages() async {
  if (_lastMessageTime == null) return;

  final missed = await ref
      .read(messageRepositoryProvider)
      .getMessagesSince(
        conversationId: conversationId,
        since: _lastMessageTime!,
      );

  final notifier = ref.read(chatProvider(conversationId).notifier);
  for (final msg in missed) {
    notifier.insertMessage(msg); // Deduplicates by ID internally
  }
}

void _handleIncoming(StompFrame frame) {
  final msg = Message.fromJson(jsonDecode(frame.body!));
  _lastMessageTime = msg.createdAt;
  ref.read(chatProvider(conversationId).notifier).insertMessage(msg);
}

void _onDisconnect(StompFrame frame) {
  ref.read(chatConnectionStatusProvider.notifier).state =
      ConnectionStatus.reconnecting;
}
```

---

### Scenario 12 — Database Indexing: Slow Daily Match Query

**Question:**
> QA reports that `GET /api/matches/daily` takes 4–6 seconds on a device with good Wi-Fi. The backend logs show the query is doing a full table scan on the `matches` table (500,000 rows). As a Flutter engineer, how do you diagnose this from the app side, and what do you report to the backend team?

**Answer:**

**Diagnosis from the Flutter side:**

1. Add timing logs in the Dio interceptor:

```dart
// timing_interceptor.dart
class TimingInterceptor extends Interceptor {
  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    options.extra['startTime'] = DateTime.now();
    handler.next(options);
  }

  @override
  void onResponse(Response response, ResponseInterceptorHandler handler) {
    final start = response.requestOptions.extra['startTime'] as DateTime?;
    if (start != null) {
      final elapsed = DateTime.now().difference(start);
      debugPrint(
        '[Timing] ${response.requestOptions.method} '
        '${response.requestOptions.path} → ${elapsed.inMilliseconds}ms',
      );
    }
    handler.next(response);
  }
}
```

2. The log shows: `[Timing] GET /api/matches/daily → 5234ms`

**Report to backend team:**
- Endpoint: `GET /api/matches/daily`
- Observed latency: 4–6 s (baseline expectation: < 300 ms)
- Payload size is small (5 match objects), ruling out serialisation overhead
- Likely cause: missing index on `matches(user_id, match_date, status)`
- Suggested fix: `CREATE INDEX idx_matches_user_date_status ON matches(user_id, match_date, status);`

**Flutter UX mitigation (while the backend fix is being deployed):**
- Show a skeleton loading screen for match cards.
- Cache the previous day's matches and display them immediately on open (stale-while-revalidate).
- Show a subtle "Refreshing…" indicator, not a full-screen blocker.

---

## Quick Reference Summary

| Concept | Key Flutter Pattern | Xparience Feature |
|---------|--------------------|--------------------|
| Race Condition | `AsyncNotifier` loading guard | Match accept/reject |
| Row Locking | Retry with exponential back-off | Subscription upgrade |
| Idempotency | Idempotency-Key header via Dio interceptor | Stripe upgrade, AI icebreaker |
| Concurrency | `compute()`, `CancelToken`, `Isolate` | Selfie upload, image compression |
| Transactions | Optimistic update + rollback | Date invite, mutual match creation |
| Database Indexing | Skeleton UI, pagination, timing interceptor | Daily matches, chat history |
| Caching | TTL + stale-while-revalidate, `ref.invalidate()` | Subscription tier, icebreaker count |
| Rate Limiting | 429 handler, cooldown timer, `Retry-After` | OTP resend, AI icebreaker |

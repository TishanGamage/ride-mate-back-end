# Daily Commuter Ride-Share — Cost Split Algorithm Prompt

## App Concept
This is a **daily commuter ride-share app**. The driver is already travelling this route for their own purpose — the route is NOT created for passengers. Passengers are commuters who share the driver's existing journey. Because the driver is going that way anyway, passengers get a built-in discount compared to a private hire. The algorithm must reflect this fairly.

---

## Fundamental Principle

> The driver's route exists regardless of passengers.
> Passengers pay a **share of the fuel/running cost** — not the full cost.
> The more passengers share a segment, the less each one pays.
> The driver always benefits by recovering more cost as passengers increase.

---

## Cost Split Scale (Main Road Segments)

| Passengers in car | Each passenger pays | Driver recovers | Notes |
|---|---|---|---|
| 0 (driver alone) | — | 0% | Driver bears full cost |
| 1 passenger | **60%** | 60% | Good deal for both |
| 2 passengers | **40% each** | 80% | Driver recovers more |
| 3 passengers | **30% each** | 90% | Near full recovery |
| 4 passengers | **25% each** | 100% | Full cost recovered |
| 5+ passengers | `max(100 / (N+1), 20)%` each | >100% = profit | N+1 treats driver as one share unit |

> **Formula for N passengers:** `share = max(60 / N, 20)%`
> - N=1 → 60%, N=2 → 40%, N=3 → 30%, N=4 → 25%, N=5 → 20% (floor)
> - Minimum floor is **20%** — no passenger ever pays less than 20% of their segment cost regardless of group size.

---

## Side Trip Rule (Detours Off Main Road)

When the driver makes a detour off the main road to pick up or drop off a specific passenger (out and back), that detour cost is split as follows:

- **That passenger pays 60%** of the detour cost (same single-passenger rate — driver is doing the detour as a favour, not as their own route)
- **Driver absorbs 40%** of the detour cost (goodwill, since they agreed to the detour)
- No other passenger shares the detour cost — it is not their journey

> If a passenger's detour is very long, the app should warn the driver before accepting — this can be a separate validation rule.

---

## Algorithm (Step by Step)

```
FUNCTION calculateCosts(ride):

  INPUT:
    ride.segments = list of:
      {
        distance: number (km),
        type: "main" | "side_trip",
        passengers: [passengerID, ...],   // who is in the car
        side_trip_for: passengerID        // only if type = "side_trip"
      }
    ride.cost_per_km: number  // base running cost per km (fuel, wear, etc.)

  OUTPUT:
    result = {
      passengerID: {
        total: number,
        breakdown: [{ segment, km, share_pct, charge }]
      }
    }

  FUNCTION getSplitRate(n):
    // n = number of passengers in car for this segment
    IF n == 0: RETURN 0
    rate = 60 / n
    RETURN max(rate, 20)   // 20% floor

  INIT cost_per_passenger = { all passengerIDs: 0 }

  FOR each segment in ride.segments:

    segment_cost = segment.distance * ride.cost_per_km

    IF segment.type == "side_trip":
      owner = segment.side_trip_for
      passenger_share = segment_cost * 0.60   // passenger pays 60%
      driver_share    = segment_cost * 0.40   // driver absorbs 40%
      cost_per_passenger[owner] += passenger_share

    ELSE IF segment.type == "main":
      n = count(segment.passengers)
      IF n == 0: CONTINUE  // driver alone, skip

      share_pct = getSplitRate(n)
      per_passenger_cost = segment_cost * (share_pct / 100)

      FOR each passenger in segment.passengers:
        cost_per_passenger[passenger] += per_passenger_cost

  RETURN cost_per_passenger
```

---

## Example — The Sample Commute Route

Assume cost = $1 per km.

| Segment | km | Who's in car | Share % | P1 charge | P2 charge | P3 charge |
|---|---|---|---|---|---|---|
| Driver alone (start) | 5 | none | — | — | — | — |
| P1 side trip pickup (out+back) | 10 | side trip for P1 | 60% | $6.00 | — | — |
| Driver + P1 | 10 | P1 | 60% | $6.00 | — | — |
| P2 side trip pickup (out+back) | 16 | side trip for P2 | 60% | — | $9.60 | — |
| Driver + P1 + P2 | 15 | P1, P2 | 40% each | $6.00 | $6.00 | — |
| P3 side trip pickup (out+back) | 10 | side trip for P3 | 60% | — | — | $6.00 |
| Driver + P1 + P2 + P3 | 40 | P1, P2, P3 | 30% each | $12.00 | $12.00 | $12.00 |
| P2 side trip drop (out+back) | 10 | side trip for P2 | 60% | — | $6.00 | — |
| Driver + P1 + P3 | 10 | P1, P3 | 40% each | $4.00 | — | $4.00 |
| P1 side trip drop (out+back) | 20 | side trip for P1 | 60% | $12.00 | — | — |
| Driver + P3 | 15 | P3 | 60% | — | — | $9.00 |
| P3 side trip drop (out+back) | 6 | side trip for P3 | 60% | — | — | $3.60 |
| Driver alone (end) | 5 | none | — | — | — | — |
| **TOTAL** | | | | **$46.00** | **$33.60** | **$34.60** |

---

## What I Want You To Build

Please implement this algorithm with the following components:

### 1. Data Models
```
Passenger { id, name }
Segment { id, distance_km, type, passengers[], side_trip_for? }
Ride { id, segments[], cost_per_km, passengers[] }
```

### 2. Core Functions
- `getSplitRate(n)` — returns share % for n passengers using `max(60/n, 20)`
- `calculateCosts(ride)` — returns total cost per passenger
- `generateReceipt(ride, passengerID)` — itemised receipt per passenger

### 3. Receipt Format (per passenger)
```
Passenger: P1
-----------------------------
Segment 1  | 10 km side trip  | 60%  | $6.00
Segment 2  | 10 km main road  | 60%  | $6.00
Segment 3  | 15 km main road  | 40%  | $6.00
...
-----------------------------
TOTAL DUE:                           $46.00
```

### 4. Edge Cases to Handle
- Driver travelling alone on a segment → no charge to anyone
- Passenger with no side trips → skip side trip calculation
- All passengers boarding at the same location → correctly group them
- N >= 5 passengers → use the formula `max(60/N, 20)`, apply 20% floor
- Zero distance segment → skip, charge $0
- Minimum fare → optionally enforce a minimum charge per passenger per ride

### 5. Language
Implement in **[JavaScript / Python / TypeScript — replace with your preference]** with clear comments on every function.

---

## Summary of the Logic in Two Sentences

> The driver is a daily commuter going this route anyway — passengers pay a fair share of running costs, not a full private-hire fee. One passenger pays 60%, and every additional passenger reduces each person's share using the formula `max(60 ÷ N, 20)%`, rewarding the driver for carrying more people while keeping the ride affordable for everyone.

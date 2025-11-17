# Coroutines Visualization Tool - Project Plan

## Project Overview
Build an interactive web-based tool to visualize Kotlin coroutines concepts including CPU cores, threads, dispatchers, and coroutine execution using a simulation approach.

## Architecture

### Backend (Kotlin/Ktor)
- Serve static frontend assets
- Provide REST/WebSocket API for simulation control
- Implement coroutine simulation engine
- Generate execution events in a controlled manner

### Frontend (JavaScript)
- Canvas-based visualization
- Control panel for scenario selection and playback
- Real-time rendering of coroutine states

## Testing Strategy

**Test as you build**: Each phase should include corresponding tests before moving to the next phase.

### Backend Testing (Kotlin)
- Unit tests for models, state machines, and business logic
- Integration tests for WebSocket communication
- Scenario validation tests
- Use Kotlin Test and MockK for mocking

### Frontend Testing (JavaScript)
- Unit tests for utility functions and state management
- Integration tests for WebSocket client
- Manual UI testing (automated UI tests optional for later)
- Use Jest or similar framework

### Test Coverage Goals
- Backend: 80%+ coverage for simulation engine
- Critical path: 100% coverage for state transitions and event handling
- Scenarios: Each scenario should have validation tests

## Phase 1: Core Infrastructure

### 1.1 Frontend Setup
- [x] Create `src/main/resources/static/` directory structure
  - `index.html` - Main UI
  - `css/styles.css` - Styling
  - `js/main.js` - Entry point
  - `js/visualizer.js` - Canvas rendering engine
  - `js/websocket-client.js` - Communication layer
- [ ] **Tests**: Manual UI verification (backend needed for full testing)

### 1.2 Backend Infrastructure
- [ ] Add Ktor plugins:
  - Static content serving
  - WebSockets support
  - CORS (for development)
  - Content negotiation (JSON)
- [ ] Create simulation engine package structure:
  - `simulation/` - Core simulation engine
  - `simulation/models/` - Data models
  - `simulation/scenarios/` - Predefined scenarios
  - `simulation/events/` - Event types
- [ ] **Tests**: Integration test for static file serving
- [ ] **Tests**: WebSocket connection test

## Phase 2: Simulation Engine

### 2.1 Core Models
- [ ] `SimulatedCpu` - Represents CPU cores (e.g., 4 or 8 cores)
- [ ] `SimulatedThread` - Thread with state (RUNNING, WAITING, PARKED, TERMINATED)
- [ ] `SimulatedDispatcher` - Dispatcher types (Default, IO, Main, Custom)
- [ ] `SimulatedCoroutine` - Coroutine with state and parent-child relationships
- [ ] `ThreadPool` - Manages threads for a dispatcher
- [ ] **Tests**: Unit tests for each model class
- [ ] **Tests**: Test model immutability and data validation

### 2.2 State Machine
- [ ] Coroutine states: CREATED → ACTIVE → SUSPENDED → RESUMED → COMPLETED/CANCELLED
- [ ] Thread states: IDLE → RUNNING → BLOCKED → PARKED
- [ ] Implement state transitions and validation
- [ ] **Tests**: Test all valid state transitions
- [ ] **Tests**: Test invalid state transitions throw appropriate errors
- [ ] **Tests**: Test edge cases (e.g., cancelling completed coroutine)

### 2.3 Event System
- [ ] Define event types:
  - `CoroutineCreated`
  - `CoroutineStarted`
  - `CoroutineSuspended`
  - `CoroutineResumed`
  - `CoroutineCompleted`
  - `CoroutineCancelled`
  - `ThreadAssigned`
  - `ThreadReleased`
  - `DispatcherQueued`
- [ ] Event serialization for WebSocket transmission
- [ ] Event replay capability for step-through debugging
- [ ] **Tests**: Test event serialization/deserialization
- [ ] **Tests**: Test event ordering and timestamps
- [ ] **Tests**: Test event replay produces same state

### 2.4 Simulation Clock
- [ ] Virtual time system (can speed up/slow down/pause)
- [ ] Event scheduler with timestamps
- [ ] Tick-based execution model
- [ ] **Tests**: Test clock pause/resume functionality
- [ ] **Tests**: Test speed multiplier calculations
- [ ] **Tests**: Test event scheduling and ordering

## Phase 3: Visualization Engine

### 3.1 Canvas Layout
- [ ] Bottom layer: CPU cores (fixed grid)
- [ ] Middle layer: Thread pools (grouped by dispatcher)
- [ ] Top layer: Coroutines (floating, animated)
- [ ] Side panel: Dispatcher queues

### 3.2 Visual Elements
- [ ] CPU Core: Box with active/idle indicator
- [ ] Thread: Box with ID, current coroutine, state color
- [ ] Coroutine: Bubble/box with ID, state, parent lines
- [ ] Dispatcher: Container with thread pool and queue
- [ ] Connections: Lines showing coroutine→thread→core relationships

### 3.3 Animations
- [ ] Coroutine creation (fade in)
- [ ] Coroutine dispatch (move to queue)
- [ ] Thread assignment (line connecting coroutine to thread)
- [ ] Suspension (coroutine becomes transparent/dotted)
- [ ] Context switch (coroutine moves between dispatchers)
- [ ] Cancellation (cascade effect through children)
- [ ] Completion (fade out)

### 3.4 Controls
- [ ] Play/Pause simulation
- [ ] Speed control (0.25x, 0.5x, 1x, 2x, 4x)
- [ ] Step forward/backward
- [ ] Reset simulation
- [ ] Scenario selector
- [ ] Timeline scrubber

## Phase 4: Scenarios

**Each scenario implementation must include validation tests**

### 4.1 Basic Scenarios
- [ ] **Scenario 1: Single Launch**
  - One coroutine on Default dispatcher
  - Show: Creation → Thread assignment → Execution → Completion
  - **Tests**: Verify correct state sequence and event order

- [ ] **Scenario 2: Multiple Launches**
  - 10 coroutines launched concurrently
  - Show: Thread pool behavior, queuing when threads full
  - **Tests**: Verify all coroutines complete, test queue behavior

- [ ] **Scenario 3: Launch vs Async**
  - Side-by-side comparison
  - Show: async returning Deferred, await suspending
  - **Tests**: Verify Job vs Deferred behavior, test await timing

### 4.2 Intermediate Scenarios
- [ ] **Scenario 4: withContext**
  - Coroutine switching from Default to IO dispatcher
  - Show: Suspension, queue on new dispatcher, resume on new thread
  - **Tests**: Verify dispatcher switch, test thread changes

- [ ] **Scenario 5: delay() vs Thread.sleep()**
  - Two coroutines: one using delay, one using Thread.sleep
  - Show: delay suspends (thread free), sleep blocks (thread occupied)
  - **Tests**: Verify thread release on delay, blocking on sleep

- [ ] **Scenario 6: Parent-Child Structured Concurrency**
  - Parent launches multiple children
  - Show: Hierarchy, parent waiting for children
  - **Tests**: Verify parent-child relationships, test completion order

### 4.3 Advanced Scenarios
- [ ] **Scenario 7: Cancellation Propagation**
  - Cancel parent, watch cancellation cascade to children
  - Show: CancellationException propagation
  - **Tests**: Verify all children cancelled, test cancellation order

- [ ] **Scenario 8: Exception Handling**
  - Coroutine throws exception
  - Show: Exception bubbling up, supervisor vs regular scope behavior
  - **Tests**: Test exception propagation, verify supervisor isolation

- [ ] **Scenario 9: Custom Dispatcher**
  - Single-threaded custom dispatcher
  - Show: All coroutines on one thread, sequential execution
  - **Tests**: Verify single thread usage, test sequential ordering

- [ ] **Scenario 10: CPU-bound vs IO-bound**
  - Compare workload on Default vs IO dispatcher
  - Show: Thread pool sizing differences
  - **Tests**: Verify correct dispatcher selection, test pool sizes

### 4.4 Complex Scenarios
- [ ] **Scenario 11: Channel Communication**
  - Producer-consumer pattern
  - Show: Data flowing between coroutines
  - **Tests**: Verify message ordering, test backpressure

- [ ] **Scenario 12: Flow**
  - Cold flow with multiple collectors
  - Show: Lazy execution, backpressure
  - **Tests**: Verify cold start behavior, test multiple collectors

## Phase 5: Educational Features

### 5.1 Information Panel
- [ ] Current state summary (active/suspended/completed coroutines)
- [ ] Thread utilization metrics
- [ ] Dispatcher queue lengths
- [ ] Selected coroutine details (ID, state, parent, children, stack)

### 5.2 Code Display
- [ ] Show equivalent Kotlin code for current scenario
- [ ] Highlight current execution point in code
- [ ] Syntax highlighting

### 5.3 Tooltips & Help
- [ ] Hover over elements for details
- [ ] Concept explanations (what is a dispatcher?, what is structured concurrency?)
- [ ] Help modal with key concepts

### 5.4 Interactive Mode
- [ ] Allow users to create custom scenarios via UI
- [ ] Drag-and-drop coroutine building blocks
- [ ] Configure dispatcher thread pools

## Phase 6: Polish & Deployment

### 6.1 UI/UX Improvements
- [ ] Responsive design
- [ ] Dark/light theme
- [ ] Smooth animations (60fps)
- [ ] Accessibility (keyboard controls, screen reader support)

### 6.2 Performance
- [ ] Optimize canvas rendering
- [ ] Limit event history to prevent memory issues
- [ ] Efficient WebSocket message batching

### 6.3 Documentation
- [ ] User guide
- [ ] API documentation for adding new scenarios
- [ ] Architecture documentation

### 6.4 Deployment
- [ ] Docker container
- [ ] Environment configuration
- [ ] Deployment instructions (local, cloud)

## Technical Decisions

### Simulation Approach Rationale
- **Pros**: Full control over timing, reproducible scenarios, no real threading complexity
- **Cons**: Not showing actual JVM behavior, simplified model
- **Trade-off**: Prioritize clarity and teaching over 100% accuracy

### Frontend Tech Stack
- **Canvas API** instead of SVG for better animation performance with many elements
- **Vanilla JS** to keep dependencies minimal (can add framework later if needed)
- **WebSocket** for bi-directional communication (simulation control + event streaming)

### Color Scheme Ideas
- CPU Cores: Gray (idle), Green (active)
- Threads: Blue (Default), Orange (IO), Purple (Main), Yellow (Custom)
- Coroutines: Gradient by state (green=active, gray=suspended, red=cancelled)
- Connections: Thin lines, semi-transparent

## File Structure
```
visualise-coroutines/
├── src/
│   ├── main/
│   │   ├── kotlin/visualise/coroutines/
│   │   │   ├── Application.kt
│   │   │   ├── Routing.kt
│   │   │   ├── WebSocketHandler.kt
│   │   │   └── simulation/
│   │   │       ├── SimulationEngine.kt
│   │   │       ├── SimulationClock.kt
│   │   │       ├── models/
│   │   │       │   ├── SimulatedCpu.kt
│   │   │       │   ├── SimulatedThread.kt
│   │   │       │   ├── SimulatedDispatcher.kt
│   │   │       │   └── SimulatedCoroutine.kt
│   │   │       ├── events/
│   │   │       │   ├── SimulationEvent.kt
│   │   │       │   └── EventTypes.kt
│   │   │       └── scenarios/
│   │   │           ├── Scenario.kt
│   │   │           ├── BasicScenarios.kt
│   │   │           └── AdvancedScenarios.kt
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── css/
│   │       │   │   └── styles.css
│   │       │   └── js/
│   │       │       ├── main.js
│   │       │       ├── visualizer.js
│   │       │       ├── websocket-client.js
│   │       │       ├── canvas-renderer.js
│   │       │       ├── controls.js
│   │       │       └── models.js
│   │       ├── application.yaml
│   │       └── logback.xml
│   └── test/
│       ├── kotlin/visualise/coroutines/
│       │   ├── simulation/
│       │   │   ├── models/
│       │   │   │   ├── SimulatedCpuTest.kt
│       │   │   │   ├── SimulatedThreadTest.kt
│       │   │   │   ├── SimulatedDispatcherTest.kt
│       │   │   │   └── SimulatedCoroutineTest.kt
│       │   │   ├── StateMachineTest.kt
│       │   │   ├── SimulationClockTest.kt
│       │   │   ├── events/
│       │   │   │   └── EventSystemTest.kt
│       │   │   └── scenarios/
│       │   │       ├── BasicScenariosTest.kt
│       │   │       ├── IntermediateScenariosTest.kt
│       │   │       └── AdvancedScenariosTest.kt
│       │   └── WebSocketTest.kt
│       └── resources/
│           └── test-scenarios/
├── build.gradle.kts
└── PLAN.md (this file)
```

## Getting Started
1. Start with Phase 1.1: Set up basic HTML page with canvas
2. Create simple rendering test (draw boxes for cores/threads)
3. Move to Phase 2.1: Define basic models
4. Implement Phase 2.4: Simulation clock
5. Build Scenario 1 as proof of concept
6. Iterate from there

## Notes
- Keep simulation simple initially, add complexity gradually
- Focus on clarity of visualization over performance early on
- **Write tests alongside code - never defer testing to later**
- Test each scenario thoroughly before moving to next
- Run tests before committing any code
- Use TDD approach for core simulation logic (write test first, then implementation)
- Consider recording simulation runs for playback
- Future: Could add "challenge mode" where users predict outcomes

## Resources
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Ktor WebSockets Documentation](https://ktor.io/docs/websocket.html)
- [Canvas API Reference](https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API)

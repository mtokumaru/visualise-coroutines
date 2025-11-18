/**
 * Main entry point for the Coroutines Visualizer
 * Coordinates UI controls, WebSocket communication, and visualization
 */

class CoroutinesVisualizer {
    constructor() {
        this.visualizer = null;
        this.wsClient = null;
        this.isPlaying = false;
        this.currentSpeed = 1.0;
        this.currentScenario = 'single-launch';

        this.initializeUI();
        this.initializeVisualizer();
        this.initializeWebSocket();
        this.setupEventListeners();
    }

    initializeUI() {
        // Cache DOM elements
        this.elements = {
            playBtn: document.getElementById('play-btn'),
            pauseBtn: document.getElementById('pause-btn'),
            resetBtn: document.getElementById('reset-btn'),
            stepBtn: document.getElementById('step-btn'),
            speedControl: document.getElementById('speed-control'),
            scenarioSelect: document.getElementById('scenario-select'),
            loadScenarioBtn: document.getElementById('load-scenario-btn'),
            timelineInput: document.getElementById('timeline-input'),
            currentTime: document.getElementById('current-time'),
            statActive: document.getElementById('stat-active'),
            statSuspended: document.getElementById('stat-suspended'),
            statCompleted: document.getElementById('stat-completed'),
            statThreads: document.getElementById('stat-threads'),
            codeDisplay: document.getElementById('code-display'),
            detailsContent: document.getElementById('details-content'),
            tooltip: document.getElementById('tooltip')
        };
    }

    initializeVisualizer() {
        const canvas = document.getElementById('viz-canvas');
        this.visualizer = new Visualizer(canvas);

        // Handle window resize
        window.addEventListener('resize', () => {
            this.visualizer.handleResize();
        });

        // Handle mouse events for tooltips
        canvas.addEventListener('mousemove', (e) => {
            this.handleMouseMove(e);
        });

        canvas.addEventListener('mouseleave', () => {
            this.hideTooltip();
        });
    }

    initializeWebSocket() {
        this.wsClient = new WebSocketClient();

        this.wsClient.onConnected(() => {
            console.log('Connected to simulation server');
            this.updateStatus('Connected');
        });

        this.wsClient.onDisconnected(() => {
            console.log('Disconnected from server');
            this.updateStatus('Disconnected');
        });

        this.wsClient.onEvent((event) => {
            this.handleSimulationEvent(event);
        });

        this.wsClient.onStateUpdate((state) => {
            this.updateStatistics(state);
        });
    }

    setupEventListeners() {
        // Playback controls
        this.elements.playBtn.addEventListener('click', () => this.play());
        this.elements.pauseBtn.addEventListener('click', () => this.pause());
        this.elements.resetBtn.addEventListener('click', () => this.reset());
        this.elements.stepBtn.addEventListener('click', () => this.step());

        // Speed control
        this.elements.speedControl.addEventListener('change', (e) => {
            this.currentSpeed = e.target.value;
            this.wsClient.send({
                type: 'SET_SPEED',
                speed: this.currentSpeed
            });
        });

        // Scenario management
        this.elements.scenarioSelect.addEventListener('change', (e) => {
            this.currentScenario = e.target.value;
        });

        this.elements.loadScenarioBtn.addEventListener('click', () => {
            this.loadScenario(this.currentScenario);
        });

        // Timeline scrubbing
        this.elements.timelineInput.addEventListener('input', (e) => {
            const time = e.target.value; // ensure messages are sent as strings
            this.wsClient.send({
                type: 'SEEK_TIME',
                time: time
            });
        });
    }

    play() {
        this.isPlaying = true;
        this.elements.playBtn.disabled = true;
        this.elements.pauseBtn.disabled = false;

        this.wsClient.send({ type: 'PLAY' });
    }

    pause() {
        this.isPlaying = false;
        this.elements.playBtn.disabled = false;
        this.elements.pauseBtn.disabled = true;

        this.wsClient.send({ type: 'PAUSE' });
    }

    reset() {
        this.pause();
        this.visualizer.clear();
        this.updateStatistics({
            active: 0,
            suspended: 0,
            completed: 0,
            activeThreads: 0,
            totalThreads: 0
        });

        this.wsClient.send({ type: 'RESET' });
        this.elements.timelineInput.value = 0;
        this.elements.currentTime.textContent = '0';
    }

    step() {
        this.wsClient.send({ type: 'STEP' });
    }

    loadScenario(scenarioName) {
        this.reset();

        this.wsClient.send({
            type: 'LOAD_SCENARIO',
            scenario: scenarioName
        });

        // Update code display with scenario code
        this.updateCodeDisplay(scenarioName);
    }

    handleSimulationEvent(event) {
        // Pass event to visualizer for rendering
        this.visualizer.processEvent(event);

        // Update timeline
        if (event.timestamp !== undefined) {
            this.elements.currentTime.textContent = event.timestamp;
            this.elements.timelineInput.value = event.timestamp;
            this.elements.timelineInput.max = Math.max(this.elements.timelineInput.max, event.timestamp);
        }

        // Update statistics based on event type
        const eventType = event.type || '';
        if (eventType.includes('Created')) {
            this.updateStatisticsFromVisualizer();
        } else if (eventType.includes('Completed') || eventType.includes('Cancelled')) {
            this.updateStatisticsFromVisualizer();
        } else if (eventType.includes('Started') || eventType.includes('Suspended')) {
            this.updateStatisticsFromVisualizer();
        }
    }

    updateStatisticsFromVisualizer() {
        const coroutines = this.visualizer.coroutines;
        const threads = this.visualizer.threads;

        const active = coroutines.filter(c => c.state === 'ACTIVE' || c.state === 'RESUMED').length;
        const suspended = coroutines.filter(c => c.state === 'SUSPENDED').length;
        const completed = coroutines.filter(c => c.state === 'COMPLETED' || c.state === 'CANCELLED').length;
        const activeThreads = threads.filter(t => t.active).length;
        const totalThreads = threads.length;

        this.updateStatistics({
            active,
            suspended,
            completed,
            activeThreads,
            totalThreads
        });
    }

    updateStatistics(state) {
        this.elements.statActive.textContent = state.active || 0;
        this.elements.statSuspended.textContent = state.suspended || 0;
        this.elements.statCompleted.textContent = state.completed || 0;
        this.elements.statThreads.textContent =
            `${state.activeThreads || 0}/${state.totalThreads || 0}`;
    }

    updateCodeDisplay(scenarioName) {
        const codeExamples = {
            'single-launch': `// Single coroutine launch
runBlocking {
    launch {
        println("Coroutine started")
        delay(1000)
        println("Coroutine completed")
    }
}`,
            'multiple-launches': `// Multiple concurrent coroutines
runBlocking {
    repeat(10) { i ->
        launch {
            println("Coroutine $i started")
            delay(500)
            println("Coroutine $i done")
        }
    }
}`,
            'launch-vs-async': `// Launch vs Async comparison
runBlocking {
    // Launch: returns Job
    val job = launch {
        delay(1000)
    }

    // Async: returns Deferred<T>
    val deferred = async {
        delay(1000)
        "Result"
    }

    val result = deferred.await()
}`,
            'with-context': `// Switching dispatchers
runBlocking {
    println("Main: \${Thread.currentThread().name}")

    withContext(Dispatchers.IO) {
        println("IO: \${Thread.currentThread().name}")
        // IO work here
    }

    println("Back to main")
}`,
            'delay-vs-sleep': `// delay() vs Thread.sleep()
runBlocking {
    launch {
        println("Using delay - non-blocking")
        delay(1000) // Suspends coroutine
        println("After delay")
    }

    launch {
        println("Using sleep - blocking")
        Thread.sleep(1000) // Blocks thread
        println("After sleep")
    }
}`,
            'structured-concurrency': `// Parent-child hierarchy
runBlocking {
    val parent = launch {
        val child1 = launch {
            delay(1000)
            println("Child 1 done")
        }

        val child2 = launch {
            delay(2000)
            println("Child 2 done")
        }

        // Parent waits for children
    }
    parent.join()
}`,
            'cancellation': `// Cancellation propagation
runBlocking {
    val parent = launch {
        val child1 = launch {
            delay(5000)
        }
        val child2 = launch {
            delay(5000)
        }
    }

    delay(1000)
    parent.cancel() // Cancels all children
}`
        };

        const code = codeExamples[scenarioName] || '// Select a scenario';
        this.elements.codeDisplay.querySelector('code').textContent = code;
    }

    handleMouseMove(e) {
        const rect = e.target.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        const elementInfo = this.visualizer.getElementAt(x, y);

        if (elementInfo) {
            this.showTooltip(e.clientX, e.clientY, elementInfo);
        } else {
            this.hideTooltip();
        }
    }

    showTooltip(x, y, info) {
        const tooltip = this.elements.tooltip;
        tooltip.innerHTML = `
            <strong>${info.type}</strong>
            ${info.details}
        `;
        tooltip.style.left = `${x + 15}px`;
        tooltip.style.top = `${y + 15}px`;
        tooltip.classList.remove('hidden');
    }

    hideTooltip() {
        this.elements.tooltip.classList.add('hidden');
    }

    updateStatus(status) {
        // Could add a status indicator in the UI
        console.log('Status:', status);
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new CoroutinesVisualizer();
    console.log('Coroutines Visualizer initialized');
});

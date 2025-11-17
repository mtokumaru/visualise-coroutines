/**
 * Visualizer - Canvas-based rendering engine for coroutines visualization
 * Handles drawing CPU cores, threads, dispatchers, and coroutines
 */

class Visualizer {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');

        // State
        this.cpuCores = [];
        this.threads = [];
        this.dispatchers = [];
        this.coroutines = [];

        // Layout constants
        this.layout = {
            padding: 20,
            cpuCoreWidth: 80,
            cpuCoreHeight: 60,
            threadWidth: 120,
            threadHeight: 50,
            coroutineSize: 40,
            dispatcherHeight: 150,
            spacing: 10
        };

        // Colors
        this.colors = {
            cpuActive: '#22c55e',
            cpuIdle: '#475569',
            threadDefault: '#3b82f6',
            threadIO: '#f97316',
            threadMain: '#8b5cf6',
            threadCustom: '#eab308',
            coroutineActive: '#22c55e',
            coroutineSuspended: '#94a3b8',
            coroutineCancelled: '#ef4444',
            background: '#0f172a',
            border: '#475569',
            text: '#f1f5f9'
        };

        this.handleResize();
        this.initializeDefaultLayout();
        this.startRenderLoop();
    }

    handleResize() {
        const rect = this.canvas.parentElement.getBoundingClientRect();
        this.canvas.width = rect.width;
        this.canvas.height = rect.height;
        this.render();
    }

    initializeDefaultLayout() {
        // Create default CPU cores (4 cores)
        const coreCount = 4;
        const totalCoreWidth = (this.layout.cpuCoreWidth + this.layout.spacing) * coreCount;
        const startX = (this.canvas.width - totalCoreWidth) / 2;
        const coreY = this.canvas.height - this.layout.cpuCoreHeight - this.layout.padding;

        for (let i = 0; i < coreCount; i++) {
            this.cpuCores.push({
                id: i,
                x: startX + i * (this.layout.cpuCoreWidth + this.layout.spacing),
                y: coreY,
                width: this.layout.cpuCoreWidth,
                height: this.layout.cpuCoreHeight,
                active: false
            });
        }

        // Create default dispatchers
        this.dispatchers = [
            {
                name: 'Default',
                type: 'DEFAULT',
                x: 50,
                y: 100,
                width: 200,
                height: this.layout.dispatcherHeight,
                threads: [],
                queue: []
            },
            {
                name: 'IO',
                type: 'IO',
                x: 280,
                y: 100,
                width: 200,
                height: this.layout.dispatcherHeight,
                threads: [],
                queue: []
            }
        ];
    }

    startRenderLoop() {
        const render = () => {
            this.render();
            requestAnimationFrame(render);
        };
        requestAnimationFrame(render);
    }

    render() {
        // Clear canvas
        this.ctx.fillStyle = this.colors.background;
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw components
        this.drawCPUCores();
        this.drawDispatchers();
        this.drawThreads();
        this.drawCoroutines();
        this.drawConnections();
    }

    drawCPUCores() {
        this.cpuCores.forEach(core => {
            this.ctx.fillStyle = core.active ? this.colors.cpuActive : this.colors.cpuIdle;
            this.ctx.fillRect(core.x, core.y, core.width, core.height);

            this.ctx.strokeStyle = this.colors.border;
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(core.x, core.y, core.width, core.height);

            this.ctx.fillStyle = this.colors.text;
            this.ctx.font = '12px monospace';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(
                `Core ${core.id}`,
                core.x + core.width / 2,
                core.y + core.height / 2
            );
        });
    }

    drawDispatchers() {
        this.dispatchers.forEach(dispatcher => {
            // Draw dispatcher container
            this.ctx.strokeStyle = this.getDispatcherColor(dispatcher.type);
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(dispatcher.x, dispatcher.y, dispatcher.width, dispatcher.height);

            // Draw header
            this.ctx.fillStyle = this.getDispatcherColor(dispatcher.type);
            this.ctx.globalAlpha = 0.2;
            this.ctx.fillRect(dispatcher.x, dispatcher.y, dispatcher.width, 30);
            this.ctx.globalAlpha = 1.0;

            // Draw label
            this.ctx.fillStyle = this.colors.text;
            this.ctx.font = 'bold 14px sans-serif';
            this.ctx.textAlign = 'center';
            this.ctx.fillText(
                dispatcher.name,
                dispatcher.x + dispatcher.width / 2,
                dispatcher.y + 15
            );

            // Draw queue count
            if (dispatcher.queue.length > 0) {
                this.ctx.font = '11px monospace';
                this.ctx.fillStyle = this.colors.text;
                this.ctx.fillText(
                    `Queue: ${dispatcher.queue.length}`,
                    dispatcher.x + dispatcher.width / 2,
                    dispatcher.y + dispatcher.height - 10
                );
            }
        });
    }

    drawThreads() {
        this.threads.forEach(thread => {
            const color = this.getDispatcherColor(thread.dispatcherType);

            // Draw thread box
            this.ctx.fillStyle = color;
            this.ctx.globalAlpha = thread.active ? 0.8 : 0.3;
            this.ctx.fillRect(thread.x, thread.y, thread.width, thread.height);
            this.ctx.globalAlpha = 1.0;

            this.ctx.strokeStyle = color;
            this.ctx.lineWidth = 2;
            this.ctx.strokeRect(thread.x, thread.y, thread.width, thread.height);

            // Draw thread label
            this.ctx.fillStyle = this.colors.text;
            this.ctx.font = '11px monospace';
            this.ctx.textAlign = 'center';
            this.ctx.fillText(
                `Thread-${thread.id}`,
                thread.x + thread.width / 2,
                thread.y + thread.height / 2
            );
        });
    }

    drawCoroutines() {
        this.coroutines.forEach(coroutine => {
            const color = this.getCoroutineColor(coroutine.state);
            const size = this.layout.coroutineSize;

            // Draw coroutine circle
            this.ctx.beginPath();
            this.ctx.arc(
                coroutine.x + size / 2,
                coroutine.y + size / 2,
                size / 2,
                0,
                Math.PI * 2
            );
            this.ctx.fillStyle = color;
            this.ctx.globalAlpha = coroutine.state === 'SUSPENDED' ? 0.5 : 0.9;
            this.ctx.fill();
            this.ctx.globalAlpha = 1.0;

            this.ctx.strokeStyle = this.colors.border;
            this.ctx.lineWidth = 2;
            this.ctx.stroke();

            // Draw coroutine ID
            this.ctx.fillStyle = this.colors.background;
            this.ctx.font = 'bold 12px monospace';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(
                `C${coroutine.id}`,
                coroutine.x + size / 2,
                coroutine.y + size / 2
            );
        });
    }

    drawConnections() {
        // Draw lines connecting coroutines to threads and threads to cores
        this.coroutines.forEach(coroutine => {
            if (coroutine.threadId !== null) {
                const thread = this.threads.find(t => t.id === coroutine.threadId);
                if (thread) {
                    this.drawLine(
                        coroutine.x + this.layout.coroutineSize / 2,
                        coroutine.y + this.layout.coroutineSize / 2,
                        thread.x + thread.width / 2,
                        thread.y + thread.height / 2,
                        this.colors.coroutineActive,
                        1
                    );
                }
            }
        });
    }

    drawLine(x1, y1, x2, y2, color, width) {
        this.ctx.beginPath();
        this.ctx.moveTo(x1, y1);
        this.ctx.lineTo(x2, y2);
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = width;
        this.ctx.globalAlpha = 0.4;
        this.ctx.stroke();
        this.ctx.globalAlpha = 1.0;
    }

    getDispatcherColor(type) {
        switch (type) {
            case 'DEFAULT': return this.colors.threadDefault;
            case 'IO': return this.colors.threadIO;
            case 'MAIN': return this.colors.threadMain;
            default: return this.colors.threadCustom;
        }
    }

    getCoroutineColor(state) {
        switch (state) {
            case 'ACTIVE': return this.colors.coroutineActive;
            case 'SUSPENDED': return this.colors.coroutineSuspended;
            case 'CANCELLED': return this.colors.coroutineCancelled;
            default: return this.colors.coroutineSuspended;
        }
    }

    processEvent(event) {
        // Handle simulation events and update visualization state
        switch (event.type) {
            case 'COROUTINE_CREATED':
                this.addCoroutine(event.coroutine);
                break;
            case 'COROUTINE_STARTED':
                this.updateCoroutine(event.coroutineId, { state: 'ACTIVE' });
                break;
            case 'COROUTINE_SUSPENDED':
                this.updateCoroutine(event.coroutineId, { state: 'SUSPENDED' });
                break;
            case 'COROUTINE_COMPLETED':
                this.removeCoroutine(event.coroutineId);
                break;
            case 'THREAD_ASSIGNED':
                this.assignThread(event.coroutineId, event.threadId);
                break;
            default:
                console.log('Unknown event type:', event.type);
        }
    }

    addCoroutine(coroutine) {
        this.coroutines.push({
            id: coroutine.id,
            x: Math.random() * (this.canvas.width - 100) + 50,
            y: Math.random() * 200 + 50,
            state: 'CREATED',
            threadId: null
        });
    }

    updateCoroutine(id, updates) {
        const coroutine = this.coroutines.find(c => c.id === id);
        if (coroutine) {
            Object.assign(coroutine, updates);
        }
    }

    removeCoroutine(id) {
        this.coroutines = this.coroutines.filter(c => c.id !== id);
    }

    assignThread(coroutineId, threadId) {
        const coroutine = this.coroutines.find(c => c.id === coroutineId);
        if (coroutine) {
            coroutine.threadId = threadId;
        }
    }

    getElementAt(x, y) {
        // Check coroutines
        for (const coroutine of this.coroutines) {
            const size = this.layout.coroutineSize;
            const dx = x - (coroutine.x + size / 2);
            const dy = y - (coroutine.y + size / 2);
            if (dx * dx + dy * dy <= (size / 2) * (size / 2)) {
                return {
                    type: 'Coroutine',
                    details: `ID: ${coroutine.id}<br>State: ${coroutine.state}<br>Thread: ${coroutine.threadId || 'None'}`
                };
            }
        }

        // Check threads
        for (const thread of this.threads) {
            if (x >= thread.x && x <= thread.x + thread.width &&
                y >= thread.y && y <= thread.y + thread.height) {
                return {
                    type: 'Thread',
                    details: `ID: ${thread.id}<br>Dispatcher: ${thread.dispatcherType}<br>Active: ${thread.active}`
                };
            }
        }

        // Check CPU cores
        for (const core of this.cpuCores) {
            if (x >= core.x && x <= core.x + core.width &&
                y >= core.y && y <= core.y + core.height) {
                return {
                    type: 'CPU Core',
                    details: `Core ${core.id}<br>Status: ${core.active ? 'Active' : 'Idle'}`
                };
            }
        }

        return null;
    }

    clear() {
        this.coroutines = [];
        this.threads = [];
        this.render();
    }
}

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

        // Animation state
        this.animations = [];
        this.lastFrameTime = performance.now();

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

    // Animation helpers
    easeOutCubic(t) {
        return 1 - Math.pow(1 - t, 3);
    }

    easeInOutQuad(t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    /**
     * Create an animation
     * @param target - The object to animate
     * @param property - Property name to animate
     * @param from - Starting value
     * @param to - Ending value
     * @param duration - Duration in milliseconds
     * @param easing - Easing function (optional)
     */
    animate(target, property, from, to, duration, easing = this.easeOutCubic.bind(this)) {
        const animation = {
            target,
            property,
            from,
            to,
            duration,
            elapsed: 0,
            easing
        };
        this.animations.push(animation);
    }

    updateAnimations(deltaTime) {
        this.animations = this.animations.filter(animation => {
            animation.elapsed += deltaTime;
            const progress = Math.min(animation.elapsed / animation.duration, 1.0);
            const easedProgress = animation.easing(progress);

            const value = animation.from + (animation.to - animation.from) * easedProgress;
            animation.target[animation.property] = value;

            return progress < 1.0; // Keep animation if not complete
        });
    }

    render() {
        // Calculate delta time for animations
        const now = performance.now();
        const deltaTime = now - this.lastFrameTime;
        this.lastFrameTime = now;

        // Update animations
        this.updateAnimations(deltaTime);

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

            // Use animated opacity with fallback
            const threadOpacity = thread.threadOpacity !== undefined ? thread.threadOpacity : (thread.active ? 0.8 : 0.3);

            // Draw thread box
            this.ctx.fillStyle = color;
            this.ctx.globalAlpha = threadOpacity;
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

            // Use animated properties with fallbacks
            const opacity = coroutine.opacity !== undefined ? coroutine.opacity : 1.0;
            const scale = coroutine.scale !== undefined ? coroutine.scale : 1.0;

            // Draw coroutine circle
            this.ctx.save();
            this.ctx.globalAlpha = opacity * (coroutine.state === 'SUSPENDED' ? 0.5 : 0.9);

            this.ctx.beginPath();
            this.ctx.arc(
                coroutine.x + size / 2,
                coroutine.y + size / 2,
                (size / 2) * scale,
                0,
                Math.PI * 2
            );
            this.ctx.fillStyle = color;
            this.ctx.fill();

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

            this.ctx.restore();
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
        const eventType = event.type || event.constructor.name;

        console.log('Processing event:', eventType, event);

        if (eventType === 'CoroutineCreated' || event.coroutineId !== undefined) {
            if (event.coroutineId && event.dispatcher) {
                // CoroutineCreated
                this.addCoroutine({
                    id: event.coroutineId,
                    dispatcher: event.dispatcher,
                    parentId: event.parentId
                });
            } else if (eventType === 'CoroutineStarted') {
                this.updateCoroutine(event.coroutineId, { state: 'ACTIVE' });
            } else if (eventType === 'CoroutineSuspended') {
                this.updateCoroutine(event.coroutineId, { state: 'SUSPENDED' });
            } else if (eventType === 'CoroutineResumed') {
                this.updateCoroutine(event.coroutineId, { state: 'RESUMED' });
            } else if (eventType === 'CoroutineCompleted') {
                this.updateCoroutine(event.coroutineId, { state: 'COMPLETED' });
                // Fade out and remove after delay
                setTimeout(() => this.removeCoroutine(event.coroutineId), 1000);
            } else if (eventType === 'CoroutineCancelled') {
                this.updateCoroutine(event.coroutineId, { state: 'CANCELLED' });
                setTimeout(() => this.removeCoroutine(event.coroutineId), 1000);
            } else if (eventType === 'ThreadAssigned') {
                this.assignThread(event.coroutineId, event.threadId);
                this.ensureThreadExists(event.threadId);
            } else if (eventType === 'ThreadReleased') {
                this.releaseThread(event.coroutineId, event.threadId);
            } else if (eventType === 'DispatcherQueued') {
                // Coroutine queued on dispatcher
                this.updateCoroutine(event.coroutineId, { queued: true });
            }
        }
    }

    ensureThreadExists(threadId) {
        const existing = this.threads.find(t => t.id === threadId);
        if (!existing) {
            const newThread = {
                id: threadId,
                dispatcherType: 'DEFAULT',
                x: 100 + (threadId * 140),
                y: this.canvas.height - 200,
                width: this.layout.threadWidth,
                height: this.layout.threadHeight,
                active: true,
                state: 'RUNNING',
                threadOpacity: 0.3
            };
            this.threads.push(newThread);

            // Animate thread becoming active
            this.animate(newThread, 'threadOpacity', 0.3, 0.8, 300);
        } else {
            existing.active = true;
            existing.state = 'RUNNING';

            // Animate to active state
            const currentOpacity = existing.threadOpacity !== undefined ? existing.threadOpacity : 0.3;
            this.animate(existing, 'threadOpacity', currentOpacity, 0.8, 300);
        }
    }

    releaseThread(coroutineId, threadId) {
        const thread = this.threads.find(t => t.id === threadId);
        if (thread) {
            thread.active = false;
            thread.state = 'IDLE';

            // Animate thread becoming idle
            const currentOpacity = thread.threadOpacity !== undefined ? thread.threadOpacity : 0.8;
            this.animate(thread, 'threadOpacity', currentOpacity, 0.3, 300);
        }

        const coroutine = this.coroutines.find(c => c.id === coroutineId);
        if (coroutine) {
            coroutine.threadId = null;
        }
    }

    addCoroutine(coroutine) {
        const newCoroutine = {
            id: coroutine.id,
            x: Math.random() * (this.canvas.width - 100) + 50,
            y: Math.random() * 200 + 50,
            state: 'CREATED',
            threadId: null,
            opacity: 0.0,
            scale: 0.3
        };
        this.coroutines.push(newCoroutine);

        // Animate creation: fade in and scale up
        this.animate(newCoroutine, 'opacity', 0.0, 1.0, 400);
        this.animate(newCoroutine, 'scale', 0.3, 1.0, 400);
    }

    updateCoroutine(id, updates) {
        const coroutine = this.coroutines.find(c => c.id === id);
        if (coroutine) {
            Object.assign(coroutine, updates);
        }
    }

    removeCoroutine(id) {
        const coroutine = this.coroutines.find(c => c.id === id);
        if (coroutine) {
            // Animate fade out before removing
            coroutine.opacity = coroutine.opacity !== undefined ? coroutine.opacity : 1.0;
            coroutine.scale = coroutine.scale !== undefined ? coroutine.scale : 1.0;

            this.animate(coroutine, 'opacity', coroutine.opacity, 0.0, 300);
            this.animate(coroutine, 'scale', coroutine.scale, 0.5, 300);

            // Actually remove after animation completes
            setTimeout(() => {
                this.coroutines = this.coroutines.filter(c => c.id !== id);
            }, 350);
        }
    }

    assignThread(coroutineId, threadId) {
        const coroutine = this.coroutines.find(c => c.id === coroutineId);
        if (coroutine) {
            coroutine.threadId = threadId;

            // Animate movement to thread
            const thread = this.threads.find(t => t.id === threadId);
            if (thread) {
                const targetX = thread.x + thread.width / 2 - this.layout.coroutineSize / 2;
                const targetY = thread.y - this.layout.coroutineSize - 10;

                this.animate(coroutine, 'x', coroutine.x, targetX, 500, this.easeInOutQuad.bind(this));
                this.animate(coroutine, 'y', coroutine.y, targetY, 500, this.easeInOutQuad.bind(this));
            }
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

/**
 * WebSocketClient - Handles communication with the backend simulation server
 */

class WebSocketClient {
    constructor() {
        this.ws = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 2000;

        // Callbacks
        this.onConnectedCallback = null;
        this.onDisconnectedCallback = null;
        this.onEventCallback = null;
        this.onStateUpdateCallback = null;

        this.connect();
    }

    connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/simulation`;

        try {
            this.ws = new WebSocket(wsUrl);

            this.ws.onopen = () => {
                console.log('WebSocket connected');
                this.connected = true;
                this.reconnectAttempts = 0;

                if (this.onConnectedCallback) {
                    this.onConnectedCallback();
                }
            };

            this.ws.onclose = () => {
                console.log('WebSocket disconnected');
                this.connected = false;

                if (this.onDisconnectedCallback) {
                    this.onDisconnectedCallback();
                }

                // Attempt reconnection
                if (this.reconnectAttempts < this.maxReconnectAttempts) {
                    this.reconnectAttempts++;
                    console.log(`Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
                    setTimeout(() => this.connect(), this.reconnectDelay);
                }
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };

            this.ws.onmessage = (event) => {
                this.handleMessage(event.data);
            };

        } catch (error) {
            console.error('Failed to create WebSocket:', error);
            // For development, when server isn't running yet
            console.log('Running in offline mode - simulation will be client-side only');
        }
    }

    handleMessage(data) {
        try {
            const message = JSON.parse(data);

            switch (message.type) {
                case 'SIMULATION_EVENT':
                    if (this.onEventCallback) {
                        this.onEventCallback(message.event);
                    }
                    break;

                case 'STATE_UPDATE':
                    if (this.onStateUpdateCallback) {
                        this.onStateUpdateCallback(message.state);
                    }
                    break;

                case 'ERROR':
                    console.error('Simulation error:', message.error);
                    break;

                default:
                    console.log('Unknown message type:', message.type);
            }
        } catch (error) {
            console.error('Failed to parse message:', error);
        }
    }

    send(message) {
        if (this.connected && this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket not connected, cannot send message:', message);
        }
    }

    onConnected(callback) {
        this.onConnectedCallback = callback;
    }

    onDisconnected(callback) {
        this.onDisconnectedCallback = callback;
    }

    onEvent(callback) {
        this.onEventCallback = callback;
    }

    onStateUpdate(callback) {
        this.onStateUpdateCallback = callback;
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
        }
    }
}

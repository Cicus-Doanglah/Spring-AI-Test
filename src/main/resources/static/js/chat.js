class ChatInterface {
    constructor() {
        this.messagesContainer = document.getElementById('messagesContainer');
        this.messageInput = document.getElementById('messageInput');
        this.sendButton = document.getElementById('sendButton');
        this.loadingIndicator = document.getElementById('loadingIndicator');
        this.streamToggle = document.getElementById('streamToggle');

        // Persona selection
        this.persona = "kiseki_sora";
        this.initEventListeners();
        this.currentStreamingMessage = null;
    }

    initEventListeners() {
        this.messageInput.addEventListener('input', this.handleInput.bind(this));

        // Persona selector event listener if you add one
        const personaSelect = document.getElementById('personaSelect');
        if (personaSelect) {
            personaSelect.addEventListener('change', (e) => {
                this.persona = e.target.value;
            });
        }
    }

    handleInput() {
        const hasText = this.messageInput.value.trim().length > 0;
        this.sendButton.disabled = !hasText;
    }

    handleKeyDown(event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }

    async sendMessage() {
        const message = this.messageInput.value.trim();
        if (!message) return;

        // Add user message to chat
        this.addMessage(message, 'user');
        this.messageInput.value = '';
        this.sendButton.disabled = true;

        // Show loading indicator
        this.showLoading();

        try {
            if (this.streamToggle.checked) {
                await this.streamAIResponse(message);
            } else {
                await this.getAIResponse(message);
            }
        } catch (error) {
            this.addMessage('Sorry, there was an error processing your request.', 'ai');
            console.error('Error:', error);
        } finally {
            this.hideLoading();
        }
    }

    // This is the stream
    async streamAIResponse(message) {
        try {
            const response = await fetch('/api/chat/model/stream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    persona: this.persona,
                    message: message
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            this.currentStreamingMessage = this.addMessage('', 'ai', true);
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                const chunk = decoder.decode(value, { stream: true });
                buffer += chunk
//                console.log('Raw chunk received:', chunk);

                const lines = buffer.split('\n');
//                console.log('Lines after split:', lines);
                buffer = lines.pop() || '';

                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const content = line.slice(5);
                        if (content && content !== '[DONE]') {
//                            console.log('Stream chunk:', content);
                            this.processStreamChunk(content);
                        }
                    }
                }
            }

            // Final cleanup
            this.currentStreamingMessage.classList.remove('streaming-text');
//            console.log('Stream completed');

        } catch (error) {
            console.error('Streaming error:', error);
            if (this.currentStreamingMessage) {
                this.currentStreamingMessage.textContent = 'Error: Unable to get streaming response.';
                this.currentStreamingMessage.classList.remove('streaming-text');
            }
            throw error;
        } finally {
            this.currentStreamingMessage = null;
        }
    }

    processStreamChunk(chunk) {
        if (chunk && this.currentStreamingMessage) {
            if (chunk.includes('<new_paragraph>')) {
                chunk = chunk.replace(/<new_paragraph>/g, '\n\n');
            }
            this.appendToMessage(this.currentStreamingMessage, chunk);
            this.scrollToBottom();
        }
    }
    // End

    async getAIResponse(message) {
        try {
            await this.streamAIResponse(message);
        } catch (error) {
            throw error;
        }
    }

    addMessage(content, sender, isStreaming = false) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}-message`;

        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';

        if (isStreaming) {
            contentDiv.classList.add('streaming-text');
        }

        contentDiv.textContent = content;
        messageDiv.appendChild(contentDiv);

        this.messagesContainer.appendChild(messageDiv);
        this.scrollToBottom();

        return contentDiv;
    }

    appendToMessage(messageElement, content) {
        messageElement.textContent += content;
        this.scrollToBottom();
    }

    showLoading() {
        this.loadingIndicator.classList.remove('hidden');
        this.scrollToBottom();
    }

    hideLoading() {
        this.loadingIndicator.classList.add('hidden');
    }

    scrollToBottom() {
        this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    clearChat() {
        this.messagesContainer.innerHTML = `
            <div class="message ai-message">
                <div class="message-content">
                    Ohayou Onii-chan~~! I'm your Sora your lovely Sister. How can I help you today?
                </div>
            </div>
        `;
    }

    // Method to cancel ongoing stream
    cancelStream() {
        if (this.currentStreamingMessage) {
            this.currentStreamingMessage.textContent += ' [Stream cancelled]';
            this.currentStreamingMessage.classList.remove('streaming-text');
            this.currentStreamingMessage = null;
        }
    }

    // Method to change persona
    setPersona(persona) {
        this.persona = persona;
        this.addMessage(`Persona changed to: ${persona}`, 'system');
    }
}

// Initialize chat interface when page loads
document.addEventListener('DOMContentLoaded', () => {
    window.chatInterface = new ChatInterface();
});

// Global functions for HTML onclick handlers
function sendMessage() {
    window.chatInterface.sendMessage();
}

function handleKeyDown(event) {
    window.chatInterface.handleKeyDown(event);
}

function clearChat() {
    window.chatInterface.clearChat();
}

function cancelStream() {
    window.chatInterface.cancelStream();
}

function setPersona(persona) {
    window.chatInterface.setPersona(persona);
}
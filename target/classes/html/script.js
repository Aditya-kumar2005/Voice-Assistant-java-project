// --- JAVASCRIPT CODE ---
        
        /**
         * Function called by HTML buttons (Pause/Resume/Settings) to send commands to Java.
         */
        var javaCommand = function(command) { 
            // window.java is the object exposed by Java's WebEngine.setMember("java", new WebBridge())
            if (window.java && window.java.handleCommand) {
                // This line sends the string command to the Java method WebBridge.handleCommand(String)
                window.java.handleCommand(command);
            } else {
                updateLog("[JS Error] Java bridge not fully initialized when clicking " + command + ".");
            }
        };

        /**
         * Function called by Java's WebGui.updateStatus(String) to update the log.
         */
        function updateLog(message) {
            var logElement = document.getElementById('log');
            
            // 1. Sanitize the message
            var cleanMessage = message.replace(/</g, "&lt;").replace(/>/g, "&gt;");
            
            // 2. Create a new span for the line
            var newLine = document.createElement('span');
            newLine.className = 'log-line';
            
            // Add a timestamp (like a digital clock ⌚)
            var now = new Date();
            var timeString = "[" + now.toLocaleTimeString() + "] ";
            
            newLine.textContent = timeString + cleanMessage;
            
            // 3. Append the new line
            logElement.appendChild(newLine);
            
            // 4. Scroll to the bottom 
            logElement.scrollTop = logElement.scrollHeight; 
        }

        /**
         * Function called by Java (WebGui.clearChatArea()) or button click to empty the display.
         * The voice command 'clear chat' in MediaCommands calls this.
         */
        function clearChat() {
            var logElement = document.getElementById('log');

            if (logElement) {
                // 1. Clear the content, but keep the header line.
                logElement.innerHTML = '<span class="log-line">--- Command History ---</span>';
                
                // 2. Add a friendly confirmation message to the now-cleared log.
                updateLog("Chat area cleared by user command.");
                
                console.log("Chat log cleared via user command.");
            } else {
                console.error("Cannot clear chat: 'log' element not found.");
            }
        }
        
        // --- JARVIS ANIMATION CONTROL FUNCTIONS ---
        
        /**
         * Shows the Jarvis spinning animation (called by Java when listening or processing).
         */
        function showLoadingAnimation() {
            // Story: This is like turning the "Thinking" light 💡 on!
            const container = document.getElementById('jarvis-container');
            if (container) {
                container.classList.add('active');
            }
        }
        
        /**
         * Hides the Jarvis spinning animation (called by Java when processing is done).
         */
        function hideLoadingAnimation() {
            const container = document.getElementById('jarvis-container');
            if (container) {
                container.classList.remove('active');
            }
        }

        // --- END JAVASCRIPT CODE ---
        
        // Initial test message to confirm JS is working
        updateLog("HTML Interface setup complete. Ready to connect."); 
        
        // Optional: Show the animation briefly on load to show it off!
        // showLoadingAnimation();
        
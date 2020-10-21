package ssh2.springboot_ssh_client.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import ssh2.springboot_ssh_client.sshclient.SSHClientHandler;
import ssh2.springboot_ssh_client.sshclient.SSHClientHandlerImpl;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsocketHandler implements WebSocketHandler {
    private final SSHClientHandlerImpl sshClientHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[*] WebSocket is connected");
        sshClientHandler.initConnection(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}

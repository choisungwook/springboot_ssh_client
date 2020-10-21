package ssh2.springboot_ssh_client.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
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
        if(message instanceof TextMessage){
            log.info("[*] 웹소켓 메세지 " + session.getAttributes().get(ConstantPool.USER_UUID_KEY));
            sshClientHandler.recvHandle(((TextMessage) message).getPayload(), session);
        }else {
            log.error("[-] Unexpected WebSocket message type: " + message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[-] wbesocket connection is error");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("[*] Websocket is disconnected");
        sshClientHandler.close(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
